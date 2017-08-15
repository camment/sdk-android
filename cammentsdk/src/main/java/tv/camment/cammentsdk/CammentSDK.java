package tv.camment.cammentsdk;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.camment.clientsdk.model.Show;

import java.lang.ref.WeakReference;

import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.IoTHelper;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.helpers.FacebookHelper;

public final class CammentSDK extends CammentLifecycle {

    private static CammentSDK INSTANCE;

    private volatile WeakReference<Context> applicationContext;

    private WeakReference<Activity> currentActivity;

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

}
