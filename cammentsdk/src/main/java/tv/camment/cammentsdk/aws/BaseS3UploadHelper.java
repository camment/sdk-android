package tv.camment.cammentsdk.aws;

import android.media.MediaPlayer;
import android.net.Uri;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.utils.FileUtils;

abstract class BaseS3UploadHelper extends CammentAsyncClient {

    private final String KEY_FORMAT = "uploads/%s.mp4";
    private final String MIME_TYPE = "video/mp4";

    private TransferUtility transferUtility;

    private TransferObserver transferObserver;

    BaseS3UploadHelper(ExecutorService executorService, TransferUtility transferUtility) {
        super(executorService);
        this.transferUtility = transferUtility;
    }

    private int checkCammentDuration(final CCamment camment) {
        int duration;

        MediaPlayer mp = MediaPlayer.create(CammentSDK.getInstance().getApplicationContext(),
                Uri.parse(camment.getUrl()));
        if (mp != null) {
            duration = mp.getDuration();
            mp.release();
            return duration;
        }

        Log.d("uploadCamment", "video not yet prepared, repeat");
        duration = checkCammentDuration(camment);
        return duration;
    }

    void uploadCammentFile(final CCamment camment) {
        Log.d("uploadCamment", "groupUuid " + camment.getUserGroupUuid());

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                int cammentDuration = checkCammentDuration(camment);
                if (cammentDuration < SDKConfig.CAMMENT_MIN_DURATION) {
                    throw new IllegalArgumentException("video too short (< 1000 ms)");
                }

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

                transferObserver.setTransferListener(getUploadTransferListener());

                return new Object();
            }
        }, uploadCammentFileCallback());
    }

    private CammentCallback<Object> uploadCammentFileCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object object) {
                Log.d("onSuccess", "camment upload started");
            }

            @Override
            public void onException(Exception exception) {
                Log.d("onException", "uploadCammentFile", exception);
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

    void downloadCammentFile(final CCamment camment, final boolean fullDownload) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                transferObserver = transferUtility.download(AWSConfig.getBucketId(),
                        String.format(KEY_FORMAT, camment.getUuid()),
                        FileUtils.getInstance().getUploadCammentFile(camment.getUuid()));

                transferObserver.setTransferListener(getDownloadTransferListener(camment, fullDownload));
                return null;
            }
        }, downloadCammentFileCallback());
    }

    private CammentCallback<Object> downloadCammentFileCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "downloadCammentFile");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "downloadCammentFile", exception);
            }
        };
    }

    private TransferListener getDownloadTransferListener(final CCamment camment, boolean fullDownload) {
        return new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    cleanup(id);
                    Log.d("Transfer", "camment inserted " + camment.getUuid());
                    CammentProvider.insertCamment(camment);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.e("onProgressChanged", bytesCurrent + "/" + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("onError", "download", ex);
            }
        };
    }

    void preCacheFile(final CCamment camment, final boolean fullCache) {
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

                CacheUtil.cache(dataSpec, AWSManager.getInstance().getExoPlayerCache(), dataSource, cachingCounters);

                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "preCacheFile");
                CammentProvider.insertCamment(camment);
            }

            @Override
            public void onException(Exception ex) {
                Log.e("onException", "preCacheFile"); //cache often fails as some camments can be smaller than 100kB
                CammentProvider.insertCamment(camment); //cache failed but display camment
            }
        });
    }

    private com.google.android.exoplayer2.upstream.TransferListener<DataSource> getCacheTransferListener() {
        return new com.google.android.exoplayer2.upstream.TransferListener<DataSource>() {
            @Override
            public void onTransferStart(DataSource dataSource, DataSpec dataSpec) {
                Log.d("exo", "onTransferStart");
            }

            @Override
            public void onBytesTransferred(DataSource dataSource, int i) {
                Log.d("exo", "onBytesTransferred " + i);
            }

            @Override
            public void onTransferEnd(DataSource dataSource) {
                Log.d("exo", "onTransferEnd");
            }
        };
    }

}
