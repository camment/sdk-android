package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;


public final class SkipTutorialView extends BaseSkipTutorialView {

    public SkipTutorialView(Context context) {
        super(context);
    }

    public SkipTutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SkipTutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public SkipTutorialView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init() {
        super.init();
    }

}
