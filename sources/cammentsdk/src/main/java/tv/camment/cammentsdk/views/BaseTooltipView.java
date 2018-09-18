package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
        RIGHT,
        BOTTOM,
        TOP
    }

    void init(View anchor, Orientation orientation, Step step) {
        this.step = step;

        switch (orientation) {
            case TOP:
                View.inflate(getContext(), R.layout.cmmsdk_tooltip_top, this);
                break;
            case BOTTOM:
                View.inflate(getContext(), R.layout.cmmsdk_tooltip_bottom, this);
                break;
            case RIGHT:
                View.inflate(getContext(), R.layout.cmmsdk_tooltip_right, this);
                break;
            case LEFT:
            default:
                View.inflate(getContext(), R.layout.cmmsdk_tooltip_left, this);
                break;
        }

        tvTooltipText = findViewById(R.id.cmmsdk_tv_tooltip_text);

        showText();

        Rect anchor_rect = new Rect();
        //returns the visible bounds
        anchor.getDrawingRect(anchor_rect);
        // calculates the relative coordinates to the parent
        ((ViewGroup) anchor.getParent()).offsetDescendantRectToMyCoords(anchor, anchor_rect);

        measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int parent_width, parent_height;
        parent_width = ((View) getParent()).getMeasuredWidth();
        parent_height = ((View) getParent()).getMeasuredHeight();

        LayoutParams params = (LayoutParams) getLayoutParams();

        switch (step) {
            case RECORD:
            case LATER:
                params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

                params.setMargins(0, 0, parent_width - anchor_rect.right, parent_height - anchor_rect.top);
                break;
            case PLAY:
            case DELETE:
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

                params.setMargins(anchor_rect.left, anchor_rect.bottom + CommonUtils.dpToPx(getContext(), 8), 0, 0);
                break;
            case SHOW:
            case HIDE:
            case TUTORIAL:
            case INVITE:
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

                params.setMargins(anchor_rect.left, parent_height / 2, 0, 0);
                break;
        }

        if (orientation == Orientation.BOTTOM) {
            View vArrow = findViewById(R.id.cmmsdk_v_arrow);

            int aWidth = vArrow.getMeasuredWidth();

            int marginRight = (anchor_rect.right - anchor_rect.left) / 2 - aWidth / 2;

            LayoutParams layoutParams = (LayoutParams) vArrow.getLayoutParams();
            layoutParams.setMargins(0, 0, marginRight, 0);
        } else if (orientation == Orientation.TOP) {
            View vArrow = findViewById(R.id.cmmsdk_v_arrow);
            int aWidth = vArrow.getMeasuredWidth();

            int marginLeft = anchor_rect.left + (anchor_rect.right - anchor_rect.left) / 2 - aWidth / 2;
            LayoutParams layoutParams = (LayoutParams) vArrow.getLayoutParams();
            layoutParams.setMargins(marginLeft - params.leftMargin, 0, 0, 0);
        }

        setLayoutParams(params);

        repeatAnimation(step);
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
                tvTooltipText.setText(R.string.cmmsdk_help_sidebar_to_invite);
                break;
            case LATER:
                tvTooltipText.setText(R.string.cmmsdk_help_start_making_video_camments);
                break;
            case TUTORIAL:
                tvTooltipText.setText(R.string.cmmsdk_help_continue_tutorial);
                break;
            default:
                break;
        }
    }

    private void repeatAnimation(final Step step) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (step == Step.LATER || step == Step.TUTORIAL) {
                    fadeOutTooltip(step);
                } else {
                    animateTooltip();
                    repeatAnimation(step);
                }
            }
        }, 5000);
    }

    private void animateTooltip() {
        AnimationUtils.animateTooltip(this);
    }

    private void fadeOutTooltip(final Step step) {
        this.animate().alpha(0.0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (getParent() instanceof OnboardingOverlay) {
                    ((OnboardingOverlay) getParent()).hideTooltipIfNeeded(step);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

}
