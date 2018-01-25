package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import com.camment.clientsdk.model.Deeplink;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.ShareDialogActionListener;

public final class ShareCammentDialog extends CammentDialog {

    public static final String TAG = "tag_share";

    private static final String ARGS_DEEPLINK_URL = "args_deeplink_url";

    public static CammentDialog createInstance(BaseMessage message, Deeplink deeplink) {
        CammentDialog dialog = new ShareCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        args.putString(ARGS_DEEPLINK_URL, deeplink.getUrl());
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new ShareDialogActionListener(getArguments().getString(ARGS_DEEPLINK_URL));
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
