package tv.camment.cammentsdk;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.camment.clientsdk.model.Show;

import java.lang.ref.WeakReference;

import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.views.CammentOverlay;

public final class CammentSDK extends CammentLifecycle {

    private static CammentSDK INSTANCE;

    private volatile WeakReference<Context> applicationContext;

    private IoTHelper ioTHelper;

    public static CammentSDK getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CammentSDK();
        }
        return INSTANCE;
    }

    private CammentSDK() {
    }

    public synchronized void init(Context context) {
        if (applicationContext == null || applicationContext.get() == null) {
            if (context == null || !(context instanceof Application)) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            applicationContext = new WeakReference<>(context);

            AWSManager.getInstance().getKeystoreHelper().checkKeyStore();

            ((Application) context).registerActivityLifecycleCallbacks(this);

            DataManager.getInstance().clearDataForUserGroupChange();

            ioTHelper = AWSManager.getInstance().getIoTHelper();
            connectToIoT();
        }
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    public synchronized void setShowUuid(String showUuid) {
        if (TextUtils.isEmpty(showUuid)) {
            throw new IllegalArgumentException("Show uuid can't be null!");
        }

        Show show = new Show();
        show.setUuid(showUuid);

        ShowProvider.insertShow(show);
    }

    private void connectToIoT() {
        if (ioTHelper != null
                && FacebookHelper.getInstance().isLoggedIn()) {
            ioTHelper.connect();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FacebookHelper.getInstance().getCallbackManager().onActivityResult(requestCode, resultCode, data);
        DataManager.getInstance().handleFbPermissionsResult();

        PermissionHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
