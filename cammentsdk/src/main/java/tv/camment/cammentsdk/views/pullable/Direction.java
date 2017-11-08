package tv.camment.cammentsdk.views.pullable;


import java.util.HashMap;
import java.util.Map;

public enum Direction {

    UP(0),
    DOWN(1),
    BOTH(2);

    private static Map<Integer, Direction> map = new HashMap<>();

    static {
        for (Direction a : Direction.values()) {
            map.put(a.value, a);
        }
    }

    private int value;

    Direction(int value) {
        this.value = value;
    }

    public static Direction fromInt(int value) {
        return map.get(value);
    }

    public int getIntValue() {
        return value;
    }

    public boolean upEnabled() {
        return this != DOWN;
    }

    public boolean downEnabled() {
        return this != DOWN;
    }
}
