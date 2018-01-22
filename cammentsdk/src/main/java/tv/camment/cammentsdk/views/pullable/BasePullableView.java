package tv.camment.cammentsdk.views.pullable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.views.CammentDialog;


abstract class BasePullableView extends FrameLayout implements CammentDialog.ActionListener {

    private static final int MOVE_THRESHOLD = 50;

    private final int slope = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private AnchorOffset anchorOffset;
    private ScrollThreshold scrollThreshold;
    private Direction direction;
    private List<BoundView> boundViews;
    private BasePullListener listener;
    private int downX;
    private int downY;
    private boolean animationRunning;
    private boolean snapped;
    private boolean recordingStopCalled;
    private long lastClick;


    BasePullableView(Context context) {
        super(context);
        init();
    }

    BasePullableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    BasePullableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    BasePullableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        direction = Direction.UP;

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                anchorOffset = new AnchorOffset(-displayMetrics.heightPixels / 4, 0);
                scrollThreshold = new ScrollThreshold(anchorOffset.getUp(), anchorOffset.getDown());
            }
        });
    }

    void addBoundView(BoundView boundView) {
        if (boundViews == null) {
            boundViews = new ArrayList<>();
        }

        boundViews.add(boundView);
    }

    void setListener(BasePullListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean consumed = false;

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
                    recordingStopCalled = false;

                    if (listener != null) {
                        listener.onOnboardingHideMaybeLaterIfNeeded();
                    }

                    if (!OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                        BaseMessage message = new BaseMessage();
                        message.type = MessageType.ONBOARDING;

                        CammentDialog cammentDialog = CammentDialog.createInstance(message);
                        cammentDialog.setActionListener(this);
                        cammentDialog.show(message.toString());
                        return true;
                    }

                    onDown(event);

                    if (Math.abs(SystemClock.uptimeMillis() - lastClick) >= 1000) {
                        lastClick = SystemClock.uptimeMillis();

                        if (PermissionHelper.getInstance().hasPermissions()
                                && boundViews != null
                                && boundViews.size() > 0
                                && boundViews.get(0).getView() instanceof ImageButton) {
                            AnimationUtils.animateActivateRecordingButton((ImageButton) boundViews.get(0).getView());
                        }

                        if (listener != null) {
                            listener.onPress();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!PermissionHelper.getInstance().hasPermissions()
                            || !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                        return true;
                    }

                    if (Math.abs(currentMoveY) > MOVE_THRESHOLD
                            && !recordingStopCalled) {
                        if (listener != null) {
                            listener.onReset(true, true);
                        }
                        recordingStopCalled = true;
                    }

                    if (checkMove(currentMoveY, direction, anchorOffset)) {
                        for (BoundView boundView : boundViews) {
                            boundView.transform(currentMoveY, anchorOffset);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!PermissionHelper.getInstance().hasPermissions()
                            || !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                        return true;
                    }

                    if (boundViews != null
                            && boundViews.size() > 0
                            && boundViews.get(0).getView() instanceof ImageButton) {
                        AnimationUtils.animateDeactivateRecordingButton((ImageButton) boundViews.get(0).getView());
                    }

                    if (isOverThreshold(currentMoveY, direction, scrollThreshold)
                            && event.getAction() == MotionEvent.ACTION_UP) {
                        anchor();
                    }
                    resetAnimated(event.getAction() == MotionEvent.ACTION_CANCEL, !recordingStopCalled);
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

    private void resetAnimated(final boolean cancelled, final boolean callRecordingStop) {
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
                        listener.onReset(cancelled, callRecordingStop);
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

    private void anchor() {
        if (listener != null) {
            listener.onAnchor();
        }
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (listener != null
                && baseMessage.type == MessageType.ONBOARDING) {
            listener.onOnboardingStart();
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {
        if (listener != null
                && baseMessage.type == MessageType.ONBOARDING) {
            listener.onOnboardingMaybeLater();
        }
    }

    public void show() {
        animate().translationX(0).alpha(0.5f).start();
    }

    public void hide() {
        animate().translationX(getWidth() * 2).alpha(0.0f).start();
    }

    interface BasePullListener {

        void onReset(boolean cancelled, boolean callRecordingStop);

        void onPullStart();

        void onAnchor();

        void onPress();

        void onOnboardingStart();

        void onOnboardingMaybeLater();

        void onOnboardingHideMaybeLaterIfNeeded();

    }

}
