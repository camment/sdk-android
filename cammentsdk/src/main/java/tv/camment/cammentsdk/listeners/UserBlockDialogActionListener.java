package tv.camment.cammentsdk.listeners;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class UserBlockDialogActionListener implements CammentDialog.ActionListener {

    private final CUserInfo userInfo;

    public UserBlockDialogActionListener(CUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        ApiManager.getInstance().getInvitationApi().blockUser(userInfo.getUserCognitoIdentityId(), userInfo.getGroupUuid());
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
