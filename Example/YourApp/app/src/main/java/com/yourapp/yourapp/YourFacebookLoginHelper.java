package com.yourapp.yourapp;


import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public final class YourFacebookLoginHelper {

    private static YourFacebookLoginHelper INSTANCE;

    private static final String[] permissions = new String[]{"public_profile", "email"};

    private final LoginManager loginManager;
    private final CallbackManager callbackManager;

    static YourFacebookLoginHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YourFacebookLoginHelper();
        }
        return INSTANCE;
    }

    private YourFacebookLoginHelper() {
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

    synchronized void logIn(Activity activity) {
        loginManager.logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    synchronized void logOut() {
        loginManager.logOut();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
