package tv.camment.cammentsdk;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

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

    public synchronized void init(Context context) {
        super.init(context);
    }

    public Context getApplicationContext() {
        return applicationContext.get();
    }

    public void setShowUuid(String showUuid) {
        super.setShowUuid(showUuid);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void handleDeeplink(Uri data, String scheme) {
        super.handleDeeplink(data, scheme);
    }

}
