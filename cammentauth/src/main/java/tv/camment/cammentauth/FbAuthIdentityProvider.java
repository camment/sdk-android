package tv.camment.cammentauth;

import android.app.Activity;

import tv.camment.cammentsdk.auth.CammentAuthIdentityProvider;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentUserInfo;


public final class FbAuthIdentityProvider implements CammentAuthIdentityProvider {

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

}
