package tv.camment.cammentsdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.utils.CommonUtils;


public final class CammentSnackBarDialog extends DialogFragment {

    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_MSG = "arg_msg";
    private static final String ARG_BTN = "arg_btn";

    private boolean doNotDestroyActivity = true;

    private Handler handler;

    public static CammentSnackBarDialog createMsgInstance(String msgString) {
        CammentSnackBarDialog dialogFragment = new CammentSnackBarDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, SnackbarType.MESSAGE_ONLY.getIntValue());
        bundle.putString(ARG_MSG, msgString);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    public static CammentSnackBarDialog createOneBtnInstance(String msgString, @StringRes int btnStringRes) {
        CammentSnackBarDialog dialogFragment = new CammentSnackBarDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, SnackbarType.ONE_BUTTON.getIntValue());
        bundle.putString(ARG_MSG, msgString);
        bundle.putInt(ARG_BTN, btnStringRes);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
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

        SnackbarType snackbarType = SnackbarType.fromInt(getArguments().getInt(ARG_TYPE));

        View view;

        switch (snackbarType) {
            case ONE_BUTTON:
                view = getActivity().getLayoutInflater().inflate(R.layout.cmmsdk_snackbar_one_btn, null);
                break;
            case MESSAGE_ONLY:
            default:
                view = getActivity().getLayoutInflater().inflate(R.layout.cmmsdk_snackbar_msg, null);
                break;
        }

        TextView tvMsg = view.findViewById(R.id.cmmsdk_tv_msg);
        tvMsg.setText(getArguments().getString(ARG_MSG));

        Button btnAction = view.findViewById(R.id.cmmsdk_btn_action);
        if (btnAction != null) {
            btnAction.setText(getArguments().getInt(ARG_BTN));
        }

        doNotDestroyActivity = true;

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            WindowManager.LayoutParams params = window.getAttributes();
            params.y = CommonUtils.dpToPx(getContext(), 16);
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

    public void show(FragmentManager manager, String tag, long delayMillis) {
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

        if (handler == null)
            handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissAllowingStateLoss();
            }
        }, delayMillis);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (!doNotDestroyActivity) {
            Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.finish();
            }
            doNotDestroyActivity = true;
        }

        SnackbarQueueHelper.getInstance().removeSnackbarFromQueue();
    }

    public enum SnackbarType {

        MESSAGE_ONLY(0),
        ONE_BUTTON(1);

        private static Map<Integer, SnackbarType> map = new HashMap<>();

        static {
            for (SnackbarType a : SnackbarType.values()) {
                map.put(a.value, a);
            }
        }

        private int value;

        SnackbarType(int value) {
            this.value = value;
        }

        public static SnackbarType fromInt(int value) {
            return map.get(value);
        }

        public int getIntValue() {
            return value;
        }

    }

}
