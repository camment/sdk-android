package tv.camment.cammentsdk.views.pullable;


import android.animation.Animator;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public final class BoundView {

    private final View view;
    private List<Transformation> transformations = new ArrayList<>();

    public BoundView(View view, List<Transformation> transformations) {
        this.view = view;
        this.transformations = transformations;
    }


    View getView() {
        return view;
    }

    void transform(int currentMoveY, AnchorOffset anchorOffset) {
        for (Transformation transformation : transformations) {
            transformation.transform(view, currentMoveY, anchorOffset);
        }
    }

    List<Animator> getAnimators(AnchorOffset anchorOffset, float progress) {
        List<Animator> animators = new ArrayList<>();
        for (Transformation transformation : transformations) {
            animators.add(transformation.getAnimator(view, anchorOffset, progress, 300));
        }
        return animators;
    }

}
