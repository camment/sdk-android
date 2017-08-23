package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StorageClass;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;

abstract class BaseS3UploadHelper extends CammentAsyncClient {

    private final String KEY_FORMAT = "uploads/%s.mp4";
    private final String MIME_TYPE = "video/mp4";

    private final TransferUtility transferUtility;

    private TransferObserver transferObserver;

    BaseS3UploadHelper(ExecutorService executorService, TransferUtility transferUtility) {
        super(executorService);
        this.transferUtility = transferUtility;
    }

    void uploadCammentFile(final CCamment camment) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(MIME_TYPE);
                metadata.setHeader(Headers.STORAGE_CLASS, StorageClass.StandardInfrequentAccess);

                transferObserver = transferUtility.upload(AWSConfig.BUCKET_ID,
                        String.format(KEY_FORMAT, camment.getUuid()),
                        new File(camment.getUrl()),
                        metadata,
                        CannedAccessControlList.PublicRead);

                CammentProvider.setRecorded(camment, true);
                CammentProvider.setCammentUploadTransferId(camment, transferObserver.getId());

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
                    final CCamment camment = CammentProvider.getCammentByTransferId(id);
                    if (camment != null && !TextUtils.isEmpty(camment.getUuid())) {
                        ApiManager.getInstance().getCammentApi().createUserGroupCamment(camment);
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("onProgressChanged", bytesCurrent + " - " + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("onError", "transfer", ex);
                if (ex instanceof AmazonClientException) {
                    //TODO check also ex.getMessage() More data read (4793) than expected (3225)?
                    Log.d("onError", "retry");
                    final CCamment camment = CammentProvider.getCammentByTransferId(id);
                    uploadCammentFile(camment);
                }
            }
        };
    }

}
