package tv.camment.cammentsdk;


import android.content.Context;
import android.text.TextUtils;

import com.camment.clientsdk.model.Show;

import java.lang.ref.WeakReference;

import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.ShowProvider;

public final class CammentSDK {

    private static CammentSDK INSTANCE;

    private static volatile WeakReference<Context> applicationContext;

    public static CammentSDK getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CammentSDK();
        }
        return INSTANCE;
    }

    private CammentSDK() {
    }

    public static synchronized void init(Context context) {
        if (applicationContext == null || applicationContext.get() == null) {
            if (context == null) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            applicationContext = new WeakReference<>(context);
            getInstance();
            AWSManager.getInstance().getKeystoreHelper().checkKeyStore();
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

}
