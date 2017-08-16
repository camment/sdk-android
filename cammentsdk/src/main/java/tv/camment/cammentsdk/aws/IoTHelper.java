package tv.camment.cammentsdk.aws;

import android.app.Activity;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.camment.clientsdk.model.Usergroup;
import com.google.gson.Gson;

import java.security.KeyStore;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.CammentMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.views.CammentDialog;

public class IoTHelper extends CammentAsyncClient
        implements CammentDialog.ActionListener {

    private AWSIotMqttManager mqttManager;
    private final KeyStore clientKeyStore;

    public IoTHelper(ExecutorService executorService,
                     KeyStore clientKeyStore) {
        super(executorService);
        this.clientKeyStore = clientKeyStore;
    }

    public void connect() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                }
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                        //TODO this should be ok just handled here
                        Log.d("iot connect", status.name());
                        if (status == AWSIotMqttClientStatus.Connected) {
                            subscribe();
                        }
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
                Log.d("onSuccess", "connect");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "connect", exception);
            }
        };
    }

    private void subscribe() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                }
                mqttManager.subscribeToTopic(SDKConfig.IOT_TOPIC, AWSIotMqttQos.QOS0, getAWSIotMqttNewMessageCallback());
                return new Object();
            }
        }, subscribeCallback());
    }

    private CammentCallback<Object> subscribeCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                Log.d("onSuccess", "subscribe");
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
                if (SDKConfig.IOT_TOPIC.equals(topic)) {
                    String message = null;

                    try {
                        message = new String(data, "UTF-8");
                        Log.d("iot message", message);
                    } catch (Exception e) {
                        Log.e("IoTHelper", "invalid message format", e);
                    }

                    //TODO check if it's not from me and format is valid
                    if (!TextUtils.isEmpty(message)) {
                        BaseMessage baseMessage = new Gson().fromJson(message, BaseMessage.class);
                        switch (baseMessage.type) {
                            case INVITATION:
                                baseMessage = new Gson().fromJson(message, InvitationMessage.class);
                                handleMessage(baseMessage);
                                break;
                            case NEW_USER_IN_GROUP:
                                baseMessage = new Gson().fromJson(message, NewUserInGroupMessage.class);
                                handleMessage(baseMessage);
                                break;
                            case CAMMENT:
                                baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                handleMessage(baseMessage);
                                break;
                            case CAMMENT_DELETED:
                                baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                handleMessage(baseMessage);
                                break;
                        }
                    }
                }
            }
        };
    }

    public void disconnect() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (mqttManager == null) {
                    mqttManager = AWSManager.getInstance().getAWSIotMqttManager();
                }
                mqttManager.disconnect();
                return new Object();
            }
        }, disconnectCallback());
    }

    private CammentCallback<Object> disconnectCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                Log.d("onSuccess", "disconnect");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "disconnect", exception);
            }
        };
    }

    private void handleMessage(final BaseMessage message) {
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
                        if (isCammentValid((CammentMessage) message)) {
                            handleCammentDeletedMessage((CammentMessage) message);
                        }
                        break;
                }
            }
        });
    }

    private boolean isInvitationValid(InvitationMessage m) {
        //TODO check also group?
        return m.body != null
                && FacebookHelper.getInstance().isMessageForMe(m.body.userFacebookId);
    }

    private boolean isNewUserInGroupValid(NewUserInGroupMessage m) {
        Usergroup usergroup = UserGroupProvider.getUserGroup();

        return usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && m.body != null
                && m.body.user != null
                && !TextUtils.isEmpty(m.body.user.facebookId)
                && usergroup.getUuid().equals(m.body.groupUuid);
    }

    private boolean isCammentValid(CammentMessage m) {
        Usergroup usergroup = UserGroupProvider.getUserGroup();

        return usergroup != null
                && !TextUtils.isEmpty(usergroup.getUuid())
                && m.body != null
                && !TextUtils.isEmpty(m.body.url)
                && !TextUtils.isEmpty(m.body.thumbnail)
                && usergroup.getUuid().equals(m.body.userGroupUuid);
    }

    private void handleInvitationMessage(BaseMessage message) {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof AppCompatActivity) {
            Fragment fragment = activity.getFragmentManager().findFragmentByTag(message.toString());
            if (fragment == null || !fragment.isAdded()) {
                CammentDialog cammentDialog = CammentDialog.createInstance(message);
                if (message instanceof InvitationMessage) {
                    cammentDialog.setInvitationListener(this);
                }
                cammentDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), message.toString());
            }
        }
    }

    private void handleNewCammentMessage(CammentMessage message) {
        CCamment camment = new CCamment();
        camment.setUuid(message.body.uuid);
        camment.setUserGroupUuid(message.body.userGroupUuid);
        camment.setThumbnail(message.body.thumbnail);
        camment.setUrl(message.body.url);
        camment.setUserGroupUuid(message.body.userCognitoIdentityId);
        camment.setTimestamp(System.currentTimeMillis());
        camment.setRecorded(true);
        camment.setTransferId(-1);

        CammentProvider.insertCamment(camment);
    }

    private void handleCammentDeletedMessage(CammentMessage message) {
        CammentProvider.deleteCammentByUuid(message.body.uuid);
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        switch (baseMessage.type) {
            case INVITATION:
                ApiManager.getInstance().getInvitationApi().acceptInvitation((InvitationMessage) baseMessage);
                break;
        }
    }

}
