package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.ChatItem;

abstract class BaseAdDetailView extends RelativeLayout {

    private ChatItem<AdMessage> adMessage;

    private ImageView ivAdImage;
    private float drawableRatio;

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
        ivAdImage = findViewById(R.id.cmmsdk_iv_ad);

        ImageButton ibClose = findViewById(R.id.cmmsdk_ib_close);
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


    void setData(ChatItem<AdMessage> adMessage) {
        if (adMessage == null || adMessage.getContent() == null)
            return;

        this.adMessage = adMessage;

        setVisibility(VISIBLE);

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(adMessage.getContent().body.file)
                .into(ivAdImage);
    }

    ChatItem<AdMessage> getData() {
        return adMessage;
    }

    private void handleAdClose() {
        setVisibility(GONE);
    }

    private void handleOnAdClick() {
        setVisibility(GONE);

        if (adMessage != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adMessage.getContent().body.url));
            getContext().startActivity(intent);
        }
    }

}
