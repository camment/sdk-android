package tv.camment.cammentsdk.utils;

import android.content.Context;

/**
 * Created by petrushka on 04/08/2017.
 */

public class CommonUtils {

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
