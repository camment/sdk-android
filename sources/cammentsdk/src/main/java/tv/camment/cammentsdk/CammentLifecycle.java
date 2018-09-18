package tv.camment.cammentsdk;


import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;

import static android.content.Context.POWER_SERVICE;

abstract class CammentLifecycle implements Application.ActivityLifecycleCallbacks {

    private static final String TAG_PROGRESS = "tag_progress";

    private boolean progressBarEnabled = true;

    private static List<Activity> activityList = new ArrayList<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        hideCustomBars();

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

        handleScreenLockedUnlocked(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        handleScreenLockedUnlocked(activity);
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
                && !(TextUtils.equals(activity.getClass().getName(), "com.facebook.CustomTabMainActivity"))
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

    private void printList(String from) {
        LogUtils.debug("activityList", "start - " + from);

        if (activityList == null) {
            LogUtils.debug("activityList", "empty");
        } else {
            for (Activity activity : activityList) {
                LogUtils.debug("activityList", activity.getClass().getSimpleName());
            }
        }

        LogUtils.debug("activityList", "end");
    }

    public synchronized void showProgressBar() {
        if (progressBarEnabled) {
            Activity activity = CammentSDK.getInstance().getCurrentActivity();
            if (activity instanceof FragmentActivity) {
                Fragment fragment = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS);
                if (fragment == null || !fragment.isAdded()) {
                    CammentProgressDialog progressDialog = CammentProgressDialog.createInstance();
                    progressDialog.show(((FragmentActivity) activity).getSupportFragmentManager(), TAG_PROGRESS);
                }
            }
        }
    }

    public synchronized void hideProgressBar() {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof FragmentActivity) {
            List<Fragment> fragments = ((FragmentActivity) activity).getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment f : fragments) {
                    if (f instanceof CammentProgressDialog) {
                        ((CammentProgressDialog) f).dismissAllowingStateLoss();
                    }
                }
            }
        }
    }

    private synchronized void hideCustomBars() {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof FragmentActivity) {
            List<Fragment> fragments = ((FragmentActivity) activity).getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment f : fragments) {
                    if (f instanceof CammentSnackBarDialog) {
                        ((CammentSnackBarDialog) f).dismissAllowingStateLoss();
                    } else if (f instanceof CammentProgressDialog) {
                        ((CammentProgressDialog) f).dismissAllowingStateLoss();
                    }
                }
            }
        }
    }

    public synchronized CammentDialog getCammentDialogByTag(String tag) {
        if (getCurrentActivity() instanceof FragmentActivity) {
            Fragment fragmentByTag = ((FragmentActivity) getCurrentActivity()).getSupportFragmentManager().findFragmentByTag(tag);
            if (fragmentByTag instanceof CammentDialog) {
                return (CammentDialog) fragmentByTag;
            }
            return null;
        }
        return null;
    }

    private void handleScreenLockedUnlocked(Activity activity) {
        PowerManager powerManager = (PowerManager) activity.getSystemService(POWER_SERVICE);
        if (powerManager == null)
            return;

        boolean isScreenOn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = powerManager.isInteractive();
        } else {
            isScreenOn = powerManager.isScreenOn();
        }

        if (isScreenOn) {
            SyncHelper.getInstance().restartHandlersIfNeeded();
        } else {
            SyncHelper.getInstance().cleanAllHandlers();
        }
    }

    protected void disableProgressBar() {
        progressBarEnabled = false;
    }

    protected void enableProgressBar() {
        progressBarEnabled = true;
    }

}
