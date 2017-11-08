package tv.camment.cammentsdk.views.pullable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


public final class PullableView extends FrameLayout {

    private final int slope = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private AnchorOffset anchorOffset;
    private ScrollThreshold scrollThreshold;
    private Direction direction;
    private List<BoundView> boundViews;
    private PullListener listener;
    private int downX;
    private int downY;
    private boolean animationRunning;
    private boolean snapped;


    public PullableView(Context context) {
        super(context);
        init();
    }

    public PullableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        direction = Direction.DOWN; //TODO

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                anchorOffset = new AnchorOffset(-v.getTop(), displayMetrics.heightPixels / 3); //TODO
                scrollThreshold = new ScrollThreshold(anchorOffset.getUp(), anchorOffset.getDown()); //TODO
            }
        });
    }

    public void addBoundView(BoundView boundView) {
        if (boundViews == null) {
            boundViews = new ArrayList<>();
        }

        boundViews.add(boundView);
    }

    public void setListener(PullListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean consumed  = false;

        if (isPullable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    consumed = checkMoveInterception(event);
                    if (consumed
                            && listener != null) {
                        listener.onPullStart();
                    }
                    break;
            }
        }

        return consumed;
    }

    private boolean isPullable() {
        return !animationRunning && !snapped;
    }

    private void onDown(MotionEvent event) {
        downX = (int) event.getRawX();
        downY = (int) event.getRawY();
    }

    private boolean checkMoveInterception(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        int moveX = Math.abs(x - downX);
        int moveY = y - downY;

        return event.getPointerCount() == 1
                && isOverSlope(moveY, slope, direction)
                && isVerticalMovement(moveX, moveY);
    }

    private boolean isVerticalMovement(int xMove, int yMove) {
        return (Math.abs(yMove) / (xMove == 0 ? 1 : xMove)) > 1.73f;
    }

    private boolean isOverSlope(int yMove, int ySlope, Direction direction) {
        return (direction.downEnabled() && yMove > ySlope)
                || (direction.upEnabled() && yMove < -ySlope);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPullable()) {
            int currentMoveY = (int) event.getRawY() - downY;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (checkMove(currentMoveY, direction, anchorOffset)) {
                        for (BoundView boundView : boundViews) {
                            boundView.transform(currentMoveY, anchorOffset);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isOverThreshold(currentMoveY, direction, scrollThreshold)) {
                        anchor();
                    }
                    resetAnimated();
                    break;
            }
            return true;
        }

        return false;
    }

    private boolean checkMove(int moveY, Direction direction, AnchorOffset anchorOffset) {
        return (direction.upEnabled() && moveY < 0 && moveY >= anchorOffset.getUp())
                || (direction.downEnabled() && moveY > 0 && moveY <= anchorOffset.getDown());
    }

    private boolean isOverThreshold(int moveY, Direction direction, ScrollThreshold scrollThreshold) {
        return (direction.upEnabled() && moveY < scrollThreshold.getUp())
                || (direction.downEnabled() && moveY > scrollThreshold.getDown());
    }

    private void resetAnimated() {
        if (!animationRunning) {
            animationRunning = true;

            List<Animator> animators = new ArrayList<>();

            for (BoundView boundView : boundViews) {
                boundView.getView().setClickable(false);
                animators.addAll(boundView.getAnimators(anchorOffset, 0.0f));
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    for (BoundView boundView : boundViews) {
                        boundView.getView().setClickable(true);
                    }
                    animationRunning = false;
                    snapped = false;
                    if (listener != null) {
                        listener.onReset();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.start();
        }
    }

    private void resetImmediate() {
        if (!animationRunning) {
            for (BoundView boundView : boundViews) {
                boundView.transform(0, anchorOffset);
            }
            snapped = false;
            if (listener != null) {
                listener.onReset();
            }
        }
    }

    private void anchor() {
//        if (!animationRunning) {
//            animationRunning = true;
//            List<Animator> animators = new ArrayList<>();
//
//            for (BoundView boundView : boundViews) {
//                boundView.getView().setClickable(false);
//                animators.addAll(boundView.getAnimators(anchorOffset, 1.0f));
//            }
//
//            AnimatorSet animatorSet = new AnimatorSet();
//            animatorSet.playTogether(animators);
//            animatorSet.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    animationRunning = false;
//                    snapped = true;
//                    if (listener != null) {
//                        listener.onAnchor();
//                    }
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animator) {
//
//                }
//            });
//            animatorSet.start();
//        }
        if (listener != null) {
            listener.onAnchor();
        }
    }

    public interface PullListener {

        void onReset();

        void onPullStart();

        void onAnchor();

    }

}
