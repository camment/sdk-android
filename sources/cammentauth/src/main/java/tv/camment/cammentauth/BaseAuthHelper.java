package tv.camment.cammentauth;


import java.util.HashSet;
import java.util.Set;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.auth.CammentAuthListener;

abstract class BaseAuthHelper {

    Set<CammentAuthListener> cammentAuthListeners;

    BaseAuthHelper() {
        addCammentAuthListener(CammentSDK.getInstance());
    }

    void addCammentAuthListener(CammentAuthListener cammentAuthListener) {
        if (cammentAuthListeners == null) {
            cammentAuthListeners = new HashSet<>();
        }
        cammentAuthListeners.add(cammentAuthListener);
    }
}
