package tv.camment.cammentsdk.aws;

import android.content.res.AssetManager;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;

final class KeystoreHelper extends CammentAsyncClient {

    KeystoreHelper(ExecutorService executorService) {
        super(executorService);
    }

    void checkKeyStore() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Boolean keystorePresent = AWSIotKeystoreHelper
                        .isKeystorePresent(CammentSDK.getInstance().getApplicationContext().getFilesDir().getPath(),
                                AWSConfig.CERT_KEYSTORE_NAME);

                if (!keystorePresent) {
                    AssetManager assetManager = CammentSDK.getInstance().getApplicationContext().getAssets();
                    try {
                        InputStream in = assetManager.open(AWSConfig.CERT_KEYSTORE_NAME);
                        File outFile = new File(CammentSDK.getInstance().getApplicationContext().getFilesDir().getPath(),
                                AWSConfig.CERT_KEYSTORE_NAME);
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int read = in.read(buffer);
                        while (read != -1) {
                            out.write(buffer, 0, read);
                            read = in.read(buffer);
                        }
                        in.close();
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Log.e("KeystoreHelper", "checkKeyStore", e);
                    }
                }

                return new Object();
            }
        }, checkKeyStoreCallback());
    }

    private CammentCallback<Object> checkKeyStoreCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "checkKeyStoreCallback");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "checkKeyStoreCallback", exception);
            }
        };
    }
}
