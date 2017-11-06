package tv.camment.cammentsdk;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.Show;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;

abstract class BaseCammentSDK extends CammentLifecycle
        implements AccessToken.AccessTokenRefreshCallback,
        IdentityChangedListener {

    static CammentSDK INSTANCE;

    volatile WeakReference<Context> applicationContext;

    private IoTHelper ioTHelper;

    private OnDeeplinkOpenShowListener onDeeplinkOpenShowListener;

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

            DataManager.getInstance().clearDataForUserGroupChange(true);

            ioTHelper = AWSManager.getInstance().getIoTHelper();

            connectToIoT();

            AWSManager.getInstance().getCognitoCachingCredentialsProvider().registerIdentityChangedListener(this);
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

    void setOnDeeplinkOpenShowListener(OnDeeplinkOpenShowListener onDeeplinkOpenShowListener) {
        this.onDeeplinkOpenShowListener = onDeeplinkOpenShowListener;
    }

    OnDeeplinkOpenShowListener getOnDeeplinkOpenShowListener() {
        return onDeeplinkOpenShowListener;
    }

    public void connectToIoT() {
        if (ioTHelper != null) {
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
        String groupUuid = GeneralPreferences.getInstance().getDeeplinkGroupUuid();
        String showUuid = GeneralPreferences.getInstance().getDeeplinkShowUuid();

        if (TextUtils.isEmpty(groupUuid)
                && GeneralPreferences.getInstance().isFirstStartup()) {
            ApiManager.getInstance().getInvitationApi().getDeferredDeepLink(getDeferredDeepLinkCallback());
            GeneralPreferences.getInstance().setFirstStartup();
            return;
        }

         if (!TextUtils.isEmpty(groupUuid)) {
            if (FacebookHelper.getInstance().isLoggedIn()
                    && ioTHelper != null) {
                GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
                GeneralPreferences.getInstance().setDeeplinkShowUuid("");

                InvitationMessage invitationMessage = new InvitationMessage();
                invitationMessage.type = MessageType.INVITATION;
                invitationMessage.body = new InvitationMessage.Body();
                invitationMessage.body.groupUuid = groupUuid;
                invitationMessage.body.showUuid = showUuid;
                invitationMessage.body.key = "#" + AccessToken.getCurrentAccessToken().getUserId();
                ioTHelper.handleInvitationMessage(invitationMessage);
            } else if (!FacebookHelper.getInstance().isLoggedIn()) {
                CammentSDK.getInstance().checkLogin();
            }
        }
    }

    private CammentCallback<Deeplink> getDeferredDeepLinkCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
                if (!TextUtils.isEmpty(result.getUrl())) {
                    String[] split = result.getUrl().split("/");
                    if (split.length > 3) {
                        InvitationMessage invitationMessage = new InvitationMessage();
                        invitationMessage.type = MessageType.INVITATION;
                        invitationMessage.body = new InvitationMessage.Body();
                        invitationMessage.body.groupUuid = split[split.length - 3];
                        invitationMessage.body.showUuid = split[split.length - 1];
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

        ApiManager.getInstance().getUserApi().getMyUserGroups();
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

                ApiManager.getInstance().getUserApi().getMyUserGroups();
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

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        if (!TextUtils.isEmpty(oldIdentityId)
                && !TextUtils.isEmpty(newIdentityId)
                && !TextUtils.equals(oldIdentityId, newIdentityId)) {
            Log.d("synchronize", "OLD identity: " + oldIdentityId);
            Dataset identitySet = AWSManager.getInstance().getCognitoSyncManager().openOrCreateDataset("identitySet");
            if (identitySet != null) {
                identitySet.put(newIdentityId, oldIdentityId);
                identitySet.synchronize(new Dataset.SyncCallback() {
                    @Override
                    public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                        Log.d("synchronize", "onSuccess");
                        ApiManager.getInstance().getUserApi().sendCongnitoIdChanged();
                    }

                    @Override
                    public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                        Log.d("synchronize", "onConflict");
                        List<Record> records = new ArrayList<>();
                        if (conflicts != null
                                && conflicts.size() > 0) {
                            for (SyncConflict conflict : conflicts) {
                                records.add(conflict.resolveWithLocalRecord());
                            }
                            dataset.resolve(records);
                        }
                        return true;
                    }

                    @Override
                    public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                        Log.d("synchronize", "onDatasetDeleted");
                        return false;
                    }

                    @Override
                    public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                        Log.d("synchronize", "onDatasetMerged");
                        return false;
                    }

                    @Override
                    public void onFailure(DataStorageException dse) {
                        Log.e("synchronize", "onFailure", dse);
                    }
                });
            }
        }
    }

}