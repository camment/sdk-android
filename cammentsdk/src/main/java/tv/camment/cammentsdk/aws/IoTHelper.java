package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.gson.Gson;

import java.security.KeyStore;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.CammentMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;

/**
 * Created by petrushka on 10/08/2017.
 */

public class IoTHelper extends CammentAsyncClient {

    private final AWSIotMqttManager mqttManager;
    private final KeyStore clientKeyStore;
    private final IoTMessageArrivedListener ioTMessageArrivedListener;

    public IoTHelper(ExecutorService executorService,
                     AWSIotMqttManager mqttManager,
                     KeyStore clientKeyStore,
                     IoTMessageArrivedListener ioTMessageArrivedListener) {
        super(executorService);
        this.mqttManager = mqttManager;
        this.clientKeyStore = clientKeyStore;
        this.ioTMessageArrivedListener = ioTMessageArrivedListener;
    }

    public void connect() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                        //TODO this should be ok just handled here
                        Log.d("iot connect", status.name());
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

    public void subscribe() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
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

                    //TODO check if it's not from me
                    if (!TextUtils.isEmpty(message)) {
                        BaseMessage baseMessage = new Gson().fromJson(message, BaseMessage.class);
                        switch (baseMessage.type) {
                            case INVITATION:
                                baseMessage = new Gson().fromJson(message, InvitationMessage.class);
                                sendMessage(baseMessage);
                                break;
                            case NEW_USER_IN_GROUP:
                                baseMessage = new Gson().fromJson(message, NewUserInGroupMessage.class);
                                sendMessage(baseMessage);
                                break;
                            case CAMMENT:
                                baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                sendMessage(baseMessage);
                                break;
                            case CAMMENT_DELETED:
                                baseMessage = new Gson().fromJson(message, CammentMessage.class);
                                sendMessage(baseMessage);
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

    private void sendMessage(final BaseMessage message) {
        if (ioTMessageArrivedListener == null)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (message.type) {
                    case INVITATION:
                        ioTMessageArrivedListener.invitationMessageReceived((InvitationMessage) message);
                        break;
                    case NEW_USER_IN_GROUP:
                        ioTMessageArrivedListener.newUserInGroupMessageReceived((NewUserInGroupMessage) message);
                        break;
                    case CAMMENT:
                        ioTMessageArrivedListener.newCammentMessage((CammentMessage) message);
                        break;
                    case CAMMENT_DELETED:
                        ioTMessageArrivedListener.cammentDeletedMessage((CammentMessage) message);
                        break;
                }
            }
        });
    }

    public interface IoTMessageArrivedListener {

        void invitationMessageReceived(InvitationMessage message);

        void newUserInGroupMessageReceived(NewUserInGroupMessage message);

        void newCammentMessage(CammentMessage message);

        void cammentDeletedMessage(CammentMessage message);

    }

}
