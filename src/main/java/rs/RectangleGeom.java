package rs;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class RectangleGeom extends Node {
    private final Geometry centerRect;
    private final Geometry topBorder;
    private final Geometry rightBorder;
    private final Geometry bottomBorder;
    private final Geometry leftBorder;
    private final Material borderMat;
    private final Material rectMat;

    private Rectangle bound = new Rectangle(0, 0, 0, 0);
    private int borderWidth = 2;
    private boolean noBorder;
    private ColorRGBA color = new ColorRGBA(0.8f, 0.8f, 0.95f, 0.25f);
    private ColorRGBA borderColor = ColorRGBA.White;

    public RectangleGeom(AssetManager assetManager) {
        super();

        rectMat = makeMaterial(assetManager, color);
        borderMat = makeMaterial(assetManager, borderColor);

        Quad centerQuad = new Quad(1, 1);
        centerRect = new Geometry("rectangle", centerQuad);
        centerRect.setMaterial(rectMat);

        Quad topQuad = new Quad(1, 1);
        topBorder = new Geometry("topBorder", topQuad);
        topBorder.setMaterial(borderMat);

        Quad rightQuad = new Quad(1, 1);
        rightBorder = new Geometry("rightBorder", rightQuad);
        rightBorder.setMaterial(borderMat);

        Quad bottomQuad = new Quad(1, 1);
        bottomBorder = new Geometry("bottomBorder", bottomQuad);
        bottomBorder.setMaterial(borderMat);

        Quad leftQuad = new Quad(1, 1);
        leftBorder = new Geometry("leftBorder", leftQuad);
        leftBorder.setMaterial(borderMat);

        attachChild(centerRect);
    }

    private Material makeMaterial(AssetManager assetManager, ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", color);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        return material;
    }

    public void setBound(float x, float y, float width, float height) {
        setLocalTranslation(x, y, 0);
        centerRect.setLocalTranslation(-width / 2f, -height / 2f, 0);
        centerRect.setLocalScale(width, height, 1);
        if (noBorder) {
            detachChild(topBorder);
            detachChild(rightBorder);
            detachChild(bottomBorder);
            detachChild(leftBorder);
        } else {
            attachChild(topBorder);
            attachChild(rightBorder);
            attachChild(bottomBorder);
            attachChild(leftBorder);
            topBorder.setLocalTranslation(-width / 2f, height / 2f - borderWidth, 0);
            topBorder.setLocalScale(width, borderWidth, 1);
            rightBorder.setLocalTranslation(width / 2f - borderWidth, -height / 2f, 0);
            rightBorder.setLocalScale(borderWidth, height, 1);
            bottomBorder.setLocalTranslation(-width / 2f, -height / 2f, 0);
            bottomBorder.setLocalScale(width, borderWidth, 1);
            leftBorder.setLocalTranslation(-width / 2f, -height / 2f, 0);
            leftBorder.setLocalScale(borderWidth, height, 1);
        }
    }

    public Rectangle getBound() {
        return bound;
    }

    public void setBound(Rectangle bound) {
        this.bound = bound;
        setBound((float) ((bound.minX + bound.getMaxX()) / 2), (float) ((bound.minY + bound.getMaxY()) / 2),
                (float) bound.width, (float) bound.height);
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
        rectMat.setColor("Color", color);
    }

    public ColorRGBA getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(ColorRGBA borderColor) {
        this.borderColor = borderColor;
        borderMat.setColor("Color", borderColor);
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        noBorder = borderWidth == 0;
        this.borderWidth = borderWidth;
    }
}