package tv.camment.cammentsdk.aws;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.data.model.CCamment;

public final class S3UploadHelper extends BaseS3UploadHelper {

    S3UploadHelper(ExecutorService executorService, TransferUtility transferUtility) {
        super(executorService, transferUtility);
    }

    public void uploadCammentFile(final CCamment camment) {
        super.uploadCammentFile(camment);
    }

}
