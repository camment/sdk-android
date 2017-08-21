package tv.camment.cammentsdk.helpers;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import tv.camment.cammentsdk.CammentSDK;

/**
 * Created by petrushka on 21/08/2017.
 */

public abstract class BasePreferences {

    protected SharedPreferences prefs = CammentSDK.getInstance().getApplicationContext()
            .getSharedPreferences(getPreferencesName(), 0);

    protected abstract String getPreferencesName();

    public void clear() {
        prefs.edit().clear().apply();
    }

    protected void putString(String key, String value) {
        prefs.edit()
                .putString(key, value)
                .apply();
    }

    protected String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    protected void putBoolean(String key, boolean value) {
        prefs.edit()
                .putBoolean(key, value)
                .apply();
    }

    protected boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    protected void putInt(String key, int value) {
        prefs.edit()
                .putInt(key, value)
                .apply();
    }

    protected int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    protected void putLong(String key, long value) {
        prefs.edit()
                .putLong(key, value)
                .apply();
    }

    protected long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

}
