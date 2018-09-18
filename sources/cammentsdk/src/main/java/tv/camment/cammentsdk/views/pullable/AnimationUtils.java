package tv.camment.cammentsdk.views.pullable;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

final class AnimationUtils {

    static void animateActivateRecordingButton(final ImageButton ibRecord) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(ibRecord, "scaleX", 1.0f, 1.5f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(ibRecord, "scaleY", 1.0f, 1.5f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(ibRecord, "alpha", 0.5f, 0.9f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorX, animatorY, animatorAlpha);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    static void animateDeactivateRecordingButton(final ImageButton ibRecord) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(ibRecord, "scaleX", 1.5f, 1.0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(ibRecord, "scaleY", 1.5f, 1.0f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(ibRecord, "alpha", 0.9f, 0.5f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorX, animatorY, animatorAlpha);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

}
