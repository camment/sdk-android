package tv.camment.cammentsdk.helpers;


import android.app.Activity;
import android.text.TextUtils;

import java.util.Date;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.AWSManager;

public final class AuthHelper {

    private CammentAuthInfo authInfo;

    private static AuthHelper INSTANCE;

    public static AuthHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthHelper();
        }
        return INSTANCE;
    }

    public boolean isLoggedIn() {
        return isHostAppLoggedIn()
                && !TextUtils.isEmpty(AWSManager.getInstance().getCammentAuthenticationProvider().getToken())
                && !TextUtils.isEmpty(IdentityPreferences.getInstance().getIdentityId());
    }

    public boolean isHostAppLoggedIn() {
        if (authInfo != null && authInfo.getAuthType() != null) {
            switch (authInfo.getAuthType()) {
                case FACEBOOK:
                    CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) authInfo;
                    return !TextUtils.isEmpty(fbAuthInfo.getToken())
                            && fbAuthInfo.getExpires().after(new Date());
            }
        }
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

    public void setAuthInfo(CammentAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public CammentAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void checkLogin() {
        if (isHostAppLoggedIn()) {
            ApiManager.getInstance().getAuthApi().logIn();
        } else {
            CammentSDK.getInstance().getAppAuthIdentityProvider().logIn(CammentSDK.getInstance().getCurrentActivity());
        }
    }

}
