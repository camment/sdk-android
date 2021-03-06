package tv.camment.cammentsdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import tv.camment.cammentsdk.helpers.GeneralPreferences;

public final class CammentDeeplinkActivity extends Activity {

    private static final String GROUP_PATH = "group";
    private static final String SHOW_PATH = "show";

    private static final String SCHEME = "camment";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readAndSaveDeeplinkUuid(getIntent().getData());

        finish();

        if (!CammentSDK.getInstance().isSomeActivityOpened()) {
            Intent i = CammentSDK.getInstance().getApplicationContext().getPackageManager()
                    .getLaunchIntentForPackage(CammentSDK.getInstance().getApplicationContext().getPackageName());
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        }
    }

    private void readAndSaveDeeplinkUuid(Uri data) {
        if (data == null
                || data.getScheme() == null
                || !data.getScheme().startsWith(SCHEME)) {
            GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
            GeneralPreferences.getInstance().setDeeplinkShowUuid("");
            return;
        }

        final String authority = data.getAuthority();
        final List<String> segments = data.getPathSegments();

        switch (authority) {
            case GROUP_PATH:
                if (segments != null
                        && segments.size() > 0) {
                    GeneralPreferences.getInstance().setDeeplinkGroupUuid(segments.get(0));
                } else {
                    GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
                }

                if (segments != null
                        && segments.size() > 2
                        && TextUtils.equals(SHOW_PATH, segments.get(1))) {
                    GeneralPreferences.getInstance().setDeeplinkShowUuid(segments.get(2));
                } else {
                    GeneralPreferences.getInstance().setDeeplinkShowUuid("");
                }
                break;
            default:
                GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
                GeneralPreferences.getInstance().setDeeplinkShowUuid("");
                break;
        }
    }

}