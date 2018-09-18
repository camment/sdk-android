package tv.camment.cammentsdk.aws;

import android.net.Uri;
import android.text.TextUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.StorageClass;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.exoplayer.upstream.DataSource;
import tv.camment.cammentsdk.exoplayer.upstream.DataSpec;
import tv.camment.cammentsdk.exoplayer.upstream.DefaultDataSourceFactory;
import tv.camment.cammentsdk.exoplayer.upstream.cache.CacheUtil;
import tv.camment.cammentsdk.exoplayer.util.Util;
import tv.camment.cammentsdk.utils.ExoCacheUtils;
import tv.camment.cammentsdk.utils.LogUtils;

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
        LogUtils.debug("uploadCamment", "groupUuid " + camment.getUserGroupUuid());

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(MIME_TYPE);
                metadata.setStorageClass(StorageClass.StandardInfrequentAccess);

                transferObserver = transferUtility.upload(AWSConfig.getBucketId(),
                        String.format(KEY_FORMAT, camment.getUuid()),
                        new File(camment.getUrl()),
                        metadata,
                        CannedAccessControlList.PublicRead);

                CammentProvider.setRecorded(camment, true);
                CammentProvider.setCammentUploadTransferId(camment, transferObserver.getId());

                transferObserver.setTransferListener(getUploadTransferListener());

                return new Object();
            }
        }, uploadCammentFileCallback());
    }

    private CammentCallback<Object> uploadCammentFileCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                LogUtils.debug("onSuccess", "camment upload started");
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "uploadCammentFile", exception);
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

    private TransferListener getUploadTransferListener() {
        return new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                LogUtils.debug("onStateChanged", state.name());
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
                LogUtils.debug("onProgressChanged", bytesCurrent + "/" + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                LogUtils.debug("onError", "error", ex);
                if (ex instanceof AmazonClientException) {
                    final CCamment camment = CammentProvider.getCammentByTransferId(id);
                    AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
                }
            }
        };
    }

    void preCacheFile(final CCamment camment, final boolean fullCache) { //TODO move elsewhere
        if (!TextUtils.isEmpty(camment.getUrl())
                && !camment.getUrl().startsWith("http")) {
            CammentProvider.insertCamment(camment); //don't cache as it's local file
            return;
        }

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final DataSpec dataSpec = fullCache ? new DataSpec(Uri.parse(camment.getUrl())) : new DataSpec(Uri.parse(camment.getUrl()), 0, 100 * 1024, null); //download first 100kB
                final DataSource dataSource = new DefaultDataSourceFactory(CammentSDK.getInstance().getApplicationContext(),
                        Util.getUserAgent(CammentSDK.getInstance().getApplicationContext(), "Camment")).createDataSource();
                final CacheUtil.CachingCounters cachingCounters = new CacheUtil.CachingCounters();

                CacheUtil.cache(dataSpec, ExoCacheUtils.getInstance().getCache(), dataSource, cachingCounters);

                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "preCacheFile");
                CammentProvider.insertCamment(camment);
            }

            @Override
            public void onException(Exception ex) {
                LogUtils.debug("onException", "preCacheFile"); //cache often fails as some camments can be smaller than 100kB
                CammentProvider.insertCamment(camment); //cache failed but display camment
            }
        });
    }

    private tv.camment.cammentsdk.exoplayer.upstream.TransferListener<DataSource> getCacheTransferListener() {
        return new tv.camment.cammentsdk.exoplayer.upstream.TransferListener<DataSource>() {
            @Override
            public void onTransferStart(DataSource dataSource, DataSpec dataSpec) {
                LogUtils.debug("exo", "onTransferStart");
            }

            @Override
            public void onBytesTransferred(DataSource dataSource, int i) {
                LogUtils.debug("exo", "onBytesTransferred " + i);
            }

            @Override
            public void onTransferEnd(DataSource dataSource) {
                LogUtils.debug("exo", "onTransferEnd");
            }
        };
    }

}
