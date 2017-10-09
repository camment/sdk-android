package tv.camment.cammentsdk.data.model;


import com.camment.clientsdk.model.Userinfo;

public final class CUserInfo extends Userinfo {

    private String groupUuid;

    public CUserInfo() {

    }

    public CUserInfo(Userinfo userinfo) {
        setUserCognitoIdentityId(userinfo.getUserCognitoIdentityId());
        setName(userinfo.getName());
        setPicture(userinfo.getPicture());
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

}
