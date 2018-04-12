package com.yourapp.yourapp;

import android.app.Application;

import tv.camment.cammentauth.FbAuthIdentityProvider;
import tv.camment.cammentsdk.CammentAudioVolume;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.EurovisionPlayerActivity;
import tv.camment.cammentsdk.EurovisionShowsActivity;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;


public class YourApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // IMPORTANT - Use exactly as this, don't pass null or other objects
        CammentSDK.getInstance().init(this, new FbAuthIdentityProvider());

        CammentSDK.getInstance().setOnDeeplinkOpenShowListener(new OnDeeplinkOpenShowListener() {
            @Override
            public void onOpenShowWithUuid(String showUuid) {
                handleOpenShowWithUuid(showUuid);
            }
        });
    }

    private void handleOpenShowWithUuid(String showUuid) {
        // This class will handle automatic opening of karaoke player
        EurovisionShowsActivity.startFromDeeplink(this, showUuid);
    }

}
