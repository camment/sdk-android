package tv.camment.cammentsdk.views.pullable;


enum Direction {

    UP,
    DOWN,
    BOTH;

    boolean upEnabled() {
        return this != DOWN;
    }

    boolean downEnabled() {
        return this != UP;
    }
}
