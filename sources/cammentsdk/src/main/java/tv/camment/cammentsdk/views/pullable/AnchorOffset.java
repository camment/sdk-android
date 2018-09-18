package tv.camment.cammentsdk.views.pullable;


final class AnchorOffset {

    private final int up;
    private final int down;

    AnchorOffset(int up, int down) {
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
