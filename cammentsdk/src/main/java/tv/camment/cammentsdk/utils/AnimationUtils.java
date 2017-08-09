package tv.camment.cammentsdk.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.widget.FrameLayout;

import static android.view.View.VISIBLE;

/**
 * Created by petrushka on 09/08/2017.
 */

public class AnimationUtils {

    public static void animateCameraView(FrameLayout flCamera, Animator.AnimatorListener animatorListener) {
        flCamera.setVisibility(VISIBLE);
        flCamera.setPivotX(0);
        flCamera.setPivotY(0);
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(flCamera, "scaleX", 0.0f, 1.0f);
        ObjectAnimator yAnimator = ObjectAnimator.ofFloat(flCamera, "scaleY", 0.0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(xAnimator, yAnimator);
        set.setDuration(500);
        set.addListener(animatorListener);
        set.start();
    }
}
