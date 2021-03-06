package tv.camment.cammentsdk.views.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.UserBlockMessage;
import tv.camment.cammentsdk.aws.messages.UserUnblockMessage;


public abstract class CammentDialog extends DialogFragment {

    private static final String ARGS_MESSAGE = "args_message";

    private BaseMessage message;

    private TextView tvTitle;
    private TextView tvMessage;
    private Button btnPositive;
    private Button btnNegative;

    private ActionListener actionListener;

    static Bundle getBaseMessageArgs(BaseMessage message) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MESSAGE, message);
        return args;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }

        message = getArguments().getParcelable(ARGS_MESSAGE);

        View view = inflater.inflate(R.layout.cmmsdk_title_message_dialog, container);

        tvTitle = view.findViewById(R.id.cmmsdk_tv_title);
        tvMessage = view.findViewById(R.id.cmmsdk_tv_message);
        btnPositive = view.findViewById(R.id.cmmsdk_btn_positive);
        btnNegative = view.findViewById(R.id.cmmsdk_btn_negative);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onPositiveButtonClick(message);
                }
                dismiss();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onNegativeButtonClick(message);
                }
                dismiss();
            }
        });

        setupTitle();
        setupMessage();
        setupButtons();

        if (getActionListener() != null) {
            setActionListener(getActionListener());
        }

        return view;
    }

    abstract ActionListener getActionListener();

    @Override
    public void onCancel(DialogInterface dialog) {
        if (actionListener != null
                && message != null
                && message.type == MessageType.ONBOARDING) {
            actionListener.onNegativeButtonClick(message);
        }
    }

    public void show() {
        final String tag = getDialogTag();

        CammentDialog cammentDialogByTag = CammentSDK.getInstance().getCammentDialogByTag(tag);
        if (cammentDialogByTag != null) {
            cammentDialogByTag.dismiss();
        }

        Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
        if (currentActivity instanceof FragmentActivity) {
            FragmentManager manager = ((FragmentActivity) currentActivity).getSupportFragmentManager();
            show(manager, tag);
        }
    }

    abstract String getDialogTag();

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
        if (manager != null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        }
    }

    private void setupTitle() {
        switch (message.type) {
            case INVITATION:
                tvTitle.setText(R.string.cmmsdk_anonymous_invited_to_chat);
                break;
            case ONBOARDING:
                tvTitle.setText(R.string.cmmsdk_setup_use_camment_chat);
                break;
            case SHARE:
                tvTitle.setText(R.string.cmmsdk_invitation_link_title);
                break;
            case LOGIN_CONFIRMATION:
                tvTitle.setText(R.string.cmmsdk_login_fb_title);
                break;
            case LEAVE_CONFIRMATION:
                tvTitle.setText(R.string.cmmsdk_leave_confirmation_title);
                break;
            case BLOCK_CONFIRMATION:
                tvTitle.setText(R.string.cmmsdk_block_confirmation_title);
                break;
            case UNBLOCK_CONFIRMATION:
                tvTitle.setText(R.string.cmmsdk_unblock_confirmation_title);
                break;
            case BLOCKED:
                tvTitle.setText(R.string.cmmsdk_blocked_from_group_title);
                break;
            case LOGOUT_CONFIRMATION:
                tvTitle.setText(R.string.cmmsdk_logout_confirmation_title);
                break;
        }
    }

    private void setupMessage() {
        switch (message.type) {
            case INVITATION:
                tvMessage.setText(R.string.cmmsdk_join_conversation);
                break;
            case ONBOARDING:
                tvMessage.setText(R.string.cmmsdk_setup_what_is_camment);
                break;
            case SHARE:
                tvMessage.setText(R.string.cmmsdk_invitation_link_desc);
                break;
            case LOGIN_CONFIRMATION:
                tvMessage.setText(R.string.cmmsdk_login_fb_msg);
                break;
            case LEAVE_CONFIRMATION:
                tvMessage.setText(R.string.cmmsdk_leave_confirmation_msg);
                break;
            case BLOCK_CONFIRMATION:
                String blockName = getString(R.string.cmmsdk_user);
                if (message instanceof UserBlockMessage) {
                    blockName = ((UserBlockMessage) message).body.name;
                }
                tvMessage.setText(String.format(getString(R.string.cmmsdk_block_confirmation_msg), blockName));
                break;
            case UNBLOCK_CONFIRMATION:
                String unblockName = getString(R.string.cmmsdk_user);
                if (message instanceof UserUnblockMessage) {
                    unblockName = ((UserUnblockMessage) message).body.name;
                }
                tvMessage.setText(String.format(getString(R.string.cmmsdk_unblock_confirmation_msg), unblockName));
                break;
            case BLOCKED:
                tvMessage.setText(R.string.cmmsdk_blocked_from_group_msg);
                break;
            case LOGOUT_CONFIRMATION:
                tvMessage.setText(R.string.cmmsdk_drawer_logout_fb_msg);
        }
    }

    private void setupButtons() {
        switch (message.type) {
            case INVITATION:
                btnPositive.setText(R.string.cmmsdk_join);
                btnNegative.setText(R.string.cmmsdk_no);
                break;
            case BLOCKED:
                btnPositive.setText(R.string.cmmsdk_ok);
                btnNegative.setVisibility(View.GONE);
                break;
            case ONBOARDING:
                btnPositive.setText(R.string.cmmsdk_setup_sounds_fun);
                btnNegative.setText(R.string.cmmsdk_setup_maybe_later);
                break;
            case LEAVE_CONFIRMATION:
            case BLOCK_CONFIRMATION:
            case UNBLOCK_CONFIRMATION:
                btnPositive.setText(R.string.cmmsdk_yes);
                btnNegative.setText(R.string.cmmsdk_no);
                break;
            case SHARE:
                btnPositive.setText(R.string.cmmsdk_ok);
                btnNegative.setText(R.string.cmmsdk_cancel);
                break;
            case LOGIN_CONFIRMATION:
                btnPositive.setText(R.string.cmmsdk_login);
                btnNegative.setText(R.string.cmmsdk_no);
                break;
            case LOGOUT_CONFIRMATION:
                btnPositive.setText(R.string.cmmsdk_drawer_logout_fb);
                btnNegative.setText(R.string.cmmsdk_cancel);
                break;
        }
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public MessageType getMessageType() {
        return message.type;
    }

    public interface ActionListener {

        void onPositiveButtonClick(BaseMessage baseMessage);

        void onNegativeButtonClick(BaseMessage baseMessage);

    }

}
