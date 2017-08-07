package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.camment.clientsdk.model.Camment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.NoSqlHelper;

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
                Log.d("Camment to upload", camment.getUuid() + " - " + camment.getUrl());

                transferObserver = transferUtility.upload(SDKConfig.BUCKET_ID, camment.getUuid(), new File(camment.getUrl()));

                NoSqlHelper.setCammentTransfer(transferObserver.getId(), camment);

                transferObserver.setTransferListener(getTransferListener());

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

    private TransferListener getTransferListener() {
        return new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("onStateChanged", state.name() + " id: " + id);
                if (state == TransferState.COMPLETED) {
                    final Camment camment = NoSqlHelper.getCammentTransfer(id);
                    if (camment != null && !TextUtils.isEmpty(camment.getUuid())) {
                        ApiManager.getInstance().getCammentApi().createUserGroupCamment(camment);
                    }
                } else if (state == TransferState.FAILED) {
                    //retry once
                    //TODO check if sufficient on retry
                    Log.d("onStateChanged", "retry");
                    final Camment camment = NoSqlHelper.getCammentTransfer(id);
                    uploadCammentFile(camment);
                }
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

}
