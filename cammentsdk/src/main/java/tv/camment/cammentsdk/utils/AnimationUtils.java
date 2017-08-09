package tv.camment.cammentsdk.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import tv.camment.cammentsdk.views.CameraGLView;
import tv.camment.cammentsdk.views.SquareFrameLayout;

import static android.view.View.VISIBLE;

/**
 * Created by petrushka on 09/08/2017.
 */

public class AnimationUtils {

    public static void animateAppearCameraView(final SquareFrameLayout flCamera,
                                               final CameraGLView cameraGLView,
                                               Animator.AnimatorListener animatorListener) {
        flCamera.setPivotX(0);
        flCamera.setPivotY(0);

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
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

    public static void animateDisappearCameraView(final SquareFrameLayout flCamera,
                                                  final CameraGLView cameraGLView,
                                                  Animator.AnimatorListener animatorListener) {
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
}
