package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import com.camment.clientsdk.model.Deeplink;

import tv.camment.cammentsdk.aws.messages.BaseMessage;

public final class FirstUserJoinedCammentDialog extends CammentDialog {

    public static final String TAG = "tag_first_user_joined";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new FirstUserJoinedCammentDialog();
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
