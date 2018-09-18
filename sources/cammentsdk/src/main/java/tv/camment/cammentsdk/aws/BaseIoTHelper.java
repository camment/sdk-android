package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.Userinfo;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.CammentDeliveredMessage;
import tv.camment.cammentsdk.aws.messages.CammentMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.NeedPlayerStateMessage;
import tv.camment.cammentsdk.aws.messages.NewGroupHostMessage;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;
import tv.camment.cammentsdk.aws.messages.PlayerStateMessage;
import tv.camment.cammentsdk.aws.messages.UserBlockedMessage;
import tv.camment.cammentsdk.aws.messages.UserOfflineMessage;
import tv.camment.cammentsdk.aws.messages.UserOnlineMessage;
import tv.camment.cammentsdk.aws.messages.UserRemovedMessage;
import tv.camment.cammentsdk.aws.messages.UserUnblockedMessage;
import tv.camment.cammentsdk.data.AdvertisementProvider;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.events.IoTStatusChangeEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.helpers.SnackbarType;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.CammentPlayerListener;
import tv.camment.cammentsdk.views.dialogs.BlockedCammentDialog;
import tv.camment.cammentsdk.views.dialogs.InvitationCammentDialog;

abstract class BaseIoTHelper extends CammentAsyncClient {

    private AWSIotMqttManager mqttManager;
    private final KeyStore clientKeyStore;

    BaseIoTHelper(ExecutorService executorService,
                  KeyStore clientKeyStore) {
        super(executorService);
        this.clientKeyStore = clientKeyStore;
    }

    int getConnectionStatus() {
        if (mqttManager != null) {
            try {
                Field field = mqttManager.getClass().getDeclaredField("connectionState");
                field.setAccessible(true);
                Enum enumObj = (Enum) field.get(mqttManager);
                return enumObj.ordinal();
            } catch (Exception e) {
                LogUtils.debug("IOT", "connectionState", e);
            }
        }
        return -1;
    }

    private boolean getMaxAutoReconnectsAlreadyAttempted() {
        if (mqttManager != null) {
            try {
                Field field = mqttManager.getClass().getDeclaredField("autoReconnectsAttempted");
                field.setAccessible(true);
                int autoReconnectsAttempted = (int) field.get(mqttManager);
                return autoReconnectsAttempted == mqttManager.getMaxAutoReconnectAttempts();
            } catch (Exception e) {
                LogUtils.debug("IOT", "connectionState", e);
            }
        }
        return false;
    }

    void connect() {
        int connectionStatus = getConnectionStatus();
        if (connectionStatus == 0 || connectionStatus == 1) { //0 - connecting, 1 - connected
            return;
        }

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                    mqttManager.setMaxAutoReconnectAttepts(20);
                    mqttManager.setReconnectRetryLimits(4, 8);
                    mqttManager.setKeepAlive(5);
                } else {
                    if (getConnectionStatus() == 3 && getMaxAutoReconnectsAlreadyAttempted()) {
                        LogUtils.debug("RECONECT", "resetReconnect!");
                        mqttManager.resetReconnect();
                        mqttManager.disconnect();
                    }
                }

                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                        if (status == AWSIotMqttClientStatus.Connected) {
                            subscribe();
                        }

                        LogUtils.debug("IOT_STATUS", "status: " + status);

                        String identityId = IdentityPreferences.getInstance().getIdentityId();
                        if (!TextUtils.isEmpty(identityId)) {
                            UserInfoProvider.changeUserOnlineOfflineStatus(identityId, status == AWSIotMqttClientStatus.Connected);
                        }

