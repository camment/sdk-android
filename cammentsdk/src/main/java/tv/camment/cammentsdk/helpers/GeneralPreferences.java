package tv.camment.cammentsdk.helpers;

public final class GeneralPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_general_prefs";

    private static final String PREFS_INITIAL_DEEPLINK = "general_initial_deeplink";

    private static GeneralPreferences INSTANCE;

    public static GeneralPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeneralPreferences();
        }
        return INSTANCE;
    }

    private GeneralPreferences() {

    }

    @Override
    protected String getPreferencesName() {
        return PREFS_NAME;
    }

    public void recordInitialDeeplinkRead() {
        putBoolean(PREFS_INITIAL_DEEPLINK, true);
    }

    public boolean wasInitialDeepLinkRead() {
        return getBoolean(PREFS_INITIAL_DEEPLINK, false);
    }

}
