package rs;


import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import java.util.ArrayList;
import java.util.List;

public class IntersectionUtil {
    public static boolean intersect(Line l1, Line l2) {
        return LineIntersection.doIntersect(l1.p1, l1.p2, l2.p1, l2.p2);
    }

    public static boolean intersect(Line l, Rectangle r) {
        Line l1 = new Line(new Point(r.minX, r.minY), new Point(r.minX, r.getMaxY()));
        Line l2 = new Line(new Point(r.minX, r.minY), new Point(r.getMaxX(), r.minY));
        Line l3 = new Line(new Point(r.getMaxX(), r.getMaxY()), new Point(r.getMaxX(), r.minY));
        Line l4 = new Line(new Point(r.getMaxX(), r.getMaxY()), new Point(r.minX, r.getMaxY()));
        return intersect(l, l1) ||
                intersect(l, l2) ||
                intersect(l, l3) ||
                intersect(l, l4) ||
                r.contain(l.p1);
    }

    public static boolean intersect(BoundingBox bb, Rectangle rectangle, Camera camera) {
        Vector3f[] bbPoints = new Vector3f[8];
        Vector3f min = bb.getMin(null);
        Vector3f max = bb.getMax(null);
        bbPoints[0] = new Vector3f(min.x, min.y, min.z);
        bbPoints[1] = new Vector3f(min.x, min.y, max.z);
        bbPoints[2] = new Vector3f(min.x, max.y, min.z);
        bbPoints[3] = new Vector3f(min.x, max.y, max.z);
        bbPoints[4] = new Vector3f(max.x, min.y, min.z);
        bbPoints[5] = new Vector3f(max.x, min.y, max.z);
        bbPoints[6] = new Vector3f(max.x, max.y, min.z);
        bbPoints[7] = new Vector3f(max.x, max.y, max.z);

        List<Point> projectPoints = new ArrayList<>(8);
        for (Vector3f bbPoint : bbPoints) {
            Vector3f screenPoint = camera.getScreenCoordinates(bbPoint);
            projectPoints.add(new Point(Math.round(screenPoint.x), Math.round(screenPoint.y)));
        }

        List<Point> hull = GrahamScan.getConvexHull(projectPoints);
        for (int i = 0; i < hull.size() - 1; i++) {
            Line l = new Line(hull.get(i), hull.get(i + 1));
            if (intersect(l, rectangle)) {
                return true;
            }
        }

        return rectangle.contain(hull.get(0));
    }
}
