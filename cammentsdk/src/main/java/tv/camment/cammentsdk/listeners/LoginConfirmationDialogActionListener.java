package tv.camment.cammentsdk.listeners;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class LoginConfirmationDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.LOGIN_CONFIRMATION) {
            PendingActions.getInstance().addAction(PendingActions.Action.HANDLE_DEEPLINK);
            CammentSDK.getInstance().getAppAuthIdentityProvider().logIn(CammentSDK.getInstance().getCurrentActivity());
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {
        GeneralPreferences.getInstance().setDeeplinkGroupUuid("");
        GeneralPreferences.getInstance().setDeeplinkShowUuid("");

        CammentSDK.getInstance().hideProgressBar();
    }

}
