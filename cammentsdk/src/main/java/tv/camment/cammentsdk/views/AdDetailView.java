package tv.camment.cammentsdk.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.ChatItem;

public final class AdDetailView extends BaseAdDetailView {

    public AdDetailView(Context context) {
        super(context);
    }

    public AdDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public AdDetailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setData(ChatItem<AdMessage> adMessage) {
        super.setData(adMessage);
    }

    public ChatItem<AdMessage> getData() {
        return super.getData();
    }

}
