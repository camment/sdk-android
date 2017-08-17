package tv.camment.cammentsdk.views;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.utils.AnimationUtils;
import tv.camment.cammentsdk.utils.CommonUtils;


@SuppressWarnings("deprecation")
public class RecordingButton extends AppCompatImageButton {

    private static final int MOVE_THRESHOLD = 10;

    private int initMargin;
    private int prevY;
    private int screenHeight;
    private boolean handledPullDown;

    private ActionsListener actionsListener;

    public RecordingButton(Context context) {
        super(context);
    }

    public RecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(ActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final ConstraintLayout.LayoutParams par = (ConstraintLayout.LayoutParams) getLayoutParams();

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(prevY - event.getRawY()) > MOVE_THRESHOLD) {
                    Log.d("MOVE", "move threshold reached");
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
                if (actionsListener != null) {
                    actionsListener.onRecordingStart();
                }

                AnimationUtils.animateActivateRecordingButton(this);

                screenHeight = CommonUtils.getScreenHeight(getContext());

                prevY = (int) event.getRawY();
                initMargin = par.topMargin;
                par.bottomMargin = -2 * getHeight();

                setLayoutParams(par);
                return true;
        }
        return false;
    }

    public void show() {
        animate().translationX(0).alpha(1.0f).start();
    }

    public void hide() {
        animate().translationX(getWidth() * 2).alpha(0.0f).start();
    }

    interface ActionsListener {

        void onPulledDown();

        void onRecordingStart();

        void onRecordingStop(boolean cancelled);

    }

}
