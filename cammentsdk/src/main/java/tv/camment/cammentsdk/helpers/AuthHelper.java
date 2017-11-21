package tv.camment.cammentsdk.helpers;


import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;

public final class AuthHelper {

    private static AuthHelper INSTANCE;

    public static AuthHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthHelper();
        }
        return INSTANCE;
    }

    public boolean isLoggedIn() {
        //TODO
        return false;
    }

    public String getUserId() {
        CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();
        String userId = "";
        if (userInfo != null) {
            switch (userInfo.getAuthType()) {
                case FACEBOOK:
                    userId = ((CammentFbUserInfo) userInfo).getFacebookUserId();
                    break;
            }
        }
        return userId;
    }

}
