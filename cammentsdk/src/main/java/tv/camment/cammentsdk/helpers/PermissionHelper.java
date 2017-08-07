package tv.camment.cammentsdk.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by petrushka on 04/08/2017.
 */

public class PermissionHelper implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = PermissionHelper.class.getSimpleName();

    private static final int RC_CAMERA_MIC_PERM = 123;

    private static PermissionHelper INSTANCE;

    private static final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private PermissionsListener listener;

    private WeakReference<Activity> activityWeakRef;


    public static PermissionHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PermissionHelper();
        }
        return INSTANCE;
    }

    private PermissionHelper() {

    }

    public void initPermissionHelper(Activity activity) {
        activityWeakRef = new WeakReference<>(activity);
    }

    public void setListener(PermissionsListener listener) {
        this.listener = listener;
    }

    @AfterPermissionGranted(RC_CAMERA_MIC_PERM)
    public void cameraAndMicTask() {
        if (activityWeakRef == null || activityWeakRef.get() == null) {
            throw new IllegalArgumentException("PermissionHelper was not initialized");
        }

        if (EasyPermissions.hasPermissions(activityWeakRef.get(), permissions)) {
            Log.d(TAG, "enableRecording");
            if (listener != null) {
                listener.enableRecording();
            }
        } else {
            Log.d(TAG, "disableRecording");
            if (listener != null) {
                listener.disableRecording();
            }
            // Ask for both permissions
            EasyPermissions.requestPermissions(activityWeakRef.get(), "TODO string",
                    RC_CAMERA_MIC_PERM, permissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied");

        if (activityWeakRef != null && activityWeakRef.get() != null) {
            if (EasyPermissions.somePermissionPermanentlyDenied(activityWeakRef.get(), perms)) {
                new AppSettingsDialog.Builder(activityWeakRef.get()).build().show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Log.d("onActivityResult", "resultCode: " + resultCode);
        }
    }

    public interface PermissionsListener {

        void enableRecording();

        void disableRecording();

    }

}
