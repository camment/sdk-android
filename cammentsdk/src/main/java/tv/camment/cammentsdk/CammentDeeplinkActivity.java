package tv.camment.cammentsdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import tv.camment.cammentsdk.helpers.GeneralPreferences;

public class CammentDeeplinkActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readAndSaveDeeplinkUuid("camment", getIntent().getData());

        finish();

        if (CammentSDK.getInstance().getPreviousActivity() == null) {
            Intent i = CammentSDK.getInstance().getApplicationContext().getPackageManager()
                    .getLaunchIntentForPackage(CammentSDK.getInstance().getApplicationContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private void readAndSaveDeeplinkUuid(String scheme, Uri data) {
        if (data == null
                || !scheme.equals(data.getScheme())) {
            GeneralPreferences.getInstance().setDeeplinkUuid("");
            return;
        }

        String authority = data.getAuthority();

        List<String> segments = data.getPathSegments();

        switch (authority) {
            case "group":
                if (segments != null
                        && segments.size() == 1) {
                    GeneralPreferences.getInstance().setDeeplinkUuid(segments.get(0));
                } else {
                    GeneralPreferences.getInstance().setDeeplinkUuid("");
                }
                break;
            default:
                GeneralPreferences.getInstance().setDeeplinkUuid("");
                break;
        }
    }
}
