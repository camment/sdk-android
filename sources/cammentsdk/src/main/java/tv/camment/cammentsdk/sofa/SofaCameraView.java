package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.camera.CameraGLView;
import tv.camment.cammentsdk.events.PermissionStatusChangedEvent;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.utils.CommonUtils;

public class SofaCameraView extends FrameLayout {

    private CameraGLView cameraGLView;

    public SofaCameraView(Context context) {
        this(context, null);
    }

    public SofaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cmmsdk_sofa_camera_view, this);

        PermissionHelper.getInstance().initPermissionHelper(CammentSDK.getInstance().getCurrentActivity());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus.getDefault().register(this);

        handleCameraView();
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);

        if (PermissionHelper.getInstance().hasPermissions()
                && cameraGLView != null) {
            cameraGLView.onPause();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            handleCameraView();
        } else if (visibility == GONE) {
            if (PermissionHelper.getInstance().hasPermissions()
                    && cameraGLView != null) {
                cameraGLView.onPause();
            }
        }
    }

    private void handleCameraView() {
        if (PermissionHelper.getInstance().hasPermissions()) {
            displayCameraPreview();
        } else {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionHelper.getInstance().cameraAndMicTask();
                }
            });
        }
    }

    public void resetCameraPreview() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof CameraGLView) {
                removeViewAt(i);
                break;
            }
        }

        handleCameraView();
    }

    private void displayCameraPreview() {
        boolean cameraAlreadyAdded = false;

        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof CameraGLView) {
                cameraAlreadyAdded = true;
                break;
            }
        }

        if (!cameraAlreadyAdded) {
            cameraGLView = new CameraGLView(getContext(), null);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            final int dp2 = CommonUtils.dpToPx(getContext(), 2);
            params.setMargins(dp2, dp2, dp2, dp2);

            addView(cameraGLView, 2, params);
        } else {
            if (cameraGLView != null) {
                cameraGLView.onResume();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PermissionStatusChangedEvent event) {
        if (PermissionHelper.getInstance().hasPermissions()) {
            displayCameraPreview();
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof CameraGLView) {
                    removeViewAt(i);
                    break;
                }
            }
        }
    }

}
