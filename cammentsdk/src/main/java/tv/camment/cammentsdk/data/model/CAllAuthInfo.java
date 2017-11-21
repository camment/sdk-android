package tv.camment.cammentsdk.data.model;


import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentUserInfo;

public final class CAllAuthInfo {

    private CammentAuthType authType;
    private CammentAuthInfo authInfo;
    private CammentUserInfo userInfo;

    public CAllAuthInfo(CammentAuthType authType, CammentAuthInfo authInfo, CammentUserInfo userInfo) {
        this.authType = authType;
        this.authInfo = authInfo;
        this.userInfo = userInfo;
    }

    public CammentAuthType getAuthType() {
        return authType;
    }

    public void setAuthType(CammentAuthType authType) {
        this.authType = authType;
    }

    public CammentAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(CammentAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public CammentUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(CammentUserInfo userInfo) {
        this.userInfo = userInfo;
    }

}
