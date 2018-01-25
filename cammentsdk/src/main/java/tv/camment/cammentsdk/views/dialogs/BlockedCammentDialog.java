package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;

public final class BlockedCammentDialog extends CammentDialog {

    public static final String TAG = "tag_blocked";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new BlockedCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return null;
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
