package tv.camment.cammentsdk.views;


import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import tv.camment.cammentsdk.utils.FontUtils;

public final class LightTextView extends AppCompatTextView {


    public LightTextView(Context context) {
        super(context);
        init();
    }

    public LightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            setTypeface(FontUtils.getInstance().getLightTypeFace());
        }
    }

}
