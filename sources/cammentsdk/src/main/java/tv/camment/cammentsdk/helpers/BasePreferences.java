package tv.camment.cammentsdk.helpers;

import android.content.SharedPreferences;

import tv.camment.cammentsdk.CammentSDK;


abstract class BasePreferences {

    private final SharedPreferences prefs = CammentSDK.getInstance().getApplicationContext()
            .getSharedPreferences(getPreferencesName(), 0);

    protected abstract String getPreferencesName();

    public void clear() {
        prefs.edit().clear().apply();
    }

    void putString(String key, String value) {
        prefs.edit()
                .putString(key, value)
                .apply();
    }

    String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    void putBoolean(String key, boolean value) {
        prefs.edit()
                .putBoolean(key, value)
                .apply();
    }

    boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    void putInt(String key, int value) {
        prefs.edit()
                .putInt(key, value)
                .apply();
    }

    int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    void putLong(String key, long value) {
        prefs.edit()
                .putLong(key, value)
                .apply();
    }

    protected long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

}
