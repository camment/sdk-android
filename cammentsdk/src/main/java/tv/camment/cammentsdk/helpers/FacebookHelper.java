package tv.camment.cammentsdk.helpers;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import tv.camment.cammentsdk.api.ApiManager;


public final class FacebookHelper {

    private static final String TAG = FacebookHelper.class.getSimpleName();

    private static FacebookHelper INSTANCE;

    private static final String[] permissions = new String[]{"public_profile", "email",
            "user_friends"};

    private final LoginManager loginManager;
    private final CallbackManager callbackManager;

    public static FacebookHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FacebookHelper();
        }
        return INSTANCE;
    }

    private FacebookHelper() {
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        loginManager.registerCallback(callbackManager, loginResultFacebookCallback);
    }

    private FacebookCallback<LoginResult> loginResultFacebookCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            Log.e(TAG, "onError", error);
        }
    };

    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken());
    }

    public boolean isMessageForMe(String userId) {
        return isLoggedIn() && !TextUtils.isEmpty(userId) && userId.equals(AccessToken.getCurrentAccessToken().getUserId());
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public synchronized void logIn(Activity activity) {
        loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    public synchronized void logOut() {
        loginManager.logOut();
    }

}
