package tv.camment.cammentsdk.data.model;


import java.util.HashMap;
import java.util.Map;

public enum UserState {

    ACTIVE("active"),
    BLOCKED("blocked"),
    UNDEFINED("undefined");

    private static Map<String, UserState> map = new HashMap<>();

    static {
        for (UserState a : UserState.values()) {
            map.put(a.value, a);
        }
    }

    private String value;

    UserState(String value) {
        this.value = value;
    }

    public static UserState fromString(String value) {
        if (map.containsKey(value)) {
            return map.get(value);
        } else {
            return UNDEFINED;
        }
    }

    public String getStringValue() {
        return value;
    }

}
