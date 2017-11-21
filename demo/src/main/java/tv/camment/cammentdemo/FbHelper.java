package tv.camment.cammentdemo;


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

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;

public class FbHelper {

    private static FbHelper INSTANCE;

    private static final String[] permissions = new String[]{"public_profile", "email",
            "user_friends", "read_custom_friendlists"};

    private final LoginManager loginManager;
    private final CallbackManager callbackManager;
    private final CammentAuthListener cammentAuthListener;

    public static FbHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FbHelper();
        }
        return INSTANCE;
    }

    private FbHelper() {
        cammentAuthListener = CammentSDK.getInstance();
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        loginManager.registerCallback(callbackManager, getLoginResultFbCallback());
    }

    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null
                && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken())
                && !AccessToken.getCurrentAccessToken().isExpired();
    }

    private FacebookCallback<LoginResult> getLoginResultFbCallback() {
        return new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin", "onSuccess " + loginResult.toString());

                if (cammentAuthListener != null) {
                    cammentAuthListener.onLoggedIn(new CammentFbAuthInfo(AccessToken.getCurrentAccessToken().getUserId(), AccessToken.getCurrentAccessToken().getToken(), AccessToken.getCurrentAccessToken().getExpires()));
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

    public CammentUserInfo getUserInfo() {
        Profile profile = Profile.getCurrentProfile();

        String facebookUserId = profile.getId();
        String name = profile.getName();
        String imageUrl = ImageRequest.getProfilePictureUri(facebookUserId, 270, 270).toString();

        return new CammentFbUserInfo(name, imageUrl, facebookUserId);
    }

    public synchronized void logIn(Activity activity) {
        loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    public synchronized void logOut() {
        loginManager.logOut();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
