package tv.camment.cammentsdk.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageButton;

import tv.camment.cammentsdk.views.SquareFrameLayout;
import tv.camment.cammentsdk.views.TooltipView;

/**
 * Created by petrushka on 09/08/2017.
 */

public class AnimationUtils {

    private static ObjectAnimator recordAnimator;
    private static ValueAnimator animator;

    public static void animateAppearCameraView(final SquareFrameLayout flCamera,
                                               Animator.AnimatorListener animatorListener) {
        flCamera.setPivotX(0);
        flCamera.setPivotY(0);

        animator = ValueAnimator.ofFloat(0.0f, 1.0f);

        animator.setDuration(250);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                flCamera.setCustomScale((Float) valueAnimator.getAnimatedValue());
            }
        });

        animator.addListener(animatorListener);
        animator.start();
    }

    public static void cancelAppearAnimation() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
        }
    }

    public static void animateDisappearCameraView(final SquareFrameLayout flCamera,
                                                  Animator.AnimatorListener animatorListener) {
        cancelAppearAnimation();

        flCamera.setPivotX(0);
        flCamera.setPivotY(0);

        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(250);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                flCamera.setCustomScale((Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.addListener(animatorListener);
        animator.start();
    }

    public static void startRecordAnimation(final View vRecordIndicator) {
        recordAnimator = ObjectAnimator.ofFloat(vRecordIndicator, "alpha", 0.0f, 1.0f);
        recordAnimator.setTarget(vRecordIndicator);
        recordAnimator.setDuration(500);
        recordAnimator.setRepeatMode(ValueAnimator.REVERSE);
        recordAnimator.setRepeatCount(Animation.INFINITE);
        recordAnimator.start();
    }

    public static void stopRecordAnimation(final View vRecordIndicator) {
        if (recordAnimator != null) {
            recordAnimator.cancel();
        }
        vRecordIndicator.setAlpha(0.0f);
    }

    public static void animateActivateRecordingButton(final ImageButton ibRecord) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(ibRecord, "scaleX", 0.8f, 1.5f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(ibRecord, "scaleY", 0.8f, 1.5f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(ibRecord, "alpha", 0.5f, 0.9f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorX, animatorY, animatorAlpha);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    public static void animateDeactivateRecordingButton(final ImageButton ibRecord) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(ibRecord, "scaleX", 1.5f, 0.8f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(ibRecord, "scaleY", 1.5f, 0.8f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(ibRecord, "alpha", 0.9f, 0.5f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorX, animatorY, animatorAlpha);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    public static void animateTooltip(final TooltipView tooltipView) {
        ObjectAnimator objectAnimator =
                ObjectAnimator.ofFloat(tooltipView, "translationY", 0, 0, -30, 0, -15, 0, 0);

        objectAnimator.setDuration(1250);
        objectAnimator.start();
    }

}
