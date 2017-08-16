package tv.camment.cammentsdk.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import tv.camment.cammentsdk.views.CameraGLView;
import tv.camment.cammentsdk.views.SquareFrameLayout;

import static android.view.View.VISIBLE;

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
        animator.setDuration(500);
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
            animator.cancel();
        }
    }

    public static void animateDisappearCameraView(final SquareFrameLayout flCamera,
                                                  Animator.AnimatorListener animatorListener) {
        cancelAppearAnimation();

        flCamera.setPivotX(0);
        flCamera.setPivotY(0);

        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.setDuration(500);
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

}
