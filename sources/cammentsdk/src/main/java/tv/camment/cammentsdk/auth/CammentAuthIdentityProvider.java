package tv.camment.cammentsdk.auth;


import android.app.Activity;
import android.content.Intent;

public interface CammentAuthIdentityProvider {

    CammentAuthType getAuthType();

    void logIn(Activity activity);

    void logOut();

    CammentUserInfo getUserInfo();

    CammentAuthInfo getAuthInfo();

    boolean isLoggedIn();

    void addCammentAuthListener(CammentAuthListener cammentAuthListener);

    void notifyLogoutSuccessful();

    void onActivityResult(int requestCode, int resultCode, Intent data);

}
