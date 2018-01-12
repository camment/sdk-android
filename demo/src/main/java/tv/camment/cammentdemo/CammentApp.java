package tv.camment.cammentdemo;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import tv.camment.cammentauth.FbAuthIdentityProvider;
import tv.camment.cammentsdk.CammentAudioVolume;
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

        setCammentAudioAdjustment(); //only for demo
    }

    private void setCammentAudioAdjustment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CammentSDK.getInstance().getApplicationContext());
        String volumeValue = prefs.getString(getString(R.string.key_adjust_camment_volume), null);

        if (volumeValue != null) {
            switch (volumeValue) {
                case "0":
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.NO_ADJUSTMENT);
                    break;
                case "1":
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.MILD_ADJUSTMENT);
                    break;
                case "2":
                default:
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.FULL_ADJUSTMENT);
                    break;
            }
        }
    }

}