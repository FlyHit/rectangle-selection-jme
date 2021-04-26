package rs;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingVolume;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.round;

public class RectangleSelection extends BaseAppState {
    private Node sceneNode;
    private Node guiNode;
    private Camera selectionCam;
    private Camera cam;
    private RectangleGeom rectangle;
    private boolean increased;
    private boolean readyToRs = false;
    private boolean selecting = false;
    private Vector2f startPoint;
    private Vector2f mousePos = new Vector2f();
    private final boolean[] mouseBtns = new boolean[3];

    private InputManager inputManager;
    private RsInputListener rsInputListener;

    private final Set<Spatial> selections = new HashSet<>();

    private static final RectangleSelection INSTANCE = new RectangleSelection();

    public enum Intersect {
        OUTSIDE,
        INSIDE,
        INTERSECTING
    }

    private RectangleSelection() {
    }

    public static RectangleSelection getInstance() {
        return INSTANCE;
    }

    public Intersect intersect(BoundingVolume bv) {
        int planeState = selectionCam.getPlaneState();
        selectionCam.setPlaneState(0);
        Camera.FrustumIntersect fi = selectionCam.contains(bv);
        if (fi == Camera.FrustumIntersect.Outside) {
            selectionCam.setPlaneState(planeState);
            return Intersect.OUTSIDE;
        } else if (fi == Camera.FrustumIntersect.Inside) {
            return Intersect.INSIDE;
        }
        return Intersect.INTERSECTING;
    }

    @Override
    protected void initialize(Application app) {
        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException();
        }

