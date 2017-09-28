package tv.camment.cammentdemo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import tv.camment.cammentsdk.CammentSDK;

public class CammentApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        CammentSDK.getInstance().init(this);
    }

}