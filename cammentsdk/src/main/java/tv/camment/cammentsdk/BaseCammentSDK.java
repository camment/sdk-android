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

class BaseCammentSDK extends CammentLifecycle {

    static CammentSDK INSTANCE;

    volatile WeakReference<Context> applicationContext;

    private IoTHelper ioTHelper;

    String apiKey;

    public synchronized void init(Context context, String apiKey) {
        if (applicationContext == null || applicationContext.get() == null) {
            if (context == null || !(context instanceof Application)) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            if (TextUtils.isEmpty(apiKey) || SDKConfig.API_KEY_DUMMY.equals(apiKey)) {
                throw new IllegalArgumentException("Invalid CammentSDK API key");
            }
            applicationContext = new WeakReference<>(context);

            this.apiKey = apiKey;

            AWSManager.getInstance().checkKeyStore();

            ((Application) context).registerActivityLifecycleCallbacks(this);

            DataManager.getInstance().clearDataForUserGroupChange();

            ioTHelper = AWSManager.getInstance().getIoTHelper();
            connectToIoT();
        }
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    void setShowUuid(String showUuid) {
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

    public String getApiKey() {
        return apiKey;
    }

}
