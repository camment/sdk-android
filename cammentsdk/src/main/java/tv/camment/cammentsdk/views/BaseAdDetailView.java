package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.AdMessage;

abstract class BaseAdDetailView extends RelativeLayout {

    private AdMessage adMessage;

    private ImageView ivAdImage;
    private ImageButton ibClose;

    BaseAdDetailView(Context context) {
        super(context);
        init(context);
    }

    BaseAdDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    BaseAdDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseAdDetailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.cmmsdk_ad_detail, this);
    }

    @Override
    protected void onFinishInflate() {
        ivAdImage = (ImageView) findViewById(R.id.cmmsdk_iv_ad);

        ibClose = (ImageButton) findViewById(R.id.cmmsdk_ib_close);
        ibClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAdClose();
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOnAdClick();
            }
        });

        super.onFinishInflate();
    }


    void setData(AdMessage adMessage) {
        if (adMessage == null)
            return;

        this.adMessage = adMessage;

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(adMessage.body.file)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable drawable, Transition<? super Drawable> transition) {
                        int drawableIntrinsicWidth = drawable.getIntrinsicWidth();
                        int drawableIntrinsicHeight = drawable.getIntrinsicHeight();
                        float drawableRatio = drawableIntrinsicWidth/drawableIntrinsicHeight;

                        ivAdImage.setImageDrawable(drawable);

                        int width = ivAdImage.getWidth();
                        int height = (int) ((1 / drawableRatio) * width);

                        ViewGroup.LayoutParams layoutParams = ivAdImage.getLayoutParams();
                        layoutParams.height = height;

                        ivAdImage.setLayoutParams(layoutParams);
                    }
                });
    }

    private void handleAdClose() {
        setVisibility(GONE);
    }

    private void handleOnAdClick() {
        setVisibility(GONE);

        if (adMessage != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adMessage.body.url));
            getContext().startActivity(intent);
        }
    }

}
