package rs;

public class Rectangle {
    public int minX;
    public int minY;
    public int width;
    public int height;

    public Rectangle(int minX, int minY, int width, int height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    public boolean contain(Point p) {
        return p.x >= minX && p.x <= minX + width && p.y >= minY && p.y <= minY + height;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxX() {
        return minX + width;
    }

    public int getMaxY() {
        return minY + height;
    }
}
