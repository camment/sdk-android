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

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;

abstract class BaseS3UploadHelper extends CammentAsyncClient {

    private final String KEY_FORMAT = "uploads/%s.mp4";
    private final String MIME_TYPE = "video/mp4";

    private TransferUtility transferUtility;

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

                transferObserver = transferUtility.upload(AWSConfig.getBucketId(),
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

            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "uploadCammentFile", exception);
            }
        };
    }

    private void cleanup(int id) {
        transferUtility.deleteTransferRecord(id);
        transferUtility = null;
        transferObserver.cleanTransferListener();
        transferObserver = null;
        executorService.shutdown();
        executorService = null;
    }

    private TransferListener getTransferListener() {
        return new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    cleanup(id);
                    final CCamment camment = CammentProvider.getCammentByTransferId(id);
                    if (camment != null && !TextUtils.isEmpty(camment.getUuid())) {
                        ApiManager.getInstance().getCammentApi().createUserGroupCamment(camment);
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                if (ex instanceof AmazonClientException) {
                    final CCamment camment = CammentProvider.getCammentByTransferId(id);
                    AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
                }
            }
        };
    }

}
