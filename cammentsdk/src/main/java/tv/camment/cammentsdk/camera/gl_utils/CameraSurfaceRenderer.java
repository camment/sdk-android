package tv.camment.cammentsdk.camera.gl_utils;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tv.camment.cammentsdk.camera.gl_encoder.MediaVideoEncoder;
import tv.camment.cammentsdk.views.CameraGLView;


public class CameraSurfaceRenderer implements
        GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    private final WeakReference<CameraGLView> cameraGLViewWeakRef;
    private SurfaceTexture surfaceTexture;

    private MediaVideoEncoder videoEncoder;

    private int hTex;

    private GLDrawer2D drawer2D;

    private final float[] stMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    private volatile boolean requestUpdateTex = false;
    private boolean flip = true;

    public CameraSurfaceRenderer(final CameraGLView cameraGLView) {
        cameraGLViewWeakRef = new WeakReference<>(cameraGLView);
        Matrix.setIdentityM(mvpMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (!extensions.contains("OES_EGL_image_external")) {
            throw new RuntimeException("This system does not support OES_EGL_image_external."); //TODO check
        }

        if (hTex > 0) {
            GLDrawer2D.deleteTex(hTex);
        }

        hTex = GLDrawer2D.initTex();

        //create SurfaceTexture;
        surfaceTexture = new SurfaceTexture(hTex);
        surfaceTexture.setOnFrameAvailableListener(this);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        final CameraGLView cameraGLView = cameraGLViewWeakRef.get();
        if (cameraGLView != null) {
            cameraGLView.setHasSurface(true);
        }

        //create object for preview display
        drawer2D = new GLDrawer2D();
        drawer2D.setMatrix(mvpMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // if at least with or height is zero, initialization of this view is still in progress.
        if (width == 0 || height == 0)
            return;
        updateViewport();
        final CameraGLView cameraGLView = cameraGLViewWeakRef.get();
        if (cameraGLView != null) {
            cameraGLView.startPreview();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestUpdateTex = true;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (drawer2D == null)
            return;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (requestUpdateTex) {
            requestUpdateTex = false;
            // update texture (came from camera)
            surfaceTexture.updateTexImage();
            // get texture matrix
            surfaceTexture.getTransformMatrix(stMatrix);
        }

        // draw to preview screen
        drawer2D.draw(hTex, stMatrix);

        flip = !flip;
        if (flip) {
            synchronized (this) {
                if (videoEncoder != null) {
                    videoEncoder.frameAvailableSoon(stMatrix, mvpMatrix);
                }
            }
        }
    }

    /**
     * when GLSurface context will be destroyed soon
     */
    public void onSurfaceDestroyed() {
        Log.d("CAMERArender", "onSurfaceDestroyed");
        if (drawer2D != null) {
            drawer2D = null;
        }

        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    public void updateViewport() {
        final CameraGLView cameraGLView = cameraGLViewWeakRef.get();
        if (cameraGLView != null) {
            final int viewWidth = cameraGLView.getWidth();
            final int viewHeight = cameraGLView.getHeight();

            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            final double videoWidth = cameraGLView.getVideoWidth();
            final double videoHeight = cameraGLView.getVideoHeight();

            if (videoWidth == 0 || videoHeight == 0)
                return;

            Matrix.setIdentityM(mvpMatrix, 0);

            final double viewAspectRatio = viewWidth / (double) viewHeight;
            Log.d("CameraSurface", String.format("view(%d,%d)%f,video(%1.0f,%1.0f)", viewWidth, viewHeight, viewAspectRatio, videoWidth, videoHeight));

            //square scale
            int viewX = 0;
            int viewY = 0;

            float scaleX = 1;
            float scaleY = 1;

            final int newPreviewSize;

            if (viewWidth >= viewHeight) {
                newPreviewSize = viewHeight;
                viewX = (viewWidth - newPreviewSize) / 2;
            } else {
                newPreviewSize = viewWidth;
                viewY = (viewHeight - newPreviewSize) / 2;
            }

            final float videoAspectRatio = (float) (videoWidth / videoHeight);

            if (videoAspectRatio >= 1) {
                scaleX = videoAspectRatio;
            } else {
                scaleY = 1 / videoAspectRatio;
            }

            Log.d("CameraSurface", "scale square: " + scaleX + " - " + scaleY);

            GLES20.glViewport(viewX, viewY, newPreviewSize, newPreviewSize);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1.0f);

            if (drawer2D != null) {
                drawer2D.setMatrix(mvpMatrix, 0);
            }
        }
    }

    public int getHTex() {
        return hTex;
    }

    public void setVideoEncoder(MediaVideoEncoder videoEncoder) {
        this.videoEncoder = videoEncoder;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

}
