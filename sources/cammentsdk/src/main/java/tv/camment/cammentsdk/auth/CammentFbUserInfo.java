package tv.camment.cammentsdk.auth;


public final class CammentFbUserInfo extends CammentUserInfo {

    private final String facebookUserId;

    public CammentFbUserInfo(String name, String imageUrl, String facebookUserId) {
        super(CammentAuthType.FACEBOOK, name, imageUrl);
        this.facebookUserId = facebookUserId;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

}
