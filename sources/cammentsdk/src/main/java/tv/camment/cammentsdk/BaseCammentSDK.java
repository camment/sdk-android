package tv.camment.cammentsdk;


import android.app.Activity;
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

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentAuthIdentityProvider;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.events.ShowMetadataSetEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentPlayerListener;

abstract class BaseCammentSDK extends CammentLifecycle
        implements IdentityChangedListener, CammentAuthListener {

    static CammentSDK INSTANCE;

    volatile WeakReference<Context> applicationContext;

    private CammentAuthIdentityProvider appIdentityProvider;

    private IoTHelper ioTHelper;

    private OnDeeplinkOpenShowListener onDeeplinkOpenShowListener;

    private CammentPlayerListener cammentPlayerListener;

    private CammentAudioListener cammentAudioListener;

    private CammentAudioVolume cammentAudioVolume = CammentAudioVolume.NO_ADJUSTMENT;

    private ShowMetadata showMetadata;

    private boolean syncEnabled = false;

    synchronized void init(Context context, CammentAuthIdentityProvider identityProvider) {
        if (applicationContext == null || applicationContext.get() == null) {
            if (context == null || !(context instanceof Application)) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            applicationContext = new WeakReference<>(context);

            if (identityProvider == null) {
                throw new IllegalArgumentException("Can't init CammentSDK with null AuthIdentityProvider");
            }

            if (identityProvider.getAuthType() == null) {
                throw new IllegalArgumentException("Can't init CammentSDK with unsupported AuthType in  AuthIdentityProvider");
            }

            appIdentityProvider = identityProvider;
            AuthHelper.getInstance().setAuthInfo(appIdentityProvider.getAuthInfo());

            if (AuthHelper.getInstance().isHostAppLoggedIn()) {
                AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();
                ApiManager.getInstance().getAuthApi().logIn();
            }

            if (TextUtils.isEmpty(getApiKey())) {
                throw new IllegalArgumentException("Missing CammentSDK API key");
            }

            CommonUtils.setViewSizes(getApplicationContext());

            AWSManager.getInstance().checkKeyStore();

            ((Application) context).registerActivityLifecycleCallbacks(this);

            DataManager.getInstance().clearDataForLogOut();

            ioTHelper = AWSManager.getInstance().getIoTHelper();

            connectToIoT();

            if (BuildConfig.USE_MIXPANEL) {
                MixpanelHelper.getInstance().getMixpanel();
                MixpanelHelper.getInstance().trackEvent(MixpanelHelper.APP_START);
            }
        }
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    void setShowMetadata(ShowMetadata showMetadata) {
        if (TextUtils.isEmpty(showMetadata.getUuid())) {
            throw new IllegalArgumentException("Show uuid in ShowMetadata can't be null!");
        } else if (TextUtils.isEmpty(showMetadata.getUuid())) {
            throw new IllegalArgumentException("Show name in ShowMetadata can't be null!");
        }

        this.showMetadata = showMetadata;

        EventBus.getDefault().post(new ShowMetadataSetEvent());
    }

    ShowMetadata getShowMetadata() {
        return showMetadata;
    }

    void setOnDeeplinkOpenShowListener(OnDeeplinkOpenShowListener onDeeplinkOpenShowListener) {
        this.onDeeplinkOpenShowListener = onDeeplinkOpenShowListener;
    }

    OnDeeplinkOpenShowListener getOnDeeplinkOpenShowListener() {
        return onDeeplinkOpenShowListener;
    }

    void connectToIoT() {
        if (ioTHelper != null) {
            ioTHelper.connect();
        }
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            GeneralPreferences.getInstance().setCancelledDeeplinkUuid(GeneralPreferences.getInstance().getDeeplinkGroupUuid());
        }

        getAppAuthIdentityProvider().onActivityResult(requestCode, resultCode, data);

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

    void handleDeeplink() {
        CammentSDK.getInstance().showProgressBar();

        String groupUuid = GeneralPreferences.getInstance().getDeeplinkGroupUuid();
        String showUuid = GeneralPreferences.getInstance().getDeeplinkShowUuid();

        if (TextUtils.isEmpty(groupUuid)
                && GeneralPreferences.getInstance().isFirstStartup()) {
            ApiManager.getInstance().getInvitationApi().getDeferredDeepLink(getDeferredDeepLinkCallback());
            GeneralPreferences.getInstance().setFirstStartup();
            return;
        }

        if (!TextUtils.isEmpty(groupUuid)) {
            if (AuthHelper.getInstance().isLoggedIn()
                    && ioTHelper != null) {
                GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
                GeneralPreferences.getInstance().setDeeplinkShowUuid("");

                InvitationMessage invitationMessage = new InvitationMessage();
                invitationMessage.type = MessageType.INVITATION;
                invitationMessage.body = new InvitationMessage.Body();
                invitationMessage.body.groupUuid = groupUuid;
                invitationMessage.body.showUuid = showUuid;
                invitationMessage.body.key = "#" + AuthHelper.getInstance().getUserId();
                ioTHelper.handleInvitationMessage(invitationMessage);

                MixpanelHelper.getInstance().trackEvent(MixpanelHelper.OPEN_DEEPLINK);
            } else if (!AuthHelper.getInstance().isLoggedIn()) {
                AuthHelper.getInstance().checkLogin(PendingActions.Action.HANDLE_DEEPLINK);
            }
        }
    }

    private CammentCallback<Deeplink> getDeferredDeepLinkCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
                CammentSDK.getInstance().hideProgressBar();

                if (!TextUtils.isEmpty(result.getUrl())) {
                    String[] split = result.getUrl().split("/");
                    if (split.length > 3) {
                        GeneralPreferences.getInstance().setDeeplinkGroupUuid(split[split.length - 3]);
                        GeneralPreferences.getInstance().setDeeplinkShowUuid(split[split.length - 1]);

                        handleDeeplink();
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                CammentSDK.getInstance().hideProgressBar();
            }
        };
    }

    @Override
    public void identityChanged(String oldIdentityId, String newIdentityId) {
        LogUtils.debug("Identity", "old: " + oldIdentityId + " new: " + newIdentityId);

        AWSManager.getInstance().getIoTHelper().subscribe();

        if (!TextUtils.isEmpty(newIdentityId)
                && !TextUtils.equals(oldIdentityId, newIdentityId)) {
            Dataset identitySet = AWSManager.getInstance().getCognitoSyncManager().openOrCreateDataset("identitySet");
            if (identitySet != null) {
                identitySet.put(newIdentityId, oldIdentityId);
                identitySet.synchronize(new Dataset.SyncCallback() {
                    @Override
                    public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                        LogUtils.debug("onSuccess", "synchronize");
                        ApiManager.getInstance().getUserApi().sendCongnitoIdChanged();
                    }

                    @Override
                    public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                        LogUtils.debug("onConflict", "synchronize");
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
                        LogUtils.debug("onDatasetDeleted", "synchronize");
                        return false;
                    }

                    @Override
                    public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                        LogUtils.debug("onDatasetsMerged", "synchronize");
                        return false;
                    }

                    @Override
                    public void onFailure(DataStorageException dse) {
                        Log.e("onFailure", "synchronize", dse);
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn(CammentAuthInfo authInfo) {
        ApiManager.clearInstance();

        AuthHelper.getInstance().setAuthInfo(authInfo);

        AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();

        ApiManager.getInstance().getAuthApi().logIn();

        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.FB_SIGNIN);
    }

    @Override
    public void onLoggedOut() {
        ApiManager.clearInstance();

        AuthHelper.getInstance().setAuthInfo(null);

        try {
            AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();

            AWSManager.getInstance().getCognitoCachingCredentialsProvider().clear();
        } catch (ConcurrentModificationException e) {
            LogUtils.debug("onException", "clear ConcurrentModificationException");
        }

        DataManager.getInstance().clearDataForLogOut();

        AWSManager.getInstance().getIoTHelper().subscribe();

        EventBus.getDefault().post(new UserGroupChangeEvent());
    }

    CammentAuthIdentityProvider getAppAuthIdentityProvider() {
        return appIdentityProvider;
    }

    void setCammentAudioVolumeAdjustment(CammentAudioVolume cammentAudioVolume) {
        this.cammentAudioVolume = cammentAudioVolume;
    }

    CammentAudioVolume getCammentAudioVolumeAdjustment() {
        return cammentAudioVolume == null ? CammentAudioVolume.NO_ADJUSTMENT : cammentAudioVolume;
    }

    void setCammentPlayerListener(CammentPlayerListener cammentPlayerListener) {
        this.cammentPlayerListener = cammentPlayerListener;
    }

    CammentPlayerListener getCammentPlayerListener() {
        return cammentPlayerListener;
    }

    void setCammentAudioListener(CammentAudioListener cammentAudioListener) {
        this.cammentAudioListener = cammentAudioListener;
    }

    CammentAudioListener getCammentAudioListener() {
        return cammentAudioListener;
    }

    void onPlaybackPaused(int currentPositionMillis) {
        SyncHelper.getInstance().onPlaybackPaused(currentPositionMillis);
    }

    void onPlaybackStarted(int currentPositionMillis) {
        SyncHelper.getInstance().onPlaybackStarted(currentPositionMillis);
    }

    void onPlaybackPositionChanged(int currentPositionMillis, boolean isPlaying) {
        SyncHelper.getInstance().onPlaybackPositionChanged(currentPositionMillis, isPlaying);
    }

    void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    boolean isSyncEnabled() {
        return syncEnabled;
    }

}