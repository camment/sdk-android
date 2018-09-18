package tv.camment.cammentsdk.auth;


public abstract class CammentAuthInfo {

    private final CammentAuthType authType;

    CammentAuthInfo(CammentAuthType authType) {
        this.authType = authType;
    }

    public CammentAuthType getAuthType() {
        return authType;
    }

}
