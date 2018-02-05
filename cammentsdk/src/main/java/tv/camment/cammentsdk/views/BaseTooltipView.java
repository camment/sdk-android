package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
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

        tvTooltipText = (TextView) findViewById(R.id.cmmsdk_tv_tooltip_text);

        showText();

        int screen_pos[] = new int[2];
        anchor.getLocationOnScreen(screen_pos);

        Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getWidth(), screen_pos[1] + anchor.getHeight());

        measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int contentViewHeight = getMeasuredHeight();

        int position_x, position_y;

        if (orientation == Orientation.BOTTOM) {
            position_x = CommonUtils.getScreenWidth(getContext()) - anchor_rect.right;
            position_y = anchor_rect.top - contentViewHeight - CommonUtils.dpToPx(getContext(), 8);
        } else if (orientation == Orientation.TOP) {
            position_x = anchor_rect.left;
            position_y = anchor_rect.bottom + CommonUtils.dpToPx(getContext(), 8);
        } else if (orientation == Orientation.RIGHT) {
            position_x = CommonUtils.getScreenWidth(getContext()) - anchor_rect.left + CommonUtils.dpToPx(getContext(), 8);
            position_y = (anchor_rect.top + anchor_rect.bottom) / 2 - contentViewHeight / 2;
        } else {
            switch (step) {
                case SHOW:
                case HIDE:
                case INVITE:
                    position_x = anchor_rect.left + CommonUtils.dpToPx(getContext(), 8);
                    position_y = (anchor_rect.top + anchor_rect.bottom) / 2 - contentViewHeight / 2;
                    break;
                case TUTORIAL:
                    position_x = anchor_rect.left + CommonUtils.dpToPx(getContext(), 16);
                    position_y = (anchor_rect.top + anchor_rect.bottom) / 2 - contentViewHeight / 2;
                    break;
                default:
                    position_x = anchor_rect.right;
                    position_y = (anchor_rect.top + anchor_rect.bottom) / 2 - contentViewHeight / 2 + CommonUtils.dpToPx(getContext(), 5);
                    break;
            }
        }

        LayoutParams params = (LayoutParams) getLayoutParams();

        switch (orientation) {
            case BOTTOM:
            case RIGHT:
                params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                params.setMargins(0, position_y, position_x, 0);
                break;
            case TOP:
            case LEFT:
            default:
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                params.setMargins(position_x, position_y, 0, 0);
                break;
        }

        if (orientation == Orientation.BOTTOM) {
            View vArrow = findViewById(R.id.cmmsdk_v_arrow);

            int aWidth = vArrow.getMeasuredWidth();

            int marginRight = (anchor_rect.right - anchor_rect.left) / 2 - aWidth;

            LayoutParams layoutParams = (LayoutParams) vArrow.getLayoutParams();
            layoutParams.setMargins(0, CommonUtils.dpToPx(getContext(), -5), marginRight, 0);
        } else if (orientation == Orientation.TOP) {
            View vArrow = findViewById(R.id.cmmsdk_v_arrow);
            int aWidth = vArrow.getMeasuredWidth();

            int marginLeft = (anchor_rect.right - anchor_rect.left) / 2 - aWidth;
            LayoutParams layoutParams = (LayoutParams) vArrow.getLayoutParams();
            layoutParams.setMargins(marginLeft, 0, 0, 0);

            LayoutParams lp = (LayoutParams) tvTooltipText.getLayoutParams();
            lp.setMargins(0, CommonUtils.dpToPx(getContext(), -5), 0, 0);
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
