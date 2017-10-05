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

    @SerializedName("membership-request")
    MEMBERSHIP_REQUEST(6),

    @SerializedName("membership-accepted")
    MEMBERSHIP_ACCEPTED(7),

    SHARE(8); //internal

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