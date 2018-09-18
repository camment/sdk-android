package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.LoginDialogActionListener;

public final class LoginCammentDialog extends CammentDialog {

    public static final String TAG = "tag_login_confirmation";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new LoginCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new LoginDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
