package tv.camment.cammentsdk.auth;


import java.util.HashMap;
import java.util.Map;

public enum CammentAuthType {

    FACEBOOK(0);

    private static Map<Integer, CammentAuthType> map = new HashMap<>();

    static {
        for (CammentAuthType a : CammentAuthType.values()) {
            map.put(a.value, a);
        }
    }

    private int value;

    CammentAuthType(int value) {
        this.value = value;
    }

    public static CammentAuthType fromInt(int value) {
        return map.get(value);
    }

    public int getIntValue() {
        return value;
    }
}
