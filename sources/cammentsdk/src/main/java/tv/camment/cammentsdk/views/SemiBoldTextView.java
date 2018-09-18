package tv.camment.cammentsdk.views;


import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import tv.camment.cammentsdk.utils.FontUtils;

public final class SemiBoldTextView extends AppCompatTextView {


    public SemiBoldTextView(Context context) {
        super(context);
        init();
    }

    public SemiBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            setTypeface(FontUtils.getInstance().getSemiBoldTypeFace());
        }
    }

}
