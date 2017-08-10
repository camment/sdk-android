package tv.camment.cammentsdk.aws.messages;

import com.google.gson.annotations.SerializedName;


public enum MessageType {

    @SerializedName("invitation")
    INVITATION,

    INVITATION_SENT,

    @SerializedName("new-user-in-group")
    NEW_USER_IN_GROUP,

    @SerializedName("camment")
    CAMMENT,

    @SerializedName("camment-deleted")
    CAMMENT_DELETED;

}
