package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.R;


public class CammentOverlay extends RelativeLayout implements CammentsAdapter.ActionListener {

    private static final int THRESHOLD = 100;

    private float startX;
    private float stopX;
    private float startY;
    private float stopY;

    private ViewGroup parentViewGroup;

    private CameraGLView cameraGLView;
    private RecyclerView rvCamments;
    private ImageButton ibRecord;

    private CammentsAdapter adapter;

    private enum Mode {
        GOING_BACK,
        HIDE,
        SHOW,
        NONE
    }

    private Mode mode = Mode.NONE;

    public CammentOverlay(Context context) {
        super(context);
        inflateView(context);
    }

    public CammentOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    public CammentOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public CammentOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }

    public void inflateView(Context context) {
        View.inflate(context, R.layout.cmmsdk_gesture_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        cameraGLView = findViewById(R.id.camera_view);
        rvCamments = findViewById(R.id.rv_camments);
        ibRecord = findViewById(R.id.ib_record);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);
        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(Camment camment) {

    }

    public void setParentViewGroup(ViewGroup parentViewGroup) {
        this.parentViewGroup = parentViewGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    startX = event.getX();
                    startY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dispatched) {
                    mode = Mode.NONE;
                    break;
                }

                stopX = event.getX();
                stopY = event.getY();

                if (Math.abs(startX - stopX) > THRESHOLD) {
                    if (Math.abs(startY - stopY) < THRESHOLD) {
                        if (startX < stopX) {
                            if (event.getPointerCount() == 1) {
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
                mode = Mode.NONE;
                break;
        }
    }


}
