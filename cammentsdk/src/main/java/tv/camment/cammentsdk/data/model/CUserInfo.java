package tv.camment.cammentsdk.data.model;


import com.camment.clientsdk.model.Userinfo;

public final class CUserInfo extends Userinfo {

    private String groupUuid;

    public CUserInfo() {

    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public UserState getUserState() {
        if (getState() == null)
            return UserState.UNDEFINED;

        return UserState.fromString(getState());
    }

}
