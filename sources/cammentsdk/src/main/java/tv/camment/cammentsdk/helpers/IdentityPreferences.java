package tv.camment.cammentsdk.helpers;


import com.amazonaws.mobileconnectors.cognito.Dataset;

import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.utils.LogUtils;

public final class IdentityPreferences extends BasePreferences {

    private static final String DEFAULT_SHAREDPREFERENCES_NAME = "com.amazonaws.android.auth";
    private static final String ID_KEY = "identityId";

    private static IdentityPreferences INSTANCE;

    public static IdentityPreferences getInstance() {
        if (INSTANCE == null) {
            synchronized (IdentityPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IdentityPreferences();
                }
            }
        }
        return INSTANCE;
    }

    private IdentityPreferences() {

    }

    @Override
    protected String getPreferencesName() {
        return DEFAULT_SHAREDPREFERENCES_NAME;
    }

    public String getIdentityId() {
        return getString(namespace(ID_KEY), "");
    }

    public void saveIdentityId(String identityId) {
        putString(namespace(ID_KEY), identityId);
    }

    private String namespace(String key) {
        return AWSManager.getInstance().getCognitoCachingCredentialsProvider().getIdentityPoolId() + "." + key;
    }

    public String getOldIdentityId() {
        try {
            Dataset identitySet = AWSManager.getInstance().getCognitoSyncManager().openOrCreateDataset("identitySet");
            if (identitySet != null) {
                return identitySet.get(getIdentityId());
            }
        } catch (Exception e) {
            LogUtils.debug("onException", "getOldIdentityId", e);
        }
        return "";
    }
}
