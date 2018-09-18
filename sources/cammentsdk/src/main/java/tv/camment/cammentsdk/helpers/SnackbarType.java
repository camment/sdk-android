package tv.camment.cammentsdk.helpers;


import java.util.HashMap;
import java.util.Map;

public enum SnackbarType {

    YOU_JOINED_GROUP("you_joined_group"),
    USER_ONLINE("user_online"),
    USER_OFFLINE("user_offline"),
    SYNCING_WITH_HOST("syncing_with_host"),
    ME_HOST_NOW("me_host_now"),
    USER_HOST_NOW("user_host_now"),
    VIDEO_PAUSED("video_paused"),
    CODEC_ISSUE("codec_issue"),
    USER_JOINED_GROUP("user_joined_group"),
    USER_LEFT_GROUP("user_left_group"),
    USER_BLOCKED("user_blocked"),
    USER_UNBLOCKED("user_unblocked");

    private static Map<String, SnackbarType> map = new HashMap<>();

    static {
        for (SnackbarType a : SnackbarType.values()) {
            map.put(a.value, a);
        }
    }

    private String value;

    SnackbarType(String value) {
        this.value = value;
    }

    public static SnackbarType fromString(String value) {
        return map.get(value);
    }

    public String getStringValue() {
        return value;
    }

}
