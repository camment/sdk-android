package tv.camment.cammentsdk;


import android.content.Context;

import com.github.florent37.androidnosql.AndroidNoSql;

import java.lang.ref.WeakReference;

import tv.camment.cammentsdk.aws.AWSManager;

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
            AndroidNoSql.initWithDefault(applicationContext.get());
            AWSManager.getInstance().getKeystoreHelper().checkKeyStore();
        }
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

}
