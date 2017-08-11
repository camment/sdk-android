package tv.camment.cammentsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.camera.CameraHandler;
import tv.camment.cammentsdk.camera.CameraThread;
import tv.camment.cammentsdk.camera.gl_encoder.MediaEncoder;
import tv.camment.cammentsdk.camera.gl_encoder.MediaVideoEncoder;
import tv.camment.cammentsdk.camera.gl_utils.CameraSurfaceRenderer;


public class CameraGLView extends GLSurfaceView implements MediaEncoder.MediaEncoderListener {

    private final CameraSurfaceRenderer surfaceRenderer;
    private CameraHandler cameraHandler = null;

    private boolean hasSurface;

    private int videoWidth = SDKConfig.CAMMENT_SIZE;
    private int videoHeight = SDKConfig.CAMMENT_SIZE;
    private int cameraRotation;

    public CameraGLView(Context context) {
        super(context);
        surfaceRenderer = new CameraSurfaceRenderer(this);
        init();
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceRenderer = new CameraSurfaceRenderer(this);
        init();
    }

    private void init() {
        setZOrderMediaOverlay(true);
        setEGLContextClientVersion(2); //GLES 2.0
        setRenderer(surfaceRenderer);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasSurface && cameraHandler == null) {
            startPreview(getWidth(), getHeight());
        }
    }

    @Override
    public void onPause() {
        if (cameraHandler != null) {
            cameraHandler.stopPreview(false);
        }
        super.onPause();
    }

    public synchronized void startPreview(int width, int height) {
        Log.d("CAMERA", "startPreview");
        if (cameraHandler == null) {
            final CameraThread cameraThread = new CameraThread(this);
            cameraThread.start();
            cameraHandler = cameraThread.getHandler();
        }
        cameraHandler.startPreview(width, height);
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

    public double getVideoWidth() {
        return videoWidth;
    }

    public double getVideoHeight() {
        return videoHeight;
    }

    public void setHasSurface(boolean hasSurface) {
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

    public void setVideoSize(int width, int height) {
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

    public SurfaceTexture getSurfaceTexture() {
        return surfaceRenderer != null ? surfaceRenderer.getSurfaceTexture() : null;
    }

    public void clearCameraHandler() {
        cameraHandler = null;
    }

    public void setCameraRotation(int cameraRotation) {
        this.cameraRotation = cameraRotation;
    }

}
