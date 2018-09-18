package tv.camment.cammentsdk.utils;


import android.graphics.Typeface;

import tv.camment.cammentsdk.CammentSDK;

public final class FontUtils {

    private static FontUtils instance = new FontUtils();
    private static Typeface boldTypeFace;
    private static Typeface regularTypeFace;
    private static Typeface lightTypeFace;
    private static Typeface semiBoldTypeFace;

    public static FontUtils getInstance() {
        return instance;
    }

    public Typeface getBoldTypeFace() {
        if (boldTypeFace == null) {
            try {
                boldTypeFace = Typeface.createFromAsset(CammentSDK.getInstance().getApplicationContext().getAssets(), "fonts/nunito-bold.ttf");
            } catch (Exception e) {
                LogUtils.debug("FontUtils", "can't load font", e);
            }
        }

        return boldTypeFace;
    }

    public Typeface getRegularTypeFace() {
        if (regularTypeFace == null) {
            try {
                regularTypeFace = Typeface.createFromAsset(CammentSDK.getInstance().getApplicationContext().getAssets(), "fonts/nunito-regular.ttf");
            } catch (Exception e) {
                LogUtils.debug("FontUtils", "can't load font", e);
            }
        }

        return regularTypeFace;
    }

    public Typeface getLightTypeFace() {
        if (lightTypeFace == null) {
            try {
                lightTypeFace = Typeface.createFromAsset(CammentSDK.getInstance().getApplicationContext().getAssets(), "fonts/nunito-light.ttf");
            } catch (Exception e) {
                LogUtils.debug("FontUtils", "can't load font", e);
            }
        }

        return lightTypeFace;
    }

    public Typeface getSemiBoldTypeFace() {
        if (semiBoldTypeFace == null) {
            try {
                semiBoldTypeFace = Typeface.createFromAsset(CammentSDK.getInstance().getApplicationContext().getAssets(), "fonts/nunito-semibold.ttf");
            } catch (Exception e) {
                LogUtils.debug("FontUtils", "can't load font", e);
            }
        }

        return semiBoldTypeFace;
    }

}
