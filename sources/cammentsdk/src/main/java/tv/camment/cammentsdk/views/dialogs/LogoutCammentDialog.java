package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.LogoutDialogActionListener;

public final class LogoutCammentDialog extends CammentDialog {

    public static final String TAG = "tag_logout_confirmation";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new LogoutCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new LogoutDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
