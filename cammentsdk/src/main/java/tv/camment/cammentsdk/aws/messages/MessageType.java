package tv.camment.cammentsdk.aws.messages;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;


public enum MessageType {

    @SerializedName("invitation")
    INVITATION(0),

    INVITATION_SENT(1), //internal

    ONBOARDING(2), //internal

    @SerializedName("new-user-in-group")
    NEW_USER_IN_GROUP(3),

    @SerializedName("camment")
    CAMMENT(4),

    @SerializedName("camment-deleted")
    CAMMENT_DELETED(5),

    SHARE(8), //internal

    LOGIN_CONFIRMATION(9), //internal

    @SerializedName("user-removed")
    USER_REMOVED(10),

    @SerializedName("camment-delivered")
    CAMMENT_DELIVERED(11),

    FIRST_USER_JOINED(12), //internal

    REMOVAL_CONFIRMATION(13), //internal

    KICKED_OUT(14), //internal

    @SerializedName("ad")
    AD(15),

    @SerializedName("user-blocked")
    USER_BLOCKED(16),

    @SerializedName("user-unblocked")
    USER_UNBLOCKED(17),

    BLOCKED(18); //internal

    private static Map<Integer, MessageType> map = new HashMap<>();

    static {
        for (MessageType a : MessageType.values()) {
            map.put(a.value, a);
        }
    }

    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType fromInt(int value) {
        return map.get(value);
    }

    public int getIntValue() {
        return value;
    }


}
