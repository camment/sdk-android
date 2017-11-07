package tv.camment.cammentsdk.helpers;

import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;

public final class MixpanelHelper {

    private static final String PROJECT_TOKEN = "6231cfc8ae03c78928c045c7cb9853b3";

    public static final String APP_START = "App Start";
    public static final String SHOWS_LIST_SCREEN = "Shows List Screen";
    public static final String SHOW_SCREEN = "Show Screen";
    public static final String INVITE = "Invite to Group";
    public static final String OPEN_DEEPLINK = "Open Deeplink";
    public static final String JOIN_GROUP = "Join Group";
    public static final String ACCEPT_JOIN_REQUEST = "Accept Join Request";
    public static final String DECLINE_JOIN_REQUEST = "Decline Join Request";
    public static final String FB_SIGNIN = "FB SignIn";
    public static final String CAMMENT_RECORD = "Record Camment";
    public static final String CAMMENT_PLAY = "Play Camment";
    public static final String CAMMENT_DELETE = "Delete Camment";
    public static final String ONBOARDING_START = "Onboarding Start";
    public static final String ONBOARDING_END = "Onboarding End";

    private static final String PROP_COGNITO_ID = "Cognito IdenityId";

    private static MixpanelHelper INSTANCE;

    public static MixpanelHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MixpanelHelper();
        }
        return INSTANCE;
    }

    private MixpanelHelper() {

    }

    public MixpanelAPI getMixpanel() {
        return MixpanelAPI.getInstance(CammentSDK.getInstance().getApplicationContext(), PROJECT_TOKEN);
    }

    private void setIdentityForAnonymousIfNeeded() {
        if (GeneralPreferences.getInstance().shouldSetMixpanelAlias()) {
            final String identityId = IdentityPreferences.getInstance().getIdentityId();
            getMixpanel().alias(identityId, getMixpanel().getDistinctId());
            GeneralPreferences.getInstance().setMixpanelAliasSet();
        }
    }

    public void setIdentity() {
        setIdentityForAnonymousIfNeeded();

        final String identityId = IdentityPreferences.getInstance().getIdentityId();
        getMixpanel().identify(identityId);
        getMixpanel().getPeople().identify(identityId);

        try {
            JSONObject props = new JSONObject();
            props.put(PROP_COGNITO_ID, identityId);
            getMixpanel().registerSuperProperties(props);
        } catch (JSONException e) {
            Log.e("Mixpanel", "Unable to put prop into JSONObject", e);
        }
    }

    public void flush() {
        getMixpanel().flush();
    }

    public void trackEvent(String event) {
        if (BuildConfig.USE_MIXPANEL) {
            getMixpanel().track(event);
        }
    }

}