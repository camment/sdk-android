package tv.camment.cammentsdk.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tv.camment.cammentsdk.SDKConfig;

@SuppressWarnings("deprecation")
final class CameraThread extends Thread {

    private final Object readyFence = new Object();
    private final WeakReference<BaseCameraGLView> cameraGlViewWeakRef;
    private CameraHandler cameraHandler;
    private volatile boolean isRunning = false;
    private Camera camera;

    private int cameraId;

    CameraThread(final BaseCameraGLView cameraGLView) {
        super("Camera thread");
        cameraGlViewWeakRef = new WeakReference<>(cameraGLView);
    }

    CameraHandler getHandler() {
        synchronized (readyFence) {
            try {
                readyFence.wait();
            } catch (final InterruptedException e) {
                Log.e("CAMERA", "getHandler", e);
            }
        }
        return cameraHandler;
    }

    /**
     * message loop
     * prepare Looper and create Handler for this thread
     */
    @Override
    public void run() {
        Looper.prepare();
        synchronized (readyFence) {
            cameraHandler = new CameraHandler(this);
            isRunning = true;
            readyFence.notify();
        }
        Looper.loop();

        synchronized (readyFence) {
            cameraHandler = null;
            isRunning = false;
        }
    }

    /**
     * start camera preview
     */
    final void startPreview(Camera.PreviewCallback previewCallback) {
        final BaseCameraGLView cameraGLView = cameraGlViewWeakRef.get();
        if (cameraGLView != null && camera == null) {
            // This is a sample project so just use 0 as camera ID.
            // it is better to selecting camera is available
            try {
                cameraId = getCameraIdForLensPosition(LensPosition.FRONT);
                camera = Camera.open(cameraId);
                final Camera.Parameters params = camera.getParameters();
                final List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
                final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
                params.setPreviewFpsRange(max_fps[0], max_fps[1]);
                params.setRecordingHint(true);

                // request closest supported preview size
                final Camera.Size closestSize = getClosestSupportedSize(
                        params.getSupportedPreviewSizes(), SDKConfig.CAMMENT_SIZE, SDKConfig.CAMMENT_SIZE);
                params.setPreviewSize(closestSize.width, closestSize.height);

                // request closest picture size for an aspect ratio issue on Nexus7
                final Camera.Size pictureSize = getClosestSupportedSize(
                        params.getSupportedPictureSizes(), SDKConfig.CAMMENT_SIZE, SDKConfig.CAMMENT_SIZE);
                params.setPictureSize(pictureSize.width, pictureSize.height);

                // rotate camera preview according to the device orientation
                setRotation();
                camera.setParameters(params);
                camera.setPreviewCallback(previewCallback);
                // get the actual preview size
                final Camera.Size previewSize = camera.getParameters().getPreviewSize();
                // adjust view size with keeping the aspect ration of camera preview.
                // here is not a UI thread and we should request cameraGLView view to execute.
                cameraGLView.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraGLView.setVideoSize(previewSize.width, previewSize.height);
                    }
                });
                final SurfaceTexture st = cameraGLView.getSurfaceTexture();
                st.setDefaultBufferSize(previewSize.width, previewSize.height);
                camera.setPreviewTexture(st);
            } catch (final IOException | RuntimeException e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
            }
            if (camera != null) {
                // start camera preview display
                camera.startPreview();
            }
        }
    }

    private enum LensPosition {

        BACK,

        FRONT,

        EXTERNAL

    }

    private int getCameraIdForLensPosition(LensPosition lensPosition) {
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = getCameraInfo(i);

            if (info.facing == facingForLensPosition(lensPosition)) {
                return i;
            }
        }

        return 0;
    }

    private int facingForLensPosition(LensPosition lensPosition) {
        switch (lensPosition) {
            case FRONT:
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            case BACK:
                return Camera.CameraInfo.CAMERA_FACING_BACK;
            default:
                throw new IllegalArgumentException("Camera is not supported: " + lensPosition);
        }
    }

    private Camera.CameraInfo getCameraInfo(int id) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        return info;
    }

    private static Camera.Size getClosestSupportedSize(List<Camera.Size> supportedSizes, final int requestedWidth, final int requestedHeight) {
        return Collections.min(supportedSizes, new Comparator<Camera.Size>() {

            private int diff(final Camera.Size size) {
                return Math.abs(requestedWidth - size.width) + Math.abs(requestedHeight - size.height);
            }

            @Override
            public int compare(final Camera.Size lhs, final Camera.Size rhs) {
                return diff(lhs) - diff(rhs);
            }
        });

    }

    /**
     * stop camera preview
     */
    void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        final BaseCameraGLView cameraGLView = cameraGlViewWeakRef.get();
        if (cameraGLView != null) {
            cameraGLView.clearCameraHandler();
        }
    }

    /**
     * rotate preview screen according to the device orientation
     */
    private void setRotation() {
        final BaseCameraGLView cameraGLView = cameraGlViewWeakRef.get();
        if (cameraGLView == null)
            return;

        final Display display = ((WindowManager) cameraGLView.getContext()
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // get whether the camera is front camera or back camera
        final Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        boolean isFrontFace = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (isFrontFace) {    // front camera
            degrees = (info.orientation + degrees) % 360;
            degrees = (360 - degrees) % 360;  // reverse
        } else {  // back camera
            degrees = (info.orientation - degrees + 360) % 360;
        }
        // apply rotation setting
        camera.setDisplayOrientation(degrees);
        cameraGLView.setCameraRotation(degrees);
    }

    boolean isRunning() {
        return isRunning;
    }
}
