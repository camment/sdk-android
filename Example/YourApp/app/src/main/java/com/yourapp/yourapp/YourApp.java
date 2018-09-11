package com.yourapp.yourapp;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.AccessToken;

import tv.camment.cammentauth.FbAuthIdentityProvider;
import tv.camment.cammentsdk.CammentAudioVolume;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthListener;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;


public class YourApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // IMPORTANT - Use exactly as this, don't pass null or other objects
        CammentSDK.getInstance().init(this, new FbAuthIdentityProvider());

        // NO_ADJUSTMENT means default Android recording volume. Test and select adjustment level by yourself
        CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.NO_ADJUSTMENT);

        CammentSDK.getInstance().setOnDeeplinkOpenShowListener(new OnDeeplinkOpenShowListener() {
            @Override
            public void onOpenShowWithUuid(String showUuid) {
                // use your navigation class to open desired activity
                // below is just simple use case, you have to then handle in the activity that the correct show is set
                MainActivity.start(getApplicationContext(), showUuid);
            }
        });

        CammentSDK.getInstance().getAppAuthIdentityProvider().addCammentAuthListener(new CammentAuthListener() {
            @Override
            public void onLoggedIn(CammentAuthInfo cammentAuthInfo) {
                Log.d("FB_LOGIN", "is CammentAuthInfo null? " + (cammentAuthInfo == null));

                String authType;
                if (cammentAuthInfo == null || cammentAuthInfo.getAuthType() == null) {
                    authType = "UNKNOWN";
                } else {
                    authType = cammentAuthInfo.getAuthType().name();
                }
                Log.d("FB_LOGIN", "CammentAuthInfo type? " + authType);

                if (cammentAuthInfo instanceof CammentFbAuthInfo) {
                    CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) cammentAuthInfo;
                    Log.d("FB_LOGIN", "FB CammentAuthInfo FB user id? " + fbAuthInfo.getFacebookUserId());
                    Log.d("FB_LOGIN", "FB CammentAuthInfo FB token? " + fbAuthInfo.getToken());
                    Log.d("FB_LOGIN", "FB CammentAuthInfo FB token expires? " + (fbAuthInfo.getExpires() == null ? "null" : fbAuthInfo.getExpires().toString()));
                }

                AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                Log.d("FB_LOGIN", "current access token null? " + (currentAccessToken == null));
                Log.d("FB_LOGIN", "current token null? " + (currentAccessToken == null || (currentAccessToken.getToken() == null)));

                if (currentAccessToken != null) {
                    Log.d("FB_LOGIN", "current access token isExpired? " + currentAccessToken.isExpired());
                }

            }

            @Override
            public void onLoggedOut() {
                // perform your code after logout
            }
        });
    }

}
