package tv.camment.cammentsdk.helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.CommonUtils;

public final class MultiImageHelper extends CammentAsyncClient {

    private static final int AVATAR_SIZE = CommonUtils.dpToPx(CammentSDK.getInstance().getApplicationContext(), 40);

    private Bitmap[] bitmaps;

    private static final int halfDivider = Math.round(CommonUtils.dpToPx(CammentSDK.getInstance().getApplicationContext(), 1) / 2f);

    public MultiImageHelper(ExecutorService executorService, int numOfUsers) {
        super(executorService);
        bitmaps = new Bitmap[numOfUsers];
    }

    public void addBitmap(Bitmap bitmap, int position, CammentCallback<Bitmap> addBitmapCallback) {
        if (bitmap == null)
            return;

        bitmaps[position] = bitmap;

        combineBitmaps(addBitmapCallback);
    }

    private void combineBitmaps(CammentCallback<Bitmap> addBitmapCallback) {
        submitTask(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                Bitmap bitmap = null;

                if (bitmaps.length > 0) {
                    MultiDrawable multiDrawable = new MultiDrawable(Arrays.asList(bitmaps));

                    List<PhotoItem> items = multiDrawable.items;

                    bitmap = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);

                    Paint bgPaint = new Paint();
                    bgPaint.setColor(Color.WHITE);
                    bgPaint.setStyle(Paint.Style.FILL);
                    canvas.drawPaint(bgPaint);

                    for (PhotoItem item : items) {
                        if (item.bitmap != null) {
                            canvas.drawBitmap(item.bitmap, null, item.position, multiDrawable.paint);
                        }
                    }
                }
                return bitmap;
            }
        }, addBitmapCallback);
    }

    private class MultiDrawable {
        private final List<Bitmap> bitmaps;
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private List<PhotoItem> items = new ArrayList<>();



        MultiDrawable(List<Bitmap> bitmaps) {
            this.bitmaps = new ArrayList<>();

            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null) {
                    this.bitmaps.add(bitmap);
                }
            }
            init();
        }

        private void init() {
            items.clear();

            if (bitmaps.size() == 0)
                return;

            if (bitmaps.size() == 1) {
                Bitmap bitmap = scaleCenterCrop(bitmaps.get(0), MultiImageHelper.AVATAR_SIZE, AVATAR_SIZE);
                items.add(new PhotoItem(bitmap, new Rect(0, 0, AVATAR_SIZE, AVATAR_SIZE)));
            } else if (bitmaps.size() == 2) {
                Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), AVATAR_SIZE, AVATAR_SIZE / 2);
                Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), AVATAR_SIZE, AVATAR_SIZE / 2);
                items.add(new PhotoItem(bitmap1, new Rect(0, 0, AVATAR_SIZE / 2 - halfDivider, AVATAR_SIZE)));
                items.add(new PhotoItem(bitmap2, new Rect(AVATAR_SIZE / 2 + halfDivider, 0, AVATAR_SIZE, AVATAR_SIZE)));
            } else if (bitmaps.size() == 3) {
                Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), AVATAR_SIZE, AVATAR_SIZE / 2);
                Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                items.add(new PhotoItem(bitmap1, new Rect(0, 0, AVATAR_SIZE / 2 - halfDivider, AVATAR_SIZE)));
                items.add(new PhotoItem(bitmap2, new Rect(AVATAR_SIZE / 2 + halfDivider, 0, AVATAR_SIZE, AVATAR_SIZE / 2 - halfDivider)));
                items.add(new PhotoItem(bitmap3, new Rect(AVATAR_SIZE / 2 + halfDivider, AVATAR_SIZE / 2 + halfDivider, AVATAR_SIZE, AVATAR_SIZE)));
            } else {
                Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                Bitmap bitmap4 = scaleCenterCrop(bitmaps.get(3), AVATAR_SIZE / 2, AVATAR_SIZE / 2);
                items.add(new PhotoItem(bitmap1, new Rect(0, 0, AVATAR_SIZE / 2 - halfDivider, AVATAR_SIZE / 2 - halfDivider)));
                items.add(new PhotoItem(bitmap2, new Rect(0, AVATAR_SIZE / 2 + halfDivider, AVATAR_SIZE / 2, AVATAR_SIZE - halfDivider)));
                items.add(new PhotoItem(bitmap3, new Rect(AVATAR_SIZE / 2 + halfDivider, 0, AVATAR_SIZE, AVATAR_SIZE / 2 - halfDivider)));
                items.add(new PhotoItem(bitmap4, new Rect(AVATAR_SIZE / 2 + halfDivider, AVATAR_SIZE / 2, AVATAR_SIZE, AVATAR_SIZE + halfDivider)));
            }
        }

        private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
            return ThumbnailUtils.extractThumbnail(source, newWidth, newHeight);
        }
    }

    private class PhotoItem {
        private final Bitmap bitmap;
        private final Rect position;

        private PhotoItem(Bitmap bitmap, Rect position) {
            this.bitmap = bitmap;
            this.position = position;
        }
    }
}
