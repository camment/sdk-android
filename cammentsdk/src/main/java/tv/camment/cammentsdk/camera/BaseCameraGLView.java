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
import tv.camment.cammentsdk.views.OnPreviewStartedListener;
import tv.camment.cammentsdk.views.SquareFrameLayout;

@SuppressWarnings("deprecation")
class BaseCameraGLView extends GLSurfaceView
        implements MediaEncoder.MediaEncoderListener,
        Camera.PreviewCallback {

    private final CameraSurfaceRenderer surfaceRenderer;
    private CameraHandler cameraHandler = null;
    private OnPreviewStartedListener previewStartedListener;

    private boolean hasSurface;

    private int videoWidth = SDKConfig.CAMMENT_SIZE;
    private int videoHeight = SDKConfig.CAMMENT_SIZE;
    private int cameraRotation;

    BaseCameraGLView(Context context) {
        super(context);
        surfaceRenderer = new CameraSurfaceRenderer(this);
        init();
    }

    BaseCameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceRenderer = new CameraSurfaceRenderer(this);
        init();
    }

    void init() {
        setZOrderMediaOverlay(true);
        setEGLContextClientVersion(2); //GLES 2.0
        setRenderer(surfaceRenderer);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasSurface && cameraHandler == null) {
            startPreview();
        }
    }

    @Override
    public void onPause() {
        if (cameraHandler != null) {
            cameraHandler.stopPreview(false);
        }
        super.onPause();
    }

    synchronized void startPreview() {
        Log.d("CAMERA", "startPreview");
        if (cameraHandler == null) {
            final CameraThread cameraThread = new CameraThread(this);
            cameraThread.start();
            cameraHandler = cameraThread.getHandler();
        }
        cameraHandler.startPreview(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("CAMERA", "surfaceDestroyed");

        if (cameraHandler != null) {
            cameraHandler.stopPreview(true);
            cameraHandler = null;
        }

        hasSurface = false;

        if (surfaceRenderer != null) {
            surfaceRenderer.onSurfaceDestroyed();
        }
        super.surfaceDestroyed(holder);
    }

    double getVideoWidth() {
        return videoWidth;
    }

    double getVideoHeight() {
        return videoHeight;
    }

    void setHasSurface(boolean hasSurface) {
        this.hasSurface = hasSurface;
    }

    private void setVideoEncoder(final MediaVideoEncoder encoder) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (surfaceRenderer) {
                    if (encoder != null) {
                        encoder.setEglContext(EGL14.eglGetCurrentContext(), surfaceRenderer.getHTex());
                    }
                    surfaceRenderer.setVideoEncoder(encoder);
                }
            }
        });
    }

    @Override
    public void onPrepared(MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoder) {
            setVideoEncoder((MediaVideoEncoder) encoder);
        }
    }

    @Override
    public void onStopped(MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoder) {
            setVideoEncoder(null);
        }
    }

    void setVideoSize(int width, int height) {
        if ((cameraRotation % 180) == 0) {
            videoWidth = width;
            videoHeight = height;
        } else {
            videoWidth = height;
            videoHeight = width;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                surfaceRenderer.updateViewport();
            }
        });
    }

    SurfaceTexture getSurfaceTexture() {
        return surfaceRenderer != null ? surfaceRenderer.getSurfaceTexture() : null;
    }

    void clearCameraHandler() {
        cameraHandler = null;
    }

    void setCameraRotation(int cameraRotation) {
        this.cameraRotation = cameraRotation;
    }

    public void setPreviewStartedListener(OnPreviewStartedListener previewStartedListener) {
        this.previewStartedListener = previewStartedListener;
    }

    public void clearPreviewStartedListener() {
        previewStartedListener = null;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (getParent() instanceof SquareFrameLayout) {
            if (((SquareFrameLayout) getParent()).getCustomScale() == 1.0f
                    && previewStartedListener != null) {
                previewStartedListener.onPreviewStarted();
            }
        }
    }

}
