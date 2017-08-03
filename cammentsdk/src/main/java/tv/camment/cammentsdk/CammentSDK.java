package tv.camment.cammentsdk;


import android.content.Context;

import com.github.florent37.androidnosql.AndroidNoSql;

public final class CammentSDK {

    private static CammentSDK INSTANCE;

    private static volatile Context applicationContext;

    public static CammentSDK getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CammentSDK();
        }
        return INSTANCE;
    }

    private CammentSDK() {
    }

    public static synchronized void init(Context context) {
        if (applicationContext == null) {
            if (context == null) {
                throw new IllegalArgumentException("Can't init CammentSDK with null application context");
            }
            applicationContext = context;
            getInstance();
            AndroidNoSql.initWithDefault(applicationContext);
        }
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

}
