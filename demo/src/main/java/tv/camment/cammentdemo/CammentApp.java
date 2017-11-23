package tv.camment.cammentdemo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import tv.camment.cammentauth.FbAuthIdentityProvider;
import tv.camment.cammentsdk.CammentSDK;

public class CammentApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.USE_FABRICS) {
            Fabric.with(this, new Crashlytics());
        }

        CammentSDK.getInstance().init(this, new FbAuthIdentityProvider());
        CammentSDK.getInstance().setOnDeeplinkOpenShowListener(new ShowNavigator());
    }

}