package tv.camment.cammentsdk;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import tv.camment.cammentsdk.auth.CammentAuthIdentityProvider;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentPlayerListener;

public final class CammentSDK extends BaseCammentSDK {

    public static CammentSDK getInstance() {
        if (INSTANCE == null) {
            synchronized (CammentSDK.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CammentSDK();
                }
            }
        }
        return INSTANCE;
    }

    private CammentSDK() {
        super();
    }

    @Override
    public synchronized void init(Context context, CammentAuthIdentityProvider identityProvider) {
        super.init(context, identityProvider);
    }

    @Override
    public Context getApplicationContext() {
        return applicationContext.get();
    }

    @Override
    public void setShowMetadata(ShowMetadata showMetadata) {
        super.setShowMetadata(showMetadata);
    }

    @Override
    public ShowMetadata getShowMetadata() {
        return super.getShowMetadata();
    }

    @Override
    public void setOnDeeplinkOpenShowListener(OnDeeplinkOpenShowListener onDeeplinkOpenShowListener) {
        super.setOnDeeplinkOpenShowListener(onDeeplinkOpenShowListener);
    }

    @Override
    public OnDeeplinkOpenShowListener getOnDeeplinkOpenShowListener() {
        return super.getOnDeeplinkOpenShowListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public CammentAuthIdentityProvider getAppAuthIdentityProvider() {
        return super.getAppAuthIdentityProvider();
    }

    @Override
    public void setCammentAudioVolumeAdjustment(CammentAudioVolume cammentAudioVolume) {
        super.setCammentAudioVolumeAdjustment(cammentAudioVolume);
    }

    @Override
    public CammentAudioVolume getCammentAudioVolumeAdjustment() {
        return super.getCammentAudioVolumeAdjustment();
    }

    @Override
    public void setCammentAudioListener(CammentAudioListener cammentAudioListener) {
        super.setCammentAudioListener(cammentAudioListener);
    }

    @Override
    public CammentAudioListener getCammentAudioListener() {
        return super.getCammentAudioListener();
    }

    @Override
    public void setCammentPlayerListener(CammentPlayerListener cammentPlayerListener) {
        super.setCammentPlayerListener(cammentPlayerListener);
    }

    @Override
    public CammentPlayerListener getCammentPlayerListener() {
        return super.getCammentPlayerListener();
    }

    @Override
    public void onPlaybackPaused(int currentPositionMillis) {
        super.onPlaybackPaused(currentPositionMillis);
    }

    @Override
    public void onPlaybackStarted(int currentPositionMillis) {
        super.onPlaybackStarted(currentPositionMillis);
    }

    @Override
    public void onPlaybackPositionChanged(int currentPositionMillis, boolean isPlaying) {
        super.onPlaybackPositionChanged(currentPositionMillis, isPlaying);
    }

    @Override
    public void setSyncEnabled(boolean syncEnabled) {
        super.setSyncEnabled(syncEnabled);
    }

    @Override
    public boolean isSyncEnabled() {
        return super.isSyncEnabled();
    }

    @Override
    public void disableProgressBar() {
        super.disableProgressBar();
    }

    @Override
    public void enableProgressBar() {
        super.enableProgressBar();
    }
}
