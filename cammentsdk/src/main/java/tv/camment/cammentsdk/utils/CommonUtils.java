package tv.camment.cammentsdk.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import tv.camment.cammentsdk.SDKConfig;


public final class CommonUtils {

    private static final float DEF_RATIO = 592f/360f;

    public static synchronized int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static synchronized int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static synchronized int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static void setViewSizes(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        float shorterSize = dpWidth > dpHeight ? dpHeight : dpWidth;
        float ratio = dpWidth > dpHeight ? dpWidth/dpHeight : dpHeight/dpWidth;

        float defCamSize = shorterSize / 4.3f;
        float defRecSize = shorterSize / 6.4f;

        float percent = ratio / DEF_RATIO;
        if (DEF_RATIO - ratio > 0.0f) {
            percent *= (1.0f - (DEF_RATIO - ratio));
        }

        SDKConfig.CAMMENT_BIG_DP = Math.round(defCamSize * percent);
        SDKConfig.RECORD_INDICATOR_DP = Math.round(defCamSize * percent / 7);
        SDKConfig.RECORD_NORMAL_DP = Math.round(defRecSize * percent);
    }
}
