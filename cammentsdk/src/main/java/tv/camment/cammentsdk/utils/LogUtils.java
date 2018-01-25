package tv.camment.cammentsdk.utils;


import android.util.Log;

import tv.camment.cammentsdk.BuildConfig;

public final class LogUtils {

    public static void debug(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void debug(final String tag, String message, Exception e) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, e);
        }
    }

}
