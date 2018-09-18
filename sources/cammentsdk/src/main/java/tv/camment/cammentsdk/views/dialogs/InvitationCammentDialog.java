package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.InvitationDialogActionListener;

public final class InvitationCammentDialog extends CammentDialog {

    public static final String TAG = "tag_invitation";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new InvitationCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new InvitationDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
