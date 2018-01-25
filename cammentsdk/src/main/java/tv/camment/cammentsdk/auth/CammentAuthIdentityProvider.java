package tv.camment.cammentsdk.auth;


import android.app.Activity;

public interface CammentAuthIdentityProvider {

    CammentAuthType getAuthType();

    void logIn(Activity activity);

    void logOut();

    CammentUserInfo getUserInfo();

    CammentAuthInfo getAuthInfo();

    boolean isLoggedIn();

    void addCammentAuthListener(CammentAuthListener cammentAuthListener);

}
