package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.LeaveDialogActionListener;

public final class LeaveDialog extends CammentDialog {

    public static final String TAG = "tag_leave_confirmation";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new LeaveDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new LeaveDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
