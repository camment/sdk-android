package tv.camment.cammentdemo;

import android.app.Application;

import tv.camment.cammentsdk.CammentSDK;


public class CammentDemoApp extends Application {

    private static CammentDemoApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        CammentSDK.init(this);
    }

}
