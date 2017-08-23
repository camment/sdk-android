package tv.camment.cammentsdk.aws;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;

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

}
