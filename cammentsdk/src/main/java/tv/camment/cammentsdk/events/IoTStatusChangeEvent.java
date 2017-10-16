package tv.camment.cammentsdk.events;


import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;

public final class IoTStatusChangeEvent {

    private final AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status;

    public IoTStatusChangeEvent(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status) {
        this.status = status;
    }

    public AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus getStatus() {
        return status;
    }

}
