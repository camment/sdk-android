package tv.camment.cammentsdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.lang.reflect.Field;


public final class CammentProgressDialog extends DialogFragment {

    private boolean doNotDestroyActivity = false;

    public static CammentProgressDialog createInstance() {
        return new CammentProgressDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        doNotDestroyActivity = false;

        View view = inflater.inflate(R.layout.cmmsdk_progressbar_dialog, container);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.cmmsdk_progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark),
                PorterDuff.Mode.SRC_IN);

        return view;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Field mDismissed = DialogFragment.class.getDeclaredField("mDismissed");
            Field mShownByMe = DialogFragment.class.getDeclaredField("mShownByMe");
            mDismissed.setAccessible(true);
            mShownByMe.setAccessible(true);
            mDismissed.setBoolean(this, false);
            mShownByMe.setBoolean(this, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!doNotDestroyActivity) {
            Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.finish();
            }
            doNotDestroyActivity = false;
        }
    }

    public void doNotDestroyActivity() {
        doNotDestroyActivity = true;
    }
}
