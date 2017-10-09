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

    public void connect() {
        super.connect();
    }

    public void disconnect() {
        super.disconnect();
    }

    public void handleInvitationMessage(InvitationMessage message) {
        super.handleInvitationMessage(message);
    }

    public void showInvitationDialog(BaseMessage message) {
        super.showInvitationDialog(message);
    }

}
