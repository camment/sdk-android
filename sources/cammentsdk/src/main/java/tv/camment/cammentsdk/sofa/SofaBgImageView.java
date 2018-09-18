package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.Executors;

import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.LogUtils;

public class SofaBgImageView extends AppCompatImageView {

    public SofaBgImageView(Context context) {
        this(context, null);
    }

    public SofaBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageUrl(String imageUrl) {
        setVisibility(INVISIBLE);

        Glide.with(getContext()).asBitmap().load(imageUrl).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                new SofaBitmapClient(Executors.newSingleThreadExecutor()).processBitmap(resource, getMeasuredWidth(), getMeasuredHeight(), new CammentCallback<SofaBitmapClient.BitmapResult>() {
                    @Override
                    public void onSuccess(SofaBitmapClient.BitmapResult bitmapResult) {
                        setImageBitmap(bitmapResult.result);

                        setVisibility(VISIBLE);
                    }

                    @Override
                    public void onException(Exception exception) {
                        LogUtils.debug("onException", "processSofaBgImage", exception);
                    }
                });
            }
        });
    }

}
