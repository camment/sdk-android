package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.LoginConfirmationDialogActionListener;

public final class LoginConfirmationCammentDialog extends CammentDialog {

    public static final String TAG = "tag_login_confirmation";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new LoginConfirmationCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new LoginConfirmationDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
