package tv.camment.cammentsdk;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.FacebookActivity;

import java.lang.ref.WeakReference;

abstract class CammentLifecycle implements Application.ActivityLifecycleCallbacks {

    private WeakReference<Activity> currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof FacebookActivity)
            return;

        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

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

    }

    public Activity getCurrentActivity() {
        return currentActivity.get();
    }

}