        SimpleApplication simpleApp = (SimpleApplication) app;
        sceneNode = simpleApp.getRootNode();
        guiNode = simpleApp.getGuiNode();
        cam = app.getCamera();
        selectionCam = cam.clone();
        selectionCam.setName("selection");
        rectangle = new RectangleGeom(app.getAssetManager());
        inputManager = app.getInputManager();
        rsInputListener = new RsInputListener();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        inputManager.addRawInputListener(rsInputListener);
    }

    @Override
    protected void onDisable() {
        inputManager.removeRawInputListener(rsInputListener);
        reset();
    }

    private void updateFrustum(float x1, float y1, float x2, float y2) {
        float n = cam.getFrustumNear();
        float f = cam.getFrustumFar();

        Vector3f pa = cam.getWorldCoordinates(new Vector2f(x1, y1), 0);
        Vector3f pb = cam.getWorldCoordinates(new Vector2f(x2, y1), 0);
        Vector3f pc = cam.getWorldCoordinates(new Vector2f(x1, y2), 0);
        Vector3f pe = cam.getLocation();

        Vector3f vr = pb.subtract(pa).normalize();
        Vector3f vu = (pc.subtract(pa)).normalize();
        Vector3f vn = vr.cross(vu).normalize();

        Vector3f va = pa.subtract(pe);
        Vector3f vb = pb.subtract(pe);
        Vector3f vc = pc.subtract(pe);

        float d = -va.dot(vn);

        float nd = n / d;
        float l = vr.dot(va) * nd;
        float r = vr.dot(vb) * nd;
        float b = vu.dot(va) * nd;
        float t = vu.dot(vc) * nd;

        selectionCam.setFrustum(n, f, l, r, t, b);
        selectionCam.lookAtDirection(vn.negate(), vu);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        // a little optimization: stop selecting when mouse stop
        Vector2f newMousePos = inputManager.getCursorPosition().clone();
        if (selecting && newMousePos.equals(mousePos)) {
            selecting = false;
        } else {
            mousePos = newMousePos;
        }

        if (selecting) {
            if (increased) {
                findGeomIntersected(sceneNode);
            } else {
                Set<Spatial> set = new HashSet<>(selections);
                for (Spatial spatial : set) {
                    findGeomIntersected(spatial);
                }
            }

            System.out.println(selections);
        }
    }

    private void findGeomIntersected(Spatial s) {
        if (s.getCullHint() == Spatial.CullHint.Always) {
            return;
        }

        if (increased) {
            if (selections.stream().anyMatch(selection ->
                    selection instanceof Node && s.hasAncestor((Node) selection))) {
                return;
            }
        }

        Intersect intersection = intersect(s.getWorldBound());
        if (intersection == Intersect.INSIDE) {
            if (s instanceof Node) {
                selections.add(s);
                selections.removeIf(selection -> selection.hasAncestor((Node) s));
            }
        } else if (intersection == Intersect.OUTSIDE) {
            selections.remove(s);
        } else {
            if (!increased) {
                if (selections.stream().anyMatch(selection ->
                        selection instanceof Node && s.hasAncestor((Node) selection))) {
                    return;
                }
            }

            if (s instanceof Geometry) {
                if (isMeshIntersect((Geometry) s)) {
                    selections.add(s);
                } else {
                    selections.remove(s);
                }
                return;
            }

            Node node = (Node) s;
            for (Spatial child : node.getChildren()) {
                findGeomIntersected(child);
            }
        }
    }

    private boolean isMeshIntersect(Geometry g) {
        Rectangle r = rectangle.getBound();
        // triangle vertices
        Vector3f temp1 = new Vector3f();
        Vector3f temp2 = new Vector3f();
        Vector3f temp3 = new Vector3f();
        Point p1;
        Point p2;
        Point p3;
        Mesh m = g.getMesh();
        int triangleCount = m.getTriangleCount();
        for (int i = 0; i < triangleCount; i++) {
            m.getTriangle(i, temp1, temp2, temp3);
            temp1 = g.localToWorld(temp1, null);
            temp2 = g.localToWorld(temp2, null);
            temp3 = g.localToWorld(temp3, null);
            temp1 = cam.getScreenCoordinates(temp1, null);
            temp2 = cam.getScreenCoordinates(temp2, null);
            temp3 = cam.getScreenCoordinates(temp3, null);
            p1 = new Point(round(temp1.x), round(temp1.y));
            p2 = new Point(round(temp2.x), round(temp2.y));
            p3 = new Point(round(temp3.x), round(temp3.y));
            Line l1 = new Line(p1, p2);
            Line l2 = new Line(p1, p3);
            Line l3 = new Line(p2, p3);
            boolean intersecting = IntersectionUtil.intersect(l1, r) ||
                    IntersectionUtil.intersect(l2, r) ||
                    IntersectionUtil.intersect(l3, r);
            if (intersecting) {
                return true;
            }
        }

        return false;
    }

    private void reset() {
        readyToRs = false;
        selecting = false;
        guiNode.detachChild(rectangle);
        rectangle.setBound(new Rectangle(0, 0, 0, 0));
        selections.clear();
    }

    class RsInputListener extends RawInputAdapter {
        @Override
        public void onMouseMotionEvent(MouseMotionEvent evt) {
            if (readyToRs) {
                selecting = true;
                float dx = evt.getX() - startPoint.x;
                float dy = evt.getY() - startPoint.y;
                int height = round(FastMath.abs(dy));
                int width = round(FastMath.abs(dx));
                Rectangle bound = new Rectangle(round(Math.min(evt.getX(), startPoint.getX())),
                        round(Math.min(evt.getY(), startPoint.y)), width, height);
                increased = bound.width * bound.height > rectangle.getBound().width * rectangle.getBound().height;
                rectangle.setBound(bound);
                guiNode.attachChild(rectangle);

                selectionCam.setLocation(cam.getLocation());
                selectionCam.setRotation(cam.getRotation());
                updateFrustum((float) bound.getMinX(), (float) bound.getMinY(),
                        (float) bound.getMaxX(), (float) bound.getMaxY());
            }
        }

        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt) {
            if (!isEnabled()) {
                readyToRs = false;
                return;
            }

            mouseBtns[evt.getButtonIndex()] = evt.isPressed();
            readyToRs = mouseBtns[0] & !mouseBtns[1] & !mouseBtns[2];
            if (!readyToRs) {
                reset();
            } else if (evt.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                startPoint = new Vector2f(evt.getX(), evt.getY());
            }
        }
    }
}
