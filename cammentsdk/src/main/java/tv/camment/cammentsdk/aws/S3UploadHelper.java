package tv.camment.cammentsdk.aws;

import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.camment.clientsdk.model.Camment;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;

/**
 * Created by petrushka on 07/08/2017.
 */

public class S3UploadHelper extends CammentAsyncClient {

    private final TransferUtility transferUtility;

    private TransferObserver transferObserver;

    S3UploadHelper(ExecutorService executorService, TransferUtility transferUtility) {
        super(executorService);
        this.transferUtility = transferUtility;
    }

    public void uploadCammentFile(final Camment camment) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                transferObserver = transferUtility.upload(SDKConfig.BUCKET_ID, camment.getUuid(), new File(camment.getUrl()));
                transferObserver.setTransferListener(transferListener);
                return new Object();
            }
        }, uploadCammentFileCallback());
    }

    private CammentCallback<Object> uploadCammentFileCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                Log.d("onSuccess", "uploadCammentFile");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "uploadCammentFile", exception);
            }
        };
    }

    private TransferListener transferListener = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            //TODO check id
            Log.d("onStateChanged", state.name() + " id: " + id);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("onProgressChanged", bytesCurrent + " - " + bytesTotal);
        }

        @Override
        public void onError(int id, Exception ex) {
            Log.e("onError", "transfer", ex);
        }
    };
}
