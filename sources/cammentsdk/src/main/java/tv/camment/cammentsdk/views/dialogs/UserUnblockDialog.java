package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.listeners.UserUnblockDialogActionListener;

public final class UserUnblockDialog extends CammentDialog {

    public static final String TAG = "tag_user_unblock";

    private static final String ARGS_USER_INFO = "args_user_info";

    public static CammentDialog createInstance(BaseMessage message, CUserInfo userInfo) {
        CammentDialog dialog = new UserUnblockDialog();
        Bundle args = getBaseMessageArgs(message);
        args.putParcelable(ARGS_USER_INFO, userInfo);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    ActionListener getActionListener() {
        CUserInfo userInfo = getArguments().getParcelable(ARGS_USER_INFO);
        return new UserUnblockDialogActionListener(userInfo);
    }

    @Override
    String getDialogTag() {
        return TAG;
    }
}
