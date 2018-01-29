package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.camment.cammentsdk.R;

abstract class BaseSkipTutorialView extends RelativeLayout {

    BaseSkipTutorialView(Context context) {
        super(context);
    }

    BaseSkipTutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    BaseSkipTutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseSkipTutorialView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void init() {
        View.inflate(getContext(), R.layout.cmmsdk_tooltip, this);

        TextView tvTooltipText = (TextView) findViewById(R.id.cmmsdk_tv_tooltip_text);

        tvTooltipText.setText(R.string.cmmsdk_help_skip_tutorial);

        LayoutParams params = (LayoutParams) getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        setLayoutParams(params);
    }

}
