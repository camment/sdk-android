package tv.camment.cammentsdk.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;


public final class CammentDialog extends DialogFragment {

    private static final String ARGS_MESSAGE = "args_messgae";

    private BaseMessage message;

    private TextView tvTitle;
    private TextView tvMessage;
    private Button btnPositive;
    private Button btnNegative;

    private ActionListener actionListener;

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new CammentDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
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
            }
        }

        message = getArguments().getParcelable(ARGS_MESSAGE);

        View view = inflater.inflate(R.layout.cmmsdk_title_message_dialog, container);

        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvMessage = (TextView) view.findViewById(R.id.tv_message);
        btnPositive = (Button) view.findViewById(R.id.btn_positive);
        btnNegative = (Button) view.findViewById(R.id.btn_negative);

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
                dismiss();
            }
        });

        setupTitle();
        setupMessage();
        setupButtons();

        return view;
    }

    private void setupTitle() {
        switch (message.type) {
            case INVITATION:
                tvTitle.setText(String.format(getString(R.string.cmmsdk_user_invited_to_chat), ((InvitationMessage) message).body.invitingUser.name));
                break;
            case INVITATION_SENT:
                tvTitle.setText(R.string.cmmsdk_user_invitation_sent_title);
                break;
            case NEW_USER_IN_GROUP:
                tvTitle.setText(String.format(getString(R.string.cmmsdk_user_entered_chat_title), ((NewUserInGroupMessage) message).body.user.name));
                break;
            case ONBOARDING:
                tvTitle.setText(R.string.cmmsdk_setup_use_camment_chat);
                break;
        }
    }

    private void setupMessage() {
        switch (message.type) {
            case INVITATION:
                tvMessage.setText(R.string.cmmsdk_join_conversation);
                break;
            case INVITATION_SENT:
                tvMessage.setText(R.string.cmmsdk_user_invitation_sent_desc);
                break;
            case NEW_USER_IN_GROUP:
                tvMessage.setText(R.string.cmmsdk_user_entered_chat_desc);
                break;
            case ONBOARDING:
                tvMessage.setText(R.string.cmmsdk_setup_what_is_camment);
                break;
        }
    }

    private void setupButtons() {
        switch (message.type) {
            case INVITATION:
                btnPositive.setText(R.string.cmmsdk_join);
                btnNegative.setText(R.string.cmmsdk_no);
                break;
            case INVITATION_SENT:
            case NEW_USER_IN_GROUP:
                btnPositive.setText(R.string.cmmsdk_ok);
                btnNegative.setVisibility(View.GONE);
                break;
            case ONBOARDING:
                btnPositive.setText(R.string.cmmsdk_setup_sounds_fun);
                btnNegative.setText(R.string.cmmsdk_setup_maybe_later);
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

    }

}
