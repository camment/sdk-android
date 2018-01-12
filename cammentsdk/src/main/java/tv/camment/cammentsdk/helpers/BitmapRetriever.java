package tv.camment.cammentsdk.helpers;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.FileUtils;

public final class BitmapRetriever extends CammentAsyncClient {

    public BitmapRetriever(ExecutorService executorService) {
        super(executorService);
    }

    public void retrieveLocalThumbnail(final String cammentUuid, final CammentCallback<Bitmap> callback) {
        submitTask(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return ThumbnailUtils.createVideoThumbnail(FileUtils.getInstance()
                        .getUploadCammentPath(cammentUuid), MediaStore.Video.Thumbnails.MINI_KIND);
            }
        }, callback);
    }

}
