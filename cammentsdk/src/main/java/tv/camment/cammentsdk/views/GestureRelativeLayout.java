package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class GestureRelativeLayout extends RelativeLayout {

    //private VelocityTracker velocityTracker;

    private static final int THRESHOLD = 100;
    //private static final int VELOCITY_THRESHOLD = 1000;

    private float startX;
    private float stopX;
    private float startY;
    private float stopY;

    private ViewGroup parentViewGroup;

    private enum Mode {
        GOING_BACK,
        HIDE,
        SHOW,
        NONE
    }

    private Mode mode = Mode.NONE;

    public GestureRelativeLayout(Context context) {
        super(context);
    }

    public GestureRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GestureRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setParentViewGroup(ViewGroup parentViewGroup) {
        this.parentViewGroup = parentViewGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TOUCH", event.getAction() + "");
//        if (mode == Mode.NONE) {
//            Log.d("TOUCH", "dispatched");

        boolean dispatched = false;

        if (parentViewGroup != null) {
            dispatched = parentViewGroup.dispatchTouchEvent(event);
        }

        checkFor2FingerSwipeBack(event, dispatched);

        return true;
    }

    private void checkFor2FingerSwipeBack(MotionEvent event, boolean dispatched) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mode = Mode.NONE;
//                if (velocityTracker == null) {
//                    velocityTracker = VelocityTracker.obtain();
//                } else {
//                    velocityTracker.clear();
//                }
//                velocityTracker.addMovement(event);
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //velocityTracker.addMovement(event);
                if (event.getPointerCount() == 2) {
                    startX = event.getX();
                    startY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dispatched) {
                    mode = Mode.NONE;
                    Log.d("touch", "reset to none");
                    break;
                }

                //velocityTracker.addMovement(event);
                stopX = event.getX();
                stopY = event.getY();

                Log.d("Y", "start: " + startY);
                Log.d("Y", "stop: " + stopY);

                if (Math.abs(startX - stopX) > THRESHOLD) {
                    if (Math.abs(startY - stopY) < THRESHOLD) {
                        if (startX < stopX) {
                            if (event.getPointerCount() == 1) {
                                Log.d("HIDE", "HIDE");
                                mode = Mode.HIDE;
                            } else if (event.getPointerCount() == 2) {
                                mode = Mode.GOING_BACK;
                            }
                        } else {
                            if (event.getPointerCount() == 1) {
                                mode = Mode.SHOW;
                            } else if (event.getPointerCount() == 2) {
                                mode = Mode.NONE;
                            }
                        }
                    } else {
                        mode = Mode.NONE;
                    }
                }
//                if (event.getPointerCount() == 2) {
//                    if (startX < stopX
//                            && startX - stopX <= THRESHOLD
//                            && startY < stopY
//                            && startY - stopY <= THRESHOLD) {
////                        velocityTracker.computeCurrentVelocity(1000);
////                        if (VelocityTrackerCompat.getXVelocity(velocityTracker, 0) > VELOCITY_THRESHOLD) {
//                            mode = Mode.GOING_BACK;
//                        //}
//                    }
//                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (mode) {
                    case GOING_BACK:
                        Log.d("TOUCH", "GO BACK!");
                        break;
                    case SHOW:
                        Log.d("TOUCH", "SHOW!");
                        break;
                    case HIDE:
                        Log.d("TOUCH", "HIDE!");
                        break;
                }
//                if (mode == Mode.GOING_BACK
//                        && stopX - startX > THRESHOLD) {
//                    Log.d("TOUCH", "GO BACK!");
//                }
                mode = Mode.NONE;
//                if (velocityTracker != null) {
//                    velocityTracker.recycle();
//                    velocityTracker = null;
//                }
                break;
        }
    }


}
