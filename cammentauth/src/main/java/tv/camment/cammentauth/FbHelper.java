package tv.camment.cammentauth;


import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;

public final class FbHelper extends BaseAuthHelper {

    private static FbHelper INSTANCE;

    private static final String[] permissions = new String[]{"public_profile", "email",
            "user_friends", "read_custom_friendlists"};

    private final LoginManager loginManager;
    private final CallbackManager callbackManager;

    public static FbHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FbHelper();
        }
        return INSTANCE;
    }

    private FbHelper() {
        super();
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        loginManager.registerCallback(callbackManager, getLoginResultFbCallback());
    }

    boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null
                && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken())
                && !AccessToken.getCurrentAccessToken().isExpired();
    }

    private FacebookCallback<LoginResult> getLoginResultFbCallback() {
        return new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin", "onSuccess " + loginResult.toString());

                if (cammentAuthListeners != null) {
                    for (CammentAuthListener cammentAuthListener : cammentAuthListeners) {
                        if (cammentAuthListener != null) {
                            cammentAuthListener.onLoggedIn(getAuthInfo());
                        }
                    }
                }
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookLogin", "onError", error);
            }
        };
    }

    CammentFbUserInfo getUserInfo() {
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            String facebookUserId = profile.getId();
            String name = profile.getName();
            String imageUrl = ImageRequest.getProfilePictureUri(facebookUserId, 270, 270).toString();

            return new CammentFbUserInfo(name, imageUrl, facebookUserId);
        }
        return null;
    }

    CammentFbAuthInfo getAuthInfo() {
        if (isLoggedIn()) {
            return new CammentFbAuthInfo(AccessToken.getCurrentAccessToken().getUserId(),
                    AccessToken.getCurrentAccessToken().getToken(),
                    AccessToken.getCurrentAccessToken().getExpires());
        }
        return null;
    }

    synchronized void logIn(Activity activity) {
        loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    synchronized void logOut() {
        loginManager.logOut();

        notifyFbLogoutSuccessful();
    }

    void notifyFbLogoutSuccessful() {
        if (cammentAuthListeners != null) {
            for (CammentAuthListener cammentAuthListener : cammentAuthListeners) {
                if (cammentAuthListener != null) {
                    cammentAuthListener.onLoggedOut();
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
