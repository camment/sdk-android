package tv.camment.cammentsdk.helpers;

import android.app.Activity;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;

import java.util.Arrays;

import tv.camment.cammentsdk.CammentSDK;


public final class FacebookHelper {

    private static final String TAG = FacebookHelper.class.getSimpleName();

    private static FacebookHelper INSTANCE;

    private static final String[] permissions = new String[]{"public_profile", "email",
            "user_friends"};

    private final LoginManager loginManager;
    private final CallbackManager callbackManager;

    private boolean showShareOptions;

    public static FacebookHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FacebookHelper();
        }
        return INSTANCE;
    }

    private FacebookHelper() {
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        loginManager.registerCallback(callbackManager, CammentSDK.getInstance().getLoginResultFbCallback());
    }

    public boolean isLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null
                && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken())
                && AccessToken.getCurrentAccessToken().getExpires().getTime() > System.currentTimeMillis();
    }

    public boolean isMessageForMe(String userId) {
        return isLoggedIn() && !TextUtils.isEmpty(userId) && userId.equals(AccessToken.getCurrentAccessToken().getUserId());
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public synchronized void logIn(Activity activity, boolean showShareOptions) {
        this.showShareOptions = showShareOptions;
        loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    public synchronized void logOut() {
        loginManager.logOut();
    }

    public boolean showShareOptions() {
        return showShareOptions;
    }

}
