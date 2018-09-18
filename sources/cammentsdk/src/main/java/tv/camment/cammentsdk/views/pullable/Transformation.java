package tv.camment.cammentsdk.views.pullable;


import android.animation.ObjectAnimator;
import android.view.View;

public interface Transformation {

    void transform(View view, int moveY, AnchorOffset anchorOffset);

    ObjectAnimator getAnimator(View view, AnchorOffset anchorOffset, float toProgress, long duration);

}
