package tv.camment.cammentsdk.views.pullable;


public final class ScrollThreshold {

    private final int up;
    private final int down;

    public ScrollThreshold(int up, int down) {
        this.up = up;
        this.down = down;
    }


    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

}
