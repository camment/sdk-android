package tv.camment.cammentsdk.aws.messages;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;


public enum MessageType {

    @SerializedName("invitation")
    INVITATION(0, "invitation"),

    ONBOARDING(2, "onboarding"), //internal

    @SerializedName("new-user-in-group")
    NEW_USER_IN_GROUP(3, "new-user-in-group"),

    @SerializedName("camment")
    CAMMENT(4, "camment"),

    @SerializedName("camment-deleted")
    CAMMENT_DELETED(5, "camment-deleted"),

    SHARE(8, "share"), //internal

    LOGIN_CONFIRMATION(9, "login-confirmation"), //internal

    @SerializedName("user-removed")
    USER_REMOVED(10, "user-removed"),

    @SerializedName("camment-delivered")
    CAMMENT_DELIVERED(11, "camment-delivered"),

    @SerializedName("ad")
    AD(15, "ad"),

    @SerializedName("user-blocked")
    USER_BLOCKED(16, "user-blocked"),

    @SerializedName("user-unblocked")
    USER_UNBLOCKED(17, "user-unblocked"),

    BLOCKED(18, "blocked"), //internal

    LEAVE_CONFIRMATION(19, "leave_confirmation"), //internal

    BLOCK_CONFIRMATION(20, "block_confirmation"), //internal

    UNBLOCK_CONFIRMATION(21, "unblock_confirmation"), //internal

    @SerializedName("player-state")
    PLAYER_STATE(22, "player-state"),

    @SerializedName("need-player-state")
    NEED_PLAYER_STATE(23, "need-player-state"),

    @SerializedName("new-group-host")
    NEW_GROUP_HOST(24, "new-group-host"),

    @SerializedName("user-online")
    USER_ONLINE(25, "user-online"),

    @SerializedName("user-offline")
    USER_OFFLINE(26, "user-offline"),

    LOGOUT_CONFIRMATION(27, "logout_confirmation"); //internal

    private static Map<Integer, MessageType> map = new HashMap<>();
    private static Map<String, MessageType> nameMap = new HashMap<>();

    static {
        for (MessageType a : MessageType.values()) {
            map.put(a.value, a);
            nameMap.put(a.name, a);
        }
    }

    private int value;
    private String name;

    MessageType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static MessageType fromInt(int value) {
        return map.get(value);
    }

    public int getIntValue() {
        return value;
    }

    public static MessageType fromString(String name) {
        return nameMap.get(name);
    }

    public String getStringValue() {
        return name;
    }

}
