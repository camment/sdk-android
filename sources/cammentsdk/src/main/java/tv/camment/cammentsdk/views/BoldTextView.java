package tv.camment.cammentsdk.views;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import tv.camment.cammentsdk.utils.FontUtils;

public final class BoldTextView extends AppCompatTextView {
    public BoldTextView(Context context) {
        super(context);
        init();
    }

    public BoldTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            setTypeface(FontUtils.getInstance().getBoldTypeFace());
        }
    }

}
