package tv.camment.cammentsdk.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.camera.CameraHandler;
import tv.camment.cammentsdk.camera.CameraThread;
import tv.camment.cammentsdk.camera.MediaEncoder;
import tv.camment.cammentsdk.camera.MediaVideoEncoder;
import tv.camment.cammentsdk.camera.CameraSurfaceRenderer;
import tv.camment.cammentsdk.views.SquareFrameLayout;


public class CameraGLView extends BaseCameraGLView {

    public CameraGLView(Context context) {
        super(context);
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
