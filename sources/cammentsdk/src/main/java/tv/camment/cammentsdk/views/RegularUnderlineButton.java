package tv.camment.cammentsdk.views;


import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import tv.camment.cammentsdk.utils.FontUtils;

public final class RegularUnderlineButton extends AppCompatButton {

    public RegularUnderlineButton(Context context) {
        super(context);
        init();
    }

    public RegularUnderlineButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularUnderlineButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            setTypeface(FontUtils.getInstance().getRegularTypeFace());
            setPaintFlags(getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        }
    }

}
