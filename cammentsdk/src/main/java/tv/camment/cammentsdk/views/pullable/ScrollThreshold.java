package tv.camment.cammentsdk.views.pullable;


final class ScrollThreshold {

    private final int up;
    private final int down;

    ScrollThreshold(int up, int down) {
        this.up = up;
        this.down = down;
    }


    int getUp() {
        return up;
    }

    int getDown() {
        return down;
    }

}
