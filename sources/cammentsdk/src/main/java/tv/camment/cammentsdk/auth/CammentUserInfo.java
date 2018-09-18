package tv.camment.cammentsdk.auth;


public abstract class CammentUserInfo {

    private final CammentAuthType authType;
    private final String name;
    private final String imageUrl;

    CammentUserInfo(CammentAuthType authType, String name, String imageUrl) {
        this.authType = authType;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public CammentAuthType getAuthType() {
        return authType;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
