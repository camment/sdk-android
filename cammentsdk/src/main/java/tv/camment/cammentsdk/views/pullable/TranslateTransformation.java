package tv.camment.cammentsdk.views.pullable;


import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public final class TranslateTransformation implements Transformation {

    private float startTranslationY = -1;

    @Override
    public void transform(View view, int moveY, AnchorOffset anchorOffset) {
        if (startTranslationY == -1) {
            startTranslationY = view.getTranslationY();
        }

        view.setTranslationY(startTranslationY + moveY);
    }

    @Override
    public ObjectAnimator getAnimator(View view, AnchorOffset anchorOffset, float toProgress, long duration) {
        if (startTranslationY == -1) {
            startTranslationY = view.getTranslationY();
        }

        final boolean isUp = view.getTranslationY() < startTranslationY;
        final float targetTranslation = startTranslationY - toProgress * (startTranslationY - (isUp ? anchorOffset.getUp() : anchorOffset.getDown()));

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), targetTranslation);
        objectAnimator.setDuration(duration).setInterpolator(new LinearInterpolator());
        return objectAnimator;
    }

}
