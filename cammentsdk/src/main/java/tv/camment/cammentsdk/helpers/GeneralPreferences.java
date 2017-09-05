package tv.camment.cammentsdk.helpers;

public final class GeneralPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_general_prefs";

    private static final String PREFS_ACTIVE_SHOW_UUID = "general_active_show_uuid";
    private static final String PREFS_PROVIDER_PASSCODE = "general_provider_passcode";
    private static final String PREFS_FIRST_STARTUP = "general_first_startup";

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

    public void setActiveShowUuid(String showUuid) {
        putString(PREFS_ACTIVE_SHOW_UUID, showUuid);
    }

    public String getActiveShowUuid() {
        return getString(PREFS_ACTIVE_SHOW_UUID, "");
    }

    public void setProviderPasscode(String passcode) {
        putString(PREFS_PROVIDER_PASSCODE, passcode);
    }

    public String getProviderPasscode() {
        return getString(PREFS_PROVIDER_PASSCODE, "");
    }

    public void setFirstStartup() {
        putBoolean(PREFS_FIRST_STARTUP, false);
    }

    public boolean isFirstStartup() {
        return getBoolean(PREFS_FIRST_STARTUP, true);
    }

}
