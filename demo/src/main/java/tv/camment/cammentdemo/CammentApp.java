package tv.camment.cammentdemo;

import android.app.Application;

import tv.camment.cammentsdk.CammentSDK;

public class CammentApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CammentSDK.getInstance().init(this);

    }

}