                        EventBus.getDefault().postSticky(new IoTStatusChangeEvent(status));
                    }
                });
                return new Object();
            }
        }, connectCallback());
    }

    private CammentCallback<Object> connectCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {

            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "connect", exception);
            }
        };
    }

    void subscribe() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                }

                if (getConnectionStatus() == 1) {
                    LogUtils.debug("Identity", "subscribe: " + IdentityPreferences.getInstance().getIdentityId());

                    mqttManager.subscribeToTopic(AWSConfig.IOT_USER_TOPIC + IdentityPreferences.getInstance().getIdentityId(), AWSIotMqttQos.QOS1, getAWSIotMqttNewMessageCallback());
                }

                return new Object();
            }
        }, subscribeCallback());
    }

    private CammentCallback<Object> subscribeCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {

            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "subscribe", exception);
            }
        };
    }

    private AWSIotMqttNewMessageCallback getAWSIotMqttNewMessageCallback() {
        return new AWSIotMqttNewMessageCallback() {
            @Override
            public void onMessageArrived(String topic, byte[] data) {
                String identityId = IdentityPreferences.getInstance().getIdentityId();
                String iotUserTopic = AWSConfig.IOT_USER_TOPIC + identityId;

                String message = null;

                try {
                    message = new String(data, "UTF-8");
                    LogUtils.debug("AWS message", message);
                } catch (Exception e) {
                    Log.e("IoTHelper", "invalid message format", e);
                }

                if (AWSConfig.IOT_TOPIC.equals(topic)
                        || iotUserTopic.equals(topic)) {
                    if (!TextUtils.isEmpty(message)) {
                        try {
                            BaseMessage baseMessage = new Gson().fromJson(message, BaseMessage.class);

                            if (baseMessage.type == null)
                                return;

                            switch (baseMessage.type) {
                                case INVITATION:
                                    baseMessage = new Gson().fromJson(message, InvitationMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case NEW_USER_IN_GROUP:
                                    baseMessage = new Gson().fromJson(message, NewUserInGroupMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case CAMMENT:
                                    baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case CAMMENT_DELETED:
                                    baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case USER_REMOVED:
                                    baseMessage = new Gson().fromJson(message, UserRemovedMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case CAMMENT_DELIVERED:
                                    baseMessage = new Gson().fromJson(message, CammentDeliveredMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case AD:
                                    baseMessage = new Gson().fromJson(message, AdMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case USER_BLOCKED:
                                    baseMessage = new Gson().fromJson(message, UserBlockedMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case USER_UNBLOCKED:
                                    baseMessage = new Gson().fromJson(message, UserUnblockedMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case PLAYER_STATE:
                                    baseMessage = new Gson().fromJson(message, PlayerStateMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case NEED_PLAYER_STATE:
                                    baseMessage = new Gson().fromJson(message, NeedPlayerStateMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case NEW_GROUP_HOST:
                                    baseMessage = new Gson().fromJson(message, NewGroupHostMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case USER_ONLINE:
                                    baseMessage = new Gson().fromJson(message, UserOnlineMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                                case USER_OFFLINE:
                                    baseMessage = new Gson().fromJson(message, UserOfflineMessage.class);
                                    handleMessage(baseMessage, identityId);
                                    break;
                            }
                        } catch (Exception e) {
                            LogUtils.debug("onException", "iot parse failed for: " + message, e);
                        }
                    }
                }
            }
        };
    }

    void disconnect() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                }
                mqttManager.disconnect();
                return new Object();
            }
        }, disconnectCallback(false));
    }

    private CammentCallback<Object> disconnectCallback(final boolean reconnect) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                LogUtils.debug("onSuccess", "disconnect");
                if (reconnect) {
                    mqttManager = null;
                    connect();
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "disconnect", exception);
            }
        };
    }

    private void handleMessage(final BaseMessage message, final String identityId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message == null || message.type == null)
                    return;

                switch (message.type) {
                    case INVITATION:
                        if (isInvitationValid((InvitationMessage) message)) {
                            handleInvitationMessage(message);
                        }
                        break;
                    case NEW_USER_IN_GROUP:
                        if (isNewUserInGroupValid((NewUserInGroupMessage) message)) {
                            handleInvitationMessage(message);
                        }
                        break;
                    case CAMMENT:
                        if (isCammentValid((CammentMessage) message)) {
                            handleNewCammentMessage((CammentMessage) message);
                        }
                        break;
                    case CAMMENT_DELETED:
                        if (isCammentDeletedValid((CammentMessage) message)) {
                            handleCammentDeletedMessage((CammentMessage) message);
                        }
                        break;
                    case USER_REMOVED:
                        if (isUserRemovedValid((UserRemovedMessage) message)) {
                            handleUserRemovedMessage((UserRemovedMessage) message, identityId);
                        }
                        break;
                    case CAMMENT_DELIVERED:
                        break;
                    case AD:
                        if (isAdValid((AdMessage) message)) {
                            handleAdMessage((AdMessage) message);
                        }
                        break;
                    case USER_BLOCKED:
                        if (isUserBlockedValid((UserBlockedMessage) message)) {
                            handleUserBlockedMessage((UserBlockedMessage) message, identityId);
                        }
                        break;
                    case USER_UNBLOCKED:
                        if (isUserUnblockedValid((UserUnblockedMessage) message)) {
                            handleUserUnblockedMessage((UserUnblockedMessage) message, identityId);
                        }
                        break;
                    case PLAYER_STATE:
                        if (CammentSDK.getInstance().isSyncEnabled() && isPlayerStateValid((PlayerStateMessage) message)) {
                            handlePlayerStateMessage((PlayerStateMessage) message);
                        }
                        break;
                    case NEED_PLAYER_STATE:
                        if (CammentSDK.getInstance().isSyncEnabled() && isNeedPlayerStateValid((NeedPlayerStateMessage) message)) {
                            handleNeedPlayerStateMessage((NeedPlayerStateMessage) message);
                        }
                        break;
                    case NEW_GROUP_HOST:
                        if (CammentSDK.getInstance().isSyncEnabled() && isNewGroupHostValid((NewGroupHostMessage) message)) {
                            handleNewGroupHostMessage((NewGroupHostMessage) message, identityId);
                        }
                        break;
                    case USER_ONLINE:
                        if (isUserOnlineValid((UserOnlineMessage) message)) {
                            handleUserOnlineMessage((UserOnlineMessage) message);
                        }
                        break;
                    case USER_OFFLINE:
                        if (isUserOfflineValid((UserOfflineMessage) message)) {
                            handleUserOfflineMessage((UserOfflineMessage) message);
                        }
                        break;
                }
            }
        });
    }

    private boolean isMessageForMe(String userFacebookId) {
        return TextUtils.equals(AuthHelper.getInstance().getUserId(), userFacebookId);
    }

    private boolean isInvitationValid(InvitationMessage m) {
        return m.body != null
                && isMessageForMe(m.body.userFacebookId);
    }

    private boolean isNewUserInGroupValid(NewUserInGroupMessage m) {
        return m.body != null
                && m.body.joiningUser != null
                && !TextUtils.isEmpty(m.body.joiningUser.name)
                && !TextUtils.isEmpty(m.body.groupUuid)
                && !TextUtils.isEmpty(m.body.groupOwnerCognitoIdentityId)
                && !TextUtils.isEmpty(m.body.showUuid);
    }

    private boolean isCammentValid(CammentMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && m.body != null
                && !TextUtils.isEmpty(m.body.url)
                && !TextUtils.isEmpty(m.body.thumbnail)
                && TextUtils.equals(usergroup.getUuid(), m.body.userGroupUuid);
    }

    private boolean isCammentDeletedValid(CammentMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && m.body != null
                && TextUtils.equals(usergroup.getUuid(), m.body.userGroupUuid);
    }

    private boolean isUserRemovedValid(UserRemovedMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid)
                && m.body.removedUser != null
                && !TextUtils.isEmpty(m.body.removedUser.userCognitoIdentityId);
    }

    private boolean isAdValid(AdMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.url);
    }

    private boolean isUserBlockedValid(UserBlockedMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid)
                && m.body.blockedUser != null
                && !TextUtils.isEmpty(m.body.blockedUser.userCognitoIdentityId)
                && !TextUtils.isEmpty(m.body.blockedUser.name);
    }

    private boolean isUserUnblockedValid(UserUnblockedMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid)
                && m.body.unblockedUser != null
                && !TextUtils.isEmpty(m.body.unblockedUser.userCognitoIdentityId)
                && !TextUtils.isEmpty(m.body.unblockedUser.name);
    }

    private boolean isPlayerStateValid(PlayerStateMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid);
    }

    private boolean isNeedPlayerStateValid(NeedPlayerStateMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid);
    }

    private boolean isNewGroupHostValid(NewGroupHostMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && TextUtils.equals(usergroup.getUuid(), m.body.groupUuid)
                && !TextUtils.isEmpty(m.body.hostId);
    }

    private boolean isUserOnlineValid(UserOnlineMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.userId);
    }

    private boolean isUserOfflineValid(UserOfflineMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.userId);
    }

    void handleInvitationMessage(BaseMessage message) {
        if (message.type == MessageType.INVITATION
                && message instanceof InvitationMessage) {
            ApiManager.getInstance().getGroupApi().getUserGroupByUuidWithGroupChange(((InvitationMessage) message).body.groupUuid, message, true);
        }

        if (message.type == MessageType.NEW_USER_IN_GROUP
                && message instanceof NewUserInGroupMessage) {
            Userinfo userinfo = new Userinfo();
            userinfo.setName(((NewUserInGroupMessage) message).body.joiningUser.name);
            userinfo.setUserCognitoIdentityId(((NewUserInGroupMessage) message).body.joiningUser.userCognitoIdentityId);
            userinfo.setPicture(((NewUserInGroupMessage) message).body.joiningUser.picture);
            userinfo.setActiveGroup(((NewUserInGroupMessage) message).body.groupUuid);
            userinfo.setIsOnline(true);
            userinfo.setState(UserState.ACTIVE.getStringValue());

            UserInfoProvider.insertUserInfo(userinfo, ((NewUserInGroupMessage) message).body.groupUuid);

            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

            if (activeUserGroup != null
                    && TextUtils.equals(((NewUserInGroupMessage) message).body.groupUuid, activeUserGroup.getUuid())
                    && !TextUtils.equals(((NewUserInGroupMessage) message).body.joiningUser.userCognitoIdentityId, IdentityPreferences.getInstance().getIdentityId())) {
                SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_JOINED_GROUP, SnackbarQueueHelper.SHORT);
                snackbar.setMsgVar(((NewUserInGroupMessage) message).body.joiningUser.name);

                SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
            }
        }
    }

    void showInvitationDialog(BaseMessage message) {
        CammentSDK.getInstance().hideProgressBar();

        if (message instanceof InvitationMessage) {
            InvitationCammentDialog.createInstance(message).show();
        }
    }

    private void handleNewCammentMessage(CammentMessage message) {
        CCamment cammentByUuid = CammentProvider.getCammentByUuid(message.body.uuid);
        if (cammentByUuid != null
                && cammentByUuid.isDeleted()) {
            return;
        }

        CCamment camment = new CCamment();
        camment.setUuid(message.body.uuid);
        camment.setUserGroupUuid(message.body.userGroupUuid);
        camment.setThumbnail(message.body.thumbnail);
        camment.setUrl(message.body.url);
        camment.setUserCognitoIdentityId(message.body.userCognitoIdentityId);
        camment.setRecorded(true);
        camment.setTransferId(-1);
        camment.setPinned(message.body.pinned);
        camment.setShowAt(BigDecimal.valueOf(message.body.showAt));

        if (cammentByUuid != null) {
            camment.setTimestampLong(cammentByUuid.getTimestampLong());
            camment.setBotData(cammentByUuid.getBotData());
            camment.setDeleted(cammentByUuid.isDeleted());
            camment.setStartTimestamp(cammentByUuid.getStartTimestamp());
            camment.setEndTimestamp(cammentByUuid.getEndTimestamp());
            camment.setSeen(cammentByUuid.isSeen());
            camment.setShowUuid(cammentByUuid.getShowUuid());
        } else {
            camment.setTimestampLong(System.currentTimeMillis());
        }

        AWSManager.getInstance().getS3UploadHelper().preCacheFile(camment, true);

        String identityId = IdentityPreferences.getInstance().getIdentityId();
        if (!TextUtils.equals(identityId, message.body.userCognitoIdentityId)) {
            ApiManager.getInstance().getCammentApi().markCammentAsReceived(camment);
        }
    }

    private void handleCammentDeletedMessage(CammentMessage message) {
        CammentProvider.setCammentDeleted(message.body.uuid);
    }

    private void handleUserRemovedMessage(UserRemovedMessage message, String identityId) {
        if (!TextUtils.equals(message.body.removedUser.userCognitoIdentityId, identityId)) {
            //somebody has left the group / active user group is checked during message validation
            String name = message.body.removedUser.name;
            if (TextUtils.isEmpty(name)) {
                name = CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_somebody);
            }

            SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_LEFT_GROUP, SnackbarQueueHelper.SHORT);
            snackbar.setMsgVar(name);

            SnackbarQueueHelper.getInstance().addSnackbar(snackbar);

            UserInfoProvider.deleteUserInfoByIdentityId(message.body.removedUser.userCognitoIdentityId, message.body.groupUuid);
        }
    }

    private void handleAdMessage(AdMessage message) {
        if (BuildConfig.SHOW_ADS) {
            AdvertisementProvider.insertAd(message);
        }
    }

    private void handleUserBlockedMessage(UserBlockedMessage message, String identityId) {
        if (TextUtils.equals(message.body.blockedUser.userCognitoIdentityId, identityId)) {
            //I've been blocked in group / active user group is checked when validating message
            BaseMessage msg = new BaseMessage();
            msg.type = MessageType.BLOCKED;

            BlockedCammentDialog.createInstance(msg).show();

            DataManager.getInstance().clearDataForUserGroupChange();

            EventBus.getDefault().post(new UserGroupChangeEvent());
        } else {
            //somebody has been blocked
            UserInfoProvider.setUserInGroupAsBlocked(message.body.blockedUser.userCognitoIdentityId, message.body.groupUuid);

            SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_BLOCKED, SnackbarQueueHelper.SHORT);
            snackbar.setMsgVar(message.body.blockedUser.name);

            SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
        }
    }

    private void handleUserUnblockedMessage(UserUnblockedMessage message, String identityId) {
        if (!TextUtils.equals(message.body.unblockedUser.userCognitoIdentityId, identityId)) {
            //somebody has been unblocked
            UserInfoProvider.setUserInGroupAsUnblocked(message.body.unblockedUser.userCognitoIdentityId, message.body.groupUuid);

            SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_UNBLOCKED, SnackbarQueueHelper.SHORT);
            snackbar.setMsgVar(message.body.unblockedUser.name);

            SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
        }
    }

    private void handlePlayerStateMessage(PlayerStateMessage message) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        String identityId = IdentityPreferences.getInstance().getIdentityId();

        if (activeUserGroup != null
                && !TextUtils.isEmpty(activeUserGroup.getUuid())
                && !TextUtils.equals(identityId, activeUserGroup.getHostId())) {
            CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();
            if (cammentPlayerListener != null) {
                if (cammentPlayerListener.isPlaying() && !message.body.isPlaying) {
                    SnackbarQueueHelper.getInstance().addSnackbar(new SnackbarQueueHelper.Snackbar(SnackbarType.VIDEO_PAUSED, SnackbarQueueHelper.SHORT));
                }

                cammentPlayerListener
                        .onSyncPosition(message.body.timestamp * 1000, message.body.isPlaying);
            }
        }
    }

    private void handleNeedPlayerStateMessage(NeedPlayerStateMessage message) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        String identityId = IdentityPreferences.getInstance().getIdentityId();

        if (activeUserGroup != null
                && !TextUtils.isEmpty(activeUserGroup.getUuid())
                && TextUtils.equals(identityId, activeUserGroup.getHostId())) {
            CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();
            if (cammentPlayerListener != null) {
                SyncHelper.getInstance().sendPositionUpdate(cammentPlayerListener.getCurrentPosition(), cammentPlayerListener.isPlaying());
            }
        }
    }

    private void handleNewGroupHostMessage(NewGroupHostMessage message, String identityId) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (!TextUtils.equals(activeUserGroup.getHostId(), message.body.hostId)) {
            SnackbarQueueHelper.Snackbar snackbar = null;

            if (TextUtils.equals(identityId, message.body.hostId)) {
                SyncHelper.getInstance().startPeriodicPositionUpdate();

                snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.ME_HOST_NOW, SnackbarQueueHelper.SHORT);
            } else {
                SyncHelper.getInstance().endPeriodicPositionUpdate();

                if (activeUserGroup.getUsers() != null) {
                    for (Userinfo userinfo : activeUserGroup.getUsers()) {
                        if (TextUtils.equals(userinfo.getUserCognitoIdentityId(), activeUserGroup.getHostId())) {
                            if (!TextUtils.isEmpty(userinfo.getName())) {
                                snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_HOST_NOW, SnackbarQueueHelper.SHORT);
                                snackbar.setMsgVar(userinfo.getName());
                            }
                            break;
                        }
                    }
                }
            }

            if (snackbar != null) {
                SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
            }
        }

        UserGroupProvider.setHostId(message.body.groupUuid, message.body.hostId);
    }

    private void handleUserOnlineMessage(UserOnlineMessage message) {
        boolean updated = UserInfoProvider.changeUserOnlineOfflineStatus(message.body.userId, true);

        if (updated) {
            String identityId = IdentityPreferences.getInstance().getIdentityId();
            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

            if (activeUserGroup != null
                    && !TextUtils.isEmpty(activeUserGroup.getUuid())
                    && !TextUtils.equals(identityId, message.body.userId)) {
                CUserInfo userInfo = UserInfoProvider.getUserInfoForGroup(message.body.userId, activeUserGroup.getUuid());

                if (userInfo != null
                        && !TextUtils.isEmpty(userInfo.getName())) {
                    SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_ONLINE, SnackbarQueueHelper.SHORT);
                    snackbar.setMsgVar(userInfo.getName());

                    SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
                }
            }
        }
    }

    private void handleUserOfflineMessage(UserOfflineMessage message) {
        boolean updated = UserInfoProvider.changeUserOnlineOfflineStatus(message.body.userId, false);

        if (updated) {
            String identityId = IdentityPreferences.getInstance().getIdentityId();
            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

            if (activeUserGroup != null
                    && !TextUtils.isEmpty(activeUserGroup.getUuid())
                    && !TextUtils.equals(identityId, message.body.userId)) {
                CUserInfo userInfo = UserInfoProvider.getUserInfoForGroup(message.body.userId, activeUserGroup.getUuid());

                if (userInfo != null
                        && !TextUtils.isEmpty(userInfo.getName())) {
                    SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.USER_OFFLINE, SnackbarQueueHelper.SHORT);
                    snackbar.setMsgVar(userInfo.getName());

                    SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
                }
            }
        }
    }

}
