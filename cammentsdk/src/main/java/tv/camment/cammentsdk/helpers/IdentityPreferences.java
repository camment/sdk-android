package tv.camment.cammentsdk.helpers;


import tv.camment.cammentsdk.aws.AWSManager;

public final class IdentityPreferences extends BasePreferences {

    private static final String DEFAULT_SHAREDPREFERENCES_NAME = "com.amazonaws.android.auth";
    private static final String ID_KEY = "identityId";

    private static IdentityPreferences INSTANCE;

    public static IdentityPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IdentityPreferences();
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

    private String namespace(String key) {
        return AWSManager.getInstance().getCognitoCachingCredentialsProvider().getIdentityPoolId() + "." + key;
    }

}
