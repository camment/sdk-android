package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.utils.CommonUtils;

abstract class BaseTooltipView extends RelativeLayout {

    private TextView tvTooltipText;

    Step step;

    BaseTooltipView(Context context) {
        super(context);
    }

    BaseTooltipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    BaseTooltipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseTooltipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    enum Orientation {
        LEFT,
        RIGHT
    }

    void init(View anchor, Orientation orientation, Step step) {
        this.step = step;

        if (orientation == Orientation.LEFT) {
            View.inflate(getContext(), R.layout.cmmsdk_tooltip_left, this);
        } else {
            View.inflate(getContext(), R.layout.cmmsdk_tooltip_right, this);
        }

        LayoutParams params = (LayoutParams) getLayoutParams();
        if (orientation == Orientation.RIGHT) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        }
        int marginTop = 0;

        switch (step) {
            case RECORD:
                marginTop = anchor.getTop() + (anchor.getHeight() / 2) - CommonUtils.dpToPx(getContext(), 20);
                break;
            case INVITE:
                marginTop = anchor.getTop() + (anchor.getHeight() / 2) - CommonUtils.dpToPx(getContext(), 30);
                break;
            case PLAY:
            case DELETE:
                marginTop = anchor.getTop() + (anchor.getRight() / 4) - CommonUtils.dpToPx(getContext(), 20);
                break;
            case HIDE:
            case SHOW:
                marginTop = CommonUtils.getScreenHeight(getContext()) / 2 - CommonUtils.dpToPx(getContext(), 18);
                break;
        }


        int marginRight = 0;
        int marginLeft = 0;
        if (orientation == Orientation.RIGHT) {
            marginRight = CommonUtils.getScreenWidth(getContext()) - anchor.getLeft() + CommonUtils.dpToPx(getContext(), 4);
        } else {
            switch (step) {
                case PLAY:
                case DELETE:
                    marginLeft = (anchor.getRight() / 2) + CommonUtils.dpToPx(getContext(), 4);
                    break;
                case HIDE:
                case SHOW:
                    marginLeft = CommonUtils.dpToPx(getContext(), 4);
                    break;
            }
        }
        params.setMargins(marginLeft, marginTop, marginRight, 0);

        setLayoutParams(params);

        tvTooltipText = (TextView) findViewById(R.id.cmmsdk_tv_tooltip_text);

        showText();

        repeatAnimation();
    }

    private void showText() {
        switch (step) {
            case RECORD:
                tvTooltipText.setText(R.string.cmmsdk_help_tap_and_hold_to_record);
                break;
            case PLAY:
                tvTooltipText.setText(R.string.cmmsdk_help_tap_to_play);
                break;
            case HIDE:
                tvTooltipText.setText(R.string.cmmsdk_help_swipe_left_to_hide_camments);
                break;
            case SHOW:
                tvTooltipText.setText(R.string.cmmsdk_help_swipe_right_to_show_camments);
                break;
            case DELETE:
                tvTooltipText.setText(R.string.cmmsdk_help_tap_and_hold_to_delete);
                break;
            case INVITE:
                tvTooltipText.setText(R.string.cmmsdk_help_swipe_down_to_invite);
                break;
            default:
                break;
        }
    }

    private void repeatAnimation() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                animateTooltip();
                repeatAnimation();
            }
        }, 5000);
    }

    private void animateTooltip() {
        AnimationUtils.animateTooltip(this);
    }

}
