package tv.camment.cammentsdk.views;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.utils.CommonUtils;


@SuppressWarnings("deprecation")
abstract class BaseRecordingButton extends SquareImageButton implements CammentDialog.ActionListener {

    private static final int MOVE_THRESHOLD = 10;

    private int initMargin;
    private int prevY;
    private int screenHeight;
    private boolean handledPullDown;

    ActionsListener actionsListener;
    private long lastClick;

    BaseRecordingButton(Context context) {
        super(context);
    }

    BaseRecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    BaseRecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final ConstraintLayout.LayoutParams par = (ConstraintLayout.LayoutParams) getLayoutParams();

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_MOVE:
                if (!PermissionHelper.getInstance().hasPermissions()
                        || !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                    return true;
                }

                if (Math.abs(prevY - event.getRawY()) > MOVE_THRESHOLD) {
                    setAlpha(0.5f);
                    setScaleX(1.0f);
                    setScaleY(1.0f);

                    if (actionsListener != null) {
                        actionsListener.onRecordingStop(true);
                    }
                }

                if (prevY >= screenHeight / 2) {
                    prevY = screenHeight / 2;

                    if (!handledPullDown) {
                        handledPullDown = true;
                        if (actionsListener != null) {
                            actionsListener.onPulledDown();
                        }
                    }
                } else {
                    par.topMargin += (int) event.getRawY() - prevY;
                    prevY = (int) event.getRawY();
                    setLayoutParams(par);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!PermissionHelper.getInstance().hasPermissions()
                        || !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                    return true;
                }

                if (actionsListener != null) {
                    actionsListener
                            .onRecordingStop(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_CANCEL);
                }

                AnimationUtils.animateDeactivateRecordingButton(this);

                par.topMargin = initMargin;

                setLayoutParams(par);
                return true;
            case MotionEvent.ACTION_DOWN:
                handledPullDown = false;

                if (!OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
                    BaseMessage message = new BaseMessage();
                    message.type = MessageType.ONBOARDING;

                    Activity activity = CammentSDK.getInstance().getCurrentActivity();

                    CammentDialog cammentDialog = CammentDialog.createInstance(message);
                    cammentDialog.setActionListener(this);
                    cammentDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), message.toString());
                    return true;
                }

                if (Math.abs(SystemClock.uptimeMillis() - lastClick) >= 1000) {
                    lastClick = SystemClock.uptimeMillis();

                    if (actionsListener != null) {
                        actionsListener.onRecordingStart();
                    }

                    if (PermissionHelper.getInstance().hasPermissions()) {
                        AnimationUtils.animateActivateRecordingButton(this);
                    }

                    screenHeight = CommonUtils.getScreenHeight(getContext());

                    prevY = (int) event.getRawY();
                    initMargin = par.topMargin;
                    par.bottomMargin = -2 * getHeight();

                    setLayoutParams(par);
                }
                return true;
        }
        return false;
    }

    void show() {
        animate().translationX(0).alpha(0.5f).start();
    }

    void hide() {
        animate().translationX(getWidth() * 2).alpha(0.0f).start();
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (actionsListener != null
                && baseMessage.type == MessageType.ONBOARDING) {
            actionsListener.onOnboardingStart();
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

    interface ActionsListener {

        void onPulledDown();

        void onRecordingStart();

        void onRecordingStop(boolean cancelled);

        void onOnboardingStart();

    }

}
