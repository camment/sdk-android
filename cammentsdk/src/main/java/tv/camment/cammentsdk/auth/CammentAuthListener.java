package tv.camment.cammentsdk.auth;


public interface CammentAuthListener {

    void onLoggedIn(CammentAuthInfo authInfo);

    void onLoggedOut();

    //void onUserInfoChanged(CammentUserInfo userInfo);

    void onAuthInfoChanged(CammentAuthInfo authInfo);

}
