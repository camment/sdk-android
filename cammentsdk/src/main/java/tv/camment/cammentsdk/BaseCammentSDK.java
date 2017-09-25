package tv.camment.cammentsdk;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.Show;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import java.lang.ref.WeakReference;
import java.util.List;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;

abstract class BaseCammentSDK extends CammentLifecycle implements AccessToken.AccessTokenRefreshCallback {

    static CammentSDK INSTANCE;

    volatile WeakReference<Context> applicationContext;

    private IoTHelper ioTHelper;

    synchronized void init(Context context) {
        if (applicationContext == null || applicationContext.get() == null) {
            if (context == null || !(context instanceof Application)) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            applicationContext = new WeakReference<>(context);

            if (TextUtils.isEmpty(getApiKey())) {
                throw new IllegalArgumentException("Missing CammentSDK API key");
            }

            AWSManager.getInstance().checkKeyStore();

            ((Application) context).registerActivityLifecycleCallbacks(this);

            DataManager.getInstance().clearDataForUserGroupChange();

            ioTHelper = AWSManager.getInstance().getIoTHelper();

            connectToIoT();
        }
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    void setShowUuid(String showUuid) {
        if (TextUtils.isEmpty(showUuid)) {
            throw new IllegalArgumentException("Show uuid can't be null!");
        }

        Show show = new Show();
        show.setUuid(showUuid);

        GeneralPreferences.getInstance().setActiveShowUuid(show.getUuid());
    }

    public void connectToIoT() {
        if (ioTHelper != null
                && FacebookHelper.getInstance().isLoggedIn()) {
            ioTHelper.connect();
        }
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        FacebookHelper.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);

        PermissionHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public String getApiKey() {
        String apiKey;
        try {
            ApplicationInfo ai = getApplicationContext().getPackageManager()
                    .getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            apiKey = bundle.getString("tv.camment.cammentsdk.ApiKey");
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            throw new IllegalArgumentException("Missing CammentSDK API key");
        }
        return apiKey;
    }

    public void handleDeeplink(String scheme) {
        Uri data = CammentSDK.getInstance().getCurrentActivity().getIntent().getData();

        if (data == null
                || !scheme.equals(data.getScheme())) {
            if (GeneralPreferences.getInstance().isFirstStartup()) {
                ApiManager.getInstance().getInvitationApi().getDeferredDeepLink(getDeferredDeepLinkCallback());
                GeneralPreferences.getInstance().setFirstStartup();
            }
            return;
        }

        String authority = data.getAuthority();

        List<String> segments = data.getPathSegments();

        switch (authority) {
            case "group":
                if (segments != null
                        && segments.size() == 1) {
                    if (FacebookHelper.getInstance().isLoggedIn()
                            && ioTHelper != null) {
                        InvitationMessage invitationMessage = new InvitationMessage();
                        invitationMessage.type = MessageType.INVITATION;
                        invitationMessage.body = new InvitationMessage.Body();
                        invitationMessage.body.groupUuid = segments.get(0);
                        invitationMessage.body.key = "#" + AccessToken.getCurrentAccessToken().getUserId();
                        ioTHelper.handleInvitationMessage(invitationMessage);
                    }
                }
                break;
        }
    }

    private CammentCallback<Deeplink> getDeferredDeepLinkCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
                if (!TextUtils.isEmpty(result.getUrl())) {
                    String[] split = result.getUrl().split("/");
                    if (split.length > 0) {
                        InvitationMessage invitationMessage = new InvitationMessage();
                        invitationMessage.type = MessageType.INVITATION;
                        invitationMessage.body = new InvitationMessage.Body();
                        invitationMessage.body.groupUuid = split[split.length - 1];
                        invitationMessage.body.key = "#" + AccessToken.getCurrentAccessToken().getUserId();
                        ioTHelper.handleInvitationMessage(invitationMessage);
                    }
                }
            }

            @Override
            public void onException(Exception exception) {

            }
        };
    }

    void checkLogin() {
        AccessToken.refreshCurrentAccessTokenAsync(this);
    }

    @Override
    public void OnTokenRefreshed(AccessToken accessToken) {
        ApiManager.getInstance().getUserApi().updateUserInfo(true);

        ApiManager.getInstance().retryFailedCallsIfNeeded();
    }

    @Override
    public void OnTokenRefreshFailed(FacebookException exception) {
        FacebookHelper.getInstance().logIn(getCurrentActivity(), false);
    }

    FacebookCallback<LoginResult> getLoginResultFbCallback() {
        return new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                ApiManager.clearInstance();

                DataManager.getInstance().handleFbPermissionsResult();

                ApiManager.getInstance().getUserApi().updateUserInfo(true);

                ApiManager.getInstance().retryFailedCallsIfNeeded();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookLogin", "onError", error);
            }
        };
    }
}
