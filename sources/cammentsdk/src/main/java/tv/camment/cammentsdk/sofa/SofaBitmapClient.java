package tv.camment.cammentsdk.sofa;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;

class SofaBitmapClient extends CammentAsyncClient {

    SofaBitmapClient(ExecutorService executorService) {
        super(executorService);
    }

    void processBitmap(final Bitmap resource, final int measuredWidth, final int measuredHeight, final CammentCallback<BitmapResult> processBitmapCallback) {
        submitTask(new Callable<BitmapResult>() {
            @Override
            public BitmapResult call() throws Exception {
                RectF rect = new RectF(994, 1195, 994 + 800, 1195 + 520);
                RectF screenRect = new RectF(0, measuredHeight / 4, measuredWidth, measuredHeight / 4 + measuredHeight / 2);

                Matrix matrix = new Matrix();
                matrix.setRectToRect(rect, screenRect, Matrix.ScaleToFit.CENTER);

                Bitmap result =
                        Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(result);
                canvas.setMatrix(matrix);
                canvas.drawBitmap(resource, 0, 0, new Paint());

                return new BitmapResult(result, matrix);
            }
        }, processBitmapCallback);
    }

    static class BitmapResult {

        final Bitmap result;
        final Matrix matrix;

        BitmapResult(Bitmap result, Matrix matrix) {
            this.result = result;
            this.matrix = matrix;
        }

    }

}
