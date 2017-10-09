package tv.camment.cammentsdk;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.FacebookActivity;

import java.lang.ref.WeakReference;

abstract class CammentLifecycle implements Application.ActivityLifecycleCallbacks {

    private WeakReference<Activity> currentActivity;
    private WeakReference<Activity> previousActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof FacebookActivity
                || activity instanceof CammentDeeplinkActivity
                || activity instanceof DeeplinkIgnore)
            return;

        previousActivity = currentActivity;
        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof FacebookActivity
                || activity instanceof CammentDeeplinkActivity
                || activity instanceof DeeplinkIgnore)
            return;

        previousActivity = currentActivity;
        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof FacebookActivity
                || activity instanceof CammentDeeplinkActivity
                || activity instanceof DeeplinkIgnore)
            return;

        previousActivity = currentActivity;
        currentActivity = new WeakReference<>(activity);

        if (previousActivity != null
                && previousActivity.get() != null) {
            CammentSDK.getInstance().handleDeeplink("camment");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        previousActivity = null;
    }

    public Activity getCurrentActivity() {
        return currentActivity.get();
    }

    Activity getPreviousActivity() {
        if (previousActivity == null)
            return null;
        return previousActivity.get();
    }

}
