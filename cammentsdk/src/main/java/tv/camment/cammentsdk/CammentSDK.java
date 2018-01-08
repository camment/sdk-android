package tv.camment.cammentsdk;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import tv.camment.cammentsdk.auth.CammentAuthIdentityProvider;

public final class CammentSDK extends BaseCammentSDK {

    public static CammentSDK getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CammentSDK();
        }
        return INSTANCE;
    }

    private CammentSDK() {
        super();
    }

    public synchronized void init(Context context, CammentAuthIdentityProvider identityProvider) {
        super.init(context, identityProvider);
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    public void setShowMetadata(ShowMetadata showMetadata) {
        super.setShowMetadata(showMetadata);
    }

    public void setOnDeeplinkOpenShowListener(OnDeeplinkOpenShowListener onDeeplinkOpenShowListener) {
        super.setOnDeeplinkOpenShowListener(onDeeplinkOpenShowListener);
    }

    public OnDeeplinkOpenShowListener getOnDeeplinkOpenShowListener() {
        return super.getOnDeeplinkOpenShowListener();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public CammentAuthIdentityProvider getAppAuthIdentityProvider() {
        return super.getAppAuthIdentityProvider();
    }

    public void setCammentAudioVolumeAdjustment(CammentAudioVolume cammentAudioVolume) {
        super.setCammentAudioVolumeAdjustment(cammentAudioVolume);
    }

    public CammentAudioVolume getCammentAudioVolumeAdjustment() {
        return super.getCammentAudioVolumeAdjustment();
    }

}
