package tv.camment.cammentsdk.helpers;

public final class GeneralPreferences extends BasePreferences {

    private static final String PREFS_NAME = "camment_general_prefs";

    private static final String PREFS_ACTIVE_SHOW_UUID = "general_active_show_uuid";
    private static final String PREFS_INVITATION_TEXT = "general_invitation_text";
    private static final String PREFS_PROVIDER_PASSCODE = "general_provider_passcode";
    private static final String PREFS_FIRST_STARTUP = "general_first_startup";
    private static final String PREFS_DEEPLINK_GROUP_UUID = "general_deeplink_group_uuid";
    private static final String PREFS_DEEPLINK_SHOW_UUID = "general_deeplink_show_uuid";
    private static final String PREFS_MIXPANEL_ALIAS = "general_mixpanel_alias";
    private static final String PREFS_CANCELLED_DEEPLINK_UUID = "general_cancelled_deeplink_uuid";
    private static final String PREFS_TOKEN = "general_token";

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

    public void setInvitationText(String invitationText) {
        putString(PREFS_INVITATION_TEXT, invitationText);
    }

    public String getInvitationText() {
        return getString(PREFS_INVITATION_TEXT, "");
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

    public void setDeeplinkGroupUuid(String groupUuid) {
        putString(PREFS_DEEPLINK_GROUP_UUID, groupUuid);
    }

    public String getDeeplinkGroupUuid() {
        return getString(PREFS_DEEPLINK_GROUP_UUID, "");
    }

    public void setDeeplinkShowUuid(String showUuid) {
        putString(PREFS_DEEPLINK_SHOW_UUID, showUuid);
    }

    public String getDeeplinkShowUuid() {
        return getString(PREFS_DEEPLINK_SHOW_UUID, "");
    }

    void setMixpanelAliasSet() {
        putBoolean(PREFS_MIXPANEL_ALIAS, false);
    }

    boolean shouldSetMixpanelAlias() {
        return getBoolean(PREFS_MIXPANEL_ALIAS, true);
    }

    public String getCancelledDeeplinkUuid() {
        return getString(PREFS_CANCELLED_DEEPLINK_UUID, "");
    }

    public void setCancelledDeeplinkUuid(String groupUuid) {
        putString(PREFS_CANCELLED_DEEPLINK_UUID, groupUuid);
    }

    public String getToken() {
        return getString(PREFS_TOKEN, "");
    }

    public void setToken(String token) {
        putString(PREFS_TOKEN, token);
    }

}
