package tv.camment.cammentsdk.helpers;


import java.util.HashMap;
import java.util.Map;

public enum Step {

    RECORD(0),
    PLAY(1),
    HIDE(2),
    SHOW(3),
    DELETE(4),
    INVITE(5),
    LATER(100),
    TUTORIAL(101);

    public static final int LAST_STEP_ID = 5;

    private static Map<Integer, Step> map = new HashMap<>();

    static {
        for (Step a : Step.values()) {
            map.put(a.value, a);
        }
    }

    private int value;

    Step(int value) {
        this.value = value;
    }

    public static Step fromInt(int value) {
        return map.get(value);
    }

    public int getIntValue() {
        return value;
    }

}
