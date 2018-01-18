package tv.camment.cammentsdk;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.views.CammentDialog;

abstract class CammentLifecycle implements Application.ActivityLifecycleCallbacks {

    private List<Activity> activityList = new ArrayList<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        hideProgressBar();

        addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (isActivityValid(activity)) {
            CammentSDK.getInstance().connectToIoT();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isActivityValid(activity)
                && !(activity instanceof DeeplinkIgnore)
                && !TextUtils.equals(GeneralPreferences.getInstance().getDeeplinkGroupUuid(), GeneralPreferences.getInstance().getCancelledDeeplinkUuid())) {
            CammentSDK.getInstance().handleDeeplink();
        }

        if (TextUtils.equals(GeneralPreferences.getInstance().getDeeplinkGroupUuid(), GeneralPreferences.getInstance().getCancelledDeeplinkUuid())) {
            GeneralPreferences.getInstance().setCancelledDeeplinkUuid("");
            GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
            GeneralPreferences.getInstance().setDeeplinkShowUuid("");
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
        removeActivity(activity);

        if (BuildConfig.USE_MIXPANEL) {
            MixpanelHelper.getInstance().flush();
        }
    }

    public synchronized Activity getCurrentActivity() {
        if (activityList == null
                || activityList.size() == 0) {
            return null;
        }

        for (int i = activityList.size() - 1; i >= 0; i--) {
            if (isActivityValid(activityList.get(i))) {
                return activityList.get(i);
            }
        }
        return null;
    }

    private synchronized boolean isActivityValid(Activity activity) {
        return !(TextUtils.equals(activity.getClass().getName(), "com.facebook.FacebookActivity"))
                && !(TextUtils.equals(activity.getClass().getName(), "com.facebook.CustomTabActivity"))
                && !(activity instanceof CammentDeeplinkActivity);
    }

    synchronized boolean isSomeActivityOpened() {
        return !(activityList == null
                || activityList.size() == 0) && activityList.size() > 1;

    }

    private synchronized void addActivity(Activity activity) {
        if (activityList != null) {
            activityList.add(activity);
        }
        //printList("add");
    }

    private synchronized void removeActivity(Activity activity) {
        if (activityList != null
                && activityList.size() > 0) {
            activityList.remove(activity);
        }
        //printList("remove");
    }

    public synchronized CammentDialog getCammentDialogByTag(String tag) {
        if (getCurrentActivity() instanceof AppCompatActivity) {
            Fragment fragmentByTag = ((AppCompatActivity) getCurrentActivity()).getSupportFragmentManager().findFragmentByTag(tag);
            if (fragmentByTag instanceof CammentDialog) {
                return (CammentDialog) fragmentByTag;
            }
            return null;
        }
        return null;
    }

    private void printList(String from) {
        Log.d("activityList", "start - " + from);

        if (activityList == null) {
            Log.d("activityList", "empty");
        } else {
            for (Activity activity : activityList) {
                Log.d("activityList", activity.getClass().getSimpleName());
            }
        }

        Log.d("activityList", "end");
    }

    public synchronized void showProgressBar() {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof AppCompatActivity) {
            Fragment fragment = ((AppCompatActivity) activity).getSupportFragmentManager().findFragmentByTag("PROGRESS");
            if (fragment == null || !fragment.isAdded()) {
                CammentProgressDialog progressDialog = CammentProgressDialog.createInstance();
                progressDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), "PROGRESS");
            }
        }
    }

    public synchronized void hideProgressBar() {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof AppCompatActivity) {
            List<Fragment> fragments = ((AppCompatActivity) activity).getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment f : fragments) {
                    if (f instanceof CammentProgressDialog) {
                        ((CammentProgressDialog) f).dismissAllowingStateLoss();
                    }
                }
            }
        }
    }

}
