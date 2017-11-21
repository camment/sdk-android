package tv.camment.cammentsdk.auth;


import java.util.Date;

public final class CammentFbAuthInfo extends CammentAuthInfo {

    private final String facebookUserId;
    private final String token;
    private final Date expires;

    public CammentFbAuthInfo(String facebookId, String token, Date expires) {
        super(CammentAuthType.FACEBOOK);
        this.facebookUserId = facebookId;
        this.token = token;
        this.expires = expires;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public String getToken() {
        return token;
    }

    public Date getExpires() {
        return expires;
    }

}
