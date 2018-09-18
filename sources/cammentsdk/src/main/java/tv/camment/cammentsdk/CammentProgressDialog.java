package tv.camment.cammentsdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.lang.reflect.Field;


public final class CammentProgressDialog extends DialogFragment {

    private boolean doNotDestroyActivity = true;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());

        View view = getActivity().getLayoutInflater().inflate(R.layout.cmmsdk_progressbar_dialog, null);

        doNotDestroyActivity = true;

        ProgressBar progressBar = view.findViewById(R.id.cmmsdk_progressbar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark),
                PorterDuff.Mode.SRC_IN);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setContentView(view);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent != null
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    doNotDestroyActivity = false;
                }
                return false;
            }
        });

        return dialog;
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!doNotDestroyActivity) {
            Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.finish();
            }
            doNotDestroyActivity = true;
        }
    }

}
