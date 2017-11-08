package tv.camment.cammentsdk.views.pullable;


public final class StartArea {

    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public StartArea(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }


    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public boolean inBounds(int x, int y) {
        return x > minX && x <= maxX && y > minY && y <= maxY;
    }

}
