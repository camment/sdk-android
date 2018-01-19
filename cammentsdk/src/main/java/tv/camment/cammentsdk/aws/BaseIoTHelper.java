package tv.camment.cammentsdk.aws;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.Userinfo;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.security.KeyStore;
import java.util.List;
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
import tv.camment.cammentsdk.aws.messages.MembershipAcceptedMessage;
import tv.camment.cammentsdk.aws.messages.MembershipRequestMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;
import tv.camment.cammentsdk.aws.messages.UserRemovedMessage;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.events.AdMessageReceivedEvent;
import tv.camment.cammentsdk.events.IoTStatusChangeEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.CammentDialog;

abstract class BaseIoTHelper extends CammentAsyncClient
        implements CammentDialog.ActionListener {

    private AWSIotMqttManager mqttManager;
    private final KeyStore clientKeyStore;

    BaseIoTHelper(ExecutorService executorService,
                  KeyStore clientKeyStore) {
        super(executorService);
        this.clientKeyStore = clientKeyStore;
    }

    void connect() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                    mqttManager.setMaxAutoReconnectAttepts(20);
                    mqttManager.setReconnectRetryLimits(4, 16);
                }

                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                        if (status == AWSIotMqttClientStatus.Connected) {
                            subscribe();
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
                mqttManager.subscribeToTopic(AWSConfig.IOT_TOPIC, AWSIotMqttQos.QOS1, getAWSIotMqttNewMessageCallback());

                mqttManager.subscribeToTopic(AWSConfig.IOT_USER_TOPIC + IdentityPreferences.getInstance().getIdentityId(), AWSIotMqttQos.QOS1, getAWSIotMqttNewMessageCallback());

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
                        BaseMessage baseMessage = new Gson().fromJson(message, BaseMessage.class);
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
                            case MEMBERSHIP_REQUEST:
                                baseMessage = new Gson().fromJson(message, MembershipRequestMessage.class);
                                handleMessage(baseMessage, identityId);
                                break;
                            case MEMBERSHIP_ACCEPTED:
                                baseMessage = new Gson().fromJson(message, MembershipAcceptedMessage.class);
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
                Log.e("onSuccess", "disconnect");
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
                    case MEMBERSHIP_REQUEST:
                        if (isMembershipRequestValid((MembershipRequestMessage) message, identityId)) {
                            handleInvitationMessage(message);
                        }
                        break;
                    case MEMBERSHIP_ACCEPTED:
                        if (isMembershipAcceptedValid((MembershipAcceptedMessage) message)) {
                            handleMembershipAcceptedMessage((MembershipAcceptedMessage) message);
                        }
                        break;
                    case USER_REMOVED:
                        if (isUserRemovedValid((UserRemovedMessage) message)) {
                            handleUserRemovedMessage((UserRemovedMessage) message, identityId);
                        }
                        break;
                    case CAMMENT_DELIVERED:
                        if (isCammentDeliveredValid((CammentDeliveredMessage) message)) {
                            handleCammentDeliveredMessage((CammentDeliveredMessage) message);
                        }
                        break;
                    case AD:
                        if (isAdValid((AdMessage) message)) {
                            handleAdMessage((AdMessage) message);
                        }
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
        return  m.body != null
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
                && usergroup.getUuid().equals(m.body.userGroupUuid);
    }

    private boolean isCammentDeletedValid(CammentMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && m.body != null
                && usergroup.getUuid().equals(m.body.userGroupUuid);
    }

    private boolean isMembershipRequestValid(MembershipRequestMessage m, String identityId) {
        return m.body != null
                && m.body.joiningUser != null
                && !TextUtils.isEmpty(m.body.joiningUser.name)
                && !TextUtils.isEmpty(m.body.joiningUser.userCognitoIdentityId)
                && !TextUtils.equals(m.body.joiningUser.userCognitoIdentityId, identityId);
    }

    private boolean isMembershipAcceptedValid(MembershipAcceptedMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.groupUuid);
    }

    private boolean isUserRemovedValid(UserRemovedMessage m) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        return m.body != null
                && usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && usergroup.getUuid().equals(m.body.groupUuid)
                && !TextUtils.isEmpty(m.body.userCognitoIdentityId);
    }

    private boolean isCammentDeliveredValid(CammentDeliveredMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.uuid);
    }

    private boolean isAdValid(AdMessage m) {
        return m.body != null
                && !TextUtils.isEmpty(m.body.url);
    }

    void handleInvitationMessage(BaseMessage message) {
        if (message.type == MessageType.INVITATION
                && message instanceof InvitationMessage) {
            MixpanelHelper.getInstance().trackEvent(MixpanelHelper.OPEN_DEEPLINK);
            ApiManager.getInstance().getGroupApi().getUserGroupByUuid(((InvitationMessage) message).body.groupUuid, message);
        }
//        } else {
//            showInvitationDialog(message);
//        }

        if (message.type == MessageType.NEW_USER_IN_GROUP
                && message instanceof NewUserInGroupMessage) {
            Userinfo userinfo = new Userinfo();
            userinfo.setName(((NewUserInGroupMessage) message).body.joiningUser.name);
            userinfo.setUserCognitoIdentityId(((NewUserInGroupMessage) message).body.joiningUser.userCognitoIdentityId);
            userinfo.setPicture(((NewUserInGroupMessage) message).body.joiningUser.picture);

            UserInfoProvider.insertUserInfo(userinfo, ((NewUserInGroupMessage) message).body.groupUuid);

            int connectedUsersCountByGroupUuid = UserInfoProvider.getConnectedUsersCountByGroupUuid(((NewUserInGroupMessage) message).body.groupUuid);
            if (connectedUsersCountByGroupUuid == 1
                    && TextUtils.equals(((NewUserInGroupMessage) message).body.groupOwnerCognitoIdentityId, IdentityPreferences.getInstance().getIdentityId())) {
                ApiManager.getInstance().getGroupApi().getUserGroupByUuid(((NewUserInGroupMessage) message).body.groupUuid, message); //this will switch group
            } else if (connectedUsersCountByGroupUuid > 1) {
                Usergroup activeUsegroup = UserGroupProvider.getActiveUserGroup();

                if (activeUsegroup != null
                        && TextUtils.equals(((NewUserInGroupMessage) message).body.groupUuid, activeUsegroup.getUuid()))
                Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                        String.format(CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_user_has_joined_title),
                                ((NewUserInGroupMessage) message).body.joiningUser.name), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void showInvitationDialog(BaseMessage message) {
        CammentSDK.getInstance().hideProgressBar();

        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof AppCompatActivity) {
            dismissInvitationSentIfNeeded(((AppCompatActivity) activity).getSupportFragmentManager().getFragments());

            Fragment fragment = ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag(message.toString());
            if (fragment == null || !fragment.isAdded()) {
                CammentDialog cammentDialog = CammentDialog.createInstance(message);
                if (message instanceof InvitationMessage
                        || message instanceof MembershipRequestMessage) {
                    cammentDialog.setActionListener(this);
                }
                cammentDialog.show(message.toString());
            }
        }
    }

    private void dismissInvitationSentIfNeeded(List<Fragment> fragments) {
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f instanceof CammentDialog
                        && ((CammentDialog) f).getMessageType() == MessageType.INVITATION_SENT) {
                    ((CammentDialog) f).dismiss();
                }
            }
        }
    }

    private void handleNewCammentMessage(CammentMessage message) {
        //FileUtils.getInstance().deleteCammentFile(message.body.name);

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
        camment.setTimestamp(System.currentTimeMillis());
        camment.setRecorded(true);
        camment.setTransferId(-1);

//        if (FileUtils.getInstance().isLocalVideoAvailable(camment.getUuid())) {
//            CammentProvider.insertCamment(camment);
//        } else {
        AWSManager.getInstance().getS3UploadHelper().preCacheFile(camment, true);
        //}

        String identityId = IdentityPreferences.getInstance().getIdentityId();
        if (!TextUtils.equals(identityId, message.body.userCognitoIdentityId)) {
            ApiManager.getInstance().getCammentApi().markCammentAsReceived(camment);
        }
    }

    private void handleCammentDeletedMessage(CammentMessage message) {
        CammentProvider.setCammentDeleted(message.body.uuid);
    }

    private void handleMembershipAcceptedMessage(final MembershipAcceptedMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                        R.string.cmmsdk_joined_private_chat, Toast.LENGTH_LONG).show();
            }
        });

        DataManager.getInstance().clearDataForUserGroupChange();

        Usergroup usergroup = new Usergroup();
        usergroup.setUuid(message.body.groupUuid);

        UserGroupProvider.insertUserGroup(usergroup, true);

        ApiManager.getInstance().getCammentApi().getUserGroupCamments();
        ApiManager.getInstance().getUserApi().getUserInfosForGroupUuid(message.body.groupUuid);
    }

    private void handleUserRemovedMessage(UserRemovedMessage message, String identityId) {
        if (TextUtils.equals(message.body.userCognitoIdentityId, identityId)) {
            //I've been removed from group / active user group is checked when validating message

            BaseMessage msg = new BaseMessage();
            msg.type = MessageType.KICKED_OUT;

            CammentDialog cammentDialog = CammentDialog.createInstance(msg);
            cammentDialog.show(message.toString());

            DataManager.getInstance().clearDataForUserGroupChange();

            EventBus.getDefault().post(new UserGroupChangeEvent());
        } else {
            //somebody has been removed
            UserInfoProvider.deleteUserInfoByIdentityId(message.body.userCognitoIdentityId, message.body.groupUuid);
        }
    }

    private void handleCammentDeliveredMessage(CammentDeliveredMessage message) {
        CammentProvider.setCammentReceived(message.body.uuid);
    }

    private void handleAdMessage(AdMessage message) {
        if (BuildConfig.SHOW_ADS) {
            EventBus.getDefault().post(new AdMessageReceivedEvent(message, System.currentTimeMillis()));
        }
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        switch (baseMessage.type) {
            case INVITATION:
                MixpanelHelper.getInstance().trackEvent(MixpanelHelper.JOIN_GROUP);

                InvitationMessage invitationMessage = (InvitationMessage) baseMessage;

                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                if (activeUserGroup != null) {
                    UserGroupProvider.setActive(activeUserGroup.getUuid(), false);
                }

                UserGroupProvider.setActive(invitationMessage.body.groupUuid, true);

                EventBus.getDefault().post(new UserGroupChangeEvent());

                ApiManager.getInstance().getCammentApi().getUserGroupCamments();

                ApiManager.getInstance().getInvitationApi().sendInvitationForDeeplink(invitationMessage.body.groupUuid, invitationMessage.body.showUuid);
                break;
            case MEMBERSHIP_REQUEST:
                MixpanelHelper.getInstance().trackEvent(MixpanelHelper.ACCEPT_JOIN_REQUEST);

                MembershipRequestMessage membershipRequestMessage = (MembershipRequestMessage) baseMessage;
                ApiManager.getInstance().getInvitationApi().replyToMembershipRequest(
                        membershipRequestMessage.body.joiningUser.userCognitoIdentityId,
                        membershipRequestMessage.body.groupUuid,
                        membershipRequestMessage.body.showUuid,
                        true
                );

                ApiManager.getInstance().getUserApi().getUserInfosForGroupUuid(membershipRequestMessage.body.groupUuid);
                break;
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {
        switch (baseMessage.type) {
            case MEMBERSHIP_REQUEST:
                MixpanelHelper.getInstance().trackEvent(MixpanelHelper.DECLINE_JOIN_REQUEST);

                MembershipRequestMessage membershipRequestMessage = (MembershipRequestMessage) baseMessage;
                ApiManager.getInstance().getInvitationApi().replyToMembershipRequest(
                        membershipRequestMessage.body.joiningUser.userCognitoIdentityId,
                        membershipRequestMessage.body.groupUuid,
                        membershipRequestMessage.body.showUuid,
                        false
                );
                break;
        }
    }
}
