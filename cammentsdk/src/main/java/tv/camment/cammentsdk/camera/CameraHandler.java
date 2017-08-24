package tv.camment.cammentsdk.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

final class CameraHandler extends Handler {

    private static final int MSG_PREVIEW_START = 1;
    private static final int MSG_PREVIEW_STOP = 2;
    private CameraThread mThread;

    CameraHandler(final CameraThread thread) {
        mThread = thread;
    }

    void startPreview(Camera.PreviewCallback previewCallback) {
        sendMessage(obtainMessage(MSG_PREVIEW_START, previewCallback));
    }

    /**
     * request to stop camera preview
     * @param needWait need to wait for stopping camera preview
     */
    void stopPreview(final boolean needWait) {
        synchronized (this) {
            sendEmptyMessage(MSG_PREVIEW_STOP);
            if (needWait && mThread.isRunning()) {
                try {
                    wait();
                } catch (final InterruptedException e) {
                    Log.e("CAMMERA", "stopPreview", e);
                }
            }
        }
    }

    /**
     * message handler for camera thread
     */
    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case MSG_PREVIEW_START:
                mThread.startPreview((Camera.PreviewCallback) msg.obj);
                break;
            case MSG_PREVIEW_STOP:
                mThread.stopPreview();
                synchronized (this) {
                    notifyAll();
                }
                Looper myLooper = Looper.myLooper();
                if (myLooper != null) {
                    myLooper.quit();
                }
                mThread = null;
                break;
            default:
                throw new RuntimeException("unknown message:what=" + msg.what);
        }
    }

}
