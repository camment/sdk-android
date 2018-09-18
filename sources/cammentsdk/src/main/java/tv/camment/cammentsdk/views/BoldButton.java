package tv.camment.cammentsdk.views;


import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import tv.camment.cammentsdk.utils.FontUtils;

public final class BoldButton extends AppCompatButton {

    public BoldButton(Context context) {
        super(context);
        init();
    }

    public BoldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            setTypeface(FontUtils.getInstance().getBoldTypeFace());
        }
    }

}
