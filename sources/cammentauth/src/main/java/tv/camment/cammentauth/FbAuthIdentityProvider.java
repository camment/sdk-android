package tv.camment.cammentauth;

import android.app.Activity;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentAuthIdentityProvider;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.helpers.AuthHelper;


public final class FbAuthIdentityProvider extends AccessTokenTracker
        implements CammentAuthIdentityProvider {

    @Override
    public CammentAuthType getAuthType() {
        return CammentAuthType.FACEBOOK;
    }

    @Override
    public void logIn(Activity activity) {
        FbHelper.getInstance().logIn(activity);
    }

    @Override
    public void logOut() {
        FbHelper.getInstance().logOut();
    }

    @Override
    public CammentUserInfo getUserInfo() {
        return FbHelper.getInstance().getUserInfo();
    }

    @Override
    public CammentAuthInfo getAuthInfo() {
        return FbHelper.getInstance().getAuthInfo();
    }

    @Override
    public boolean isLoggedIn() {
        return FbHelper.getInstance().isLoggedIn();
    }

    @Override
    public void addCammentAuthListener(CammentAuthListener cammentAuthListener) {
        FbHelper.getInstance().addCammentAuthListener(cammentAuthListener);
    }

    @Override
    public void notifyLogoutSuccessful() {
        FbHelper.getInstance().notifyFbLogoutSuccessful();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FbHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
        if (oldAccessToken == null && currentAccessToken != null) {
            if (AuthHelper.getInstance().isHostAppLoggedIn()) {
                AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();
                ApiManager.getInstance().getAuthApi().logIn();
            }
        } else if (oldAccessToken != null && currentAccessToken == null) {
            CammentSDK.getInstance().getAppAuthIdentityProvider().notifyLogoutSuccessful();
        }
    }

}
