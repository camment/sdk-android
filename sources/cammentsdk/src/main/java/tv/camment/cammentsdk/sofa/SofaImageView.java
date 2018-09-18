package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.Executors;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.LogUtils;

public class SofaImageView extends AppCompatImageView {

    private Listener listener;
    private float[] values = new float[9];

    public SofaImageView(Context context) {
        this(context, null);
    }

    public SofaImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                configureBounds();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            configureBounds();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void configureBounds() {
        Glide.with(getContext()).asBitmap().load(R.drawable.cmmsdk_couchview_couch).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                new SofaBitmapClient(Executors.newSingleThreadExecutor()).processBitmap(resource, getMeasuredWidth(), getMeasuredHeight(), new CammentCallback<SofaBitmapClient.BitmapResult>() {
                    @Override
                    public void onSuccess(SofaBitmapClient.BitmapResult bitmapResult) {
                        RectF rect = new RectF(994, 1195, 994 + 800, 1195 + 520);
                        RectF screenRect = new RectF(0, getMeasuredHeight() / 4, getMeasuredWidth(), getMeasuredHeight() / 4 + getMeasuredHeight() / 2);

                        setImageBitmap(bitmapResult.result);

                        bitmapResult.matrix.getValues(values);

                        float left = screenRect.left;
                        float top = screenRect.top;

                        top += (screenRect.height() - values[0] * rect.height()) / 2;
                        top += 16 * values[0];

                        left += (screenRect.width() - values[0] * rect.width()) / 2;

                        if (listener != null) {
                            listener.onAddHeads(values, left, top);
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        LogUtils.debug("onException", "processSofaImage", exception);
                    }
                });
            }
        });
    }

    public interface Listener {

        void onAddHeads(float[] values, float left, float top);

    }

}
