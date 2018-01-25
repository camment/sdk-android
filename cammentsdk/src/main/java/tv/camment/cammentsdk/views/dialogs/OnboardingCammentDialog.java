package tv.camment.cammentsdk.views.dialogs;


import android.os.Bundle;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.listeners.OnboardingDialogActionListener;

public final class OnboardingCammentDialog extends CammentDialog {

    public static final String TAG = "tag_onboarding";

    public static CammentDialog createInstance(BaseMessage message) {
        CammentDialog dialog = new OnboardingCammentDialog();
        Bundle args = getBaseMessageArgs(message);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    ActionListener getActionListener() {
        return new OnboardingDialogActionListener();
    }

    @Override
    String getDialogTag() {
        return TAG;
    }

}
