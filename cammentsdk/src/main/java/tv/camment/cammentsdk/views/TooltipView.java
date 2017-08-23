package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import tv.camment.cammentsdk.helpers.Step;


public final class TooltipView extends BaseTooltipView {

    public TooltipView(Context context) {
        super(context);
    }

    public TooltipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TooltipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public TooltipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(View anchor, Orientation orientation, Step step) {
        super.init(anchor, orientation, step);
    }

    public Step getStep() {
        return step;
    }

}
