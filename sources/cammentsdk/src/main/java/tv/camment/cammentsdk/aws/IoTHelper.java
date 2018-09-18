package tv.camment.cammentsdk.aws;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;

public final class IoTHelper extends BaseIoTHelper {

    IoTHelper(ExecutorService executorService,
              KeyStore clientKeyStore) {
        super(executorService, clientKeyStore);
    }

    @Override
    public int getConnectionStatus() {
        return super.getConnectionStatus();
    }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }

    @Override
    public void subscribe() {
        super.subscribe();
    }

    public void handleInvitationMessage(InvitationMessage message) {
        super.handleInvitationMessage(message);
    }

    @Override
    public void showInvitationDialog(BaseMessage message) {
        super.showInvitationDialog(message);
    }

}
