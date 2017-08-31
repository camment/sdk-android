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

import com.camment.clientsdk.model.Show;
import com.facebook.AccessToken;

import java.lang.ref.WeakReference;
import java.util.List;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;

abstract class BaseCammentSDK extends CammentLifecycle {

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

            ApiManager.getInstance().getInvitationApi().getDeferredDeepLink();
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

    private void connectToIoT() {
        if (ioTHelper != null
                && FacebookHelper.getInstance().isLoggedIn()) {
            ioTHelper.connect();
        }
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean fbHandled = FacebookHelper.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);

        if (fbHandled) {
            ApiManager.clearInstance();

            DataManager.getInstance().handleFbPermissionsResult();
            ApiManager.getInstance().getUserApi().updateUserInfo();
            connectToIoT();
        }

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

    void handleDeeplink(Uri data, String scheme) {
        if (data == null
                || !scheme.equals(data.getScheme()))
            return;

        String authority = data.getAuthority();

        List<String> segments = data.getPathSegments();

        switch (authority) {
            case "group":
                if (segments != null
                        && segments.size() == 1) {
                    //TODO check if fb logged in
                    //TODO do accept and then insert user group
                    //#facebooKId
                    if (FacebookHelper.getInstance().isLoggedIn()) {
                        ApiManager.getInstance().getInvitationApi().acceptInvitation(segments.get(0), "#" + AccessToken.getCurrentAccessToken().getUserId());
                    }
                }
                break;
        }
    }

}
