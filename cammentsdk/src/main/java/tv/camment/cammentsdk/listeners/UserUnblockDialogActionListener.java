package tv.camment.cammentsdk.listeners;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class UserUnblockDialogActionListener implements CammentDialog.ActionListener {

    private final CUserInfo userInfo;

    public UserUnblockDialogActionListener(CUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        ApiManager.getInstance().getInvitationApi().unblockUser(userInfo.getUserCognitoIdentityId(), userInfo.getGroupUuid());
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }
    
}
