package tv.camment.cammentsdk.listeners;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class RemovalConfirmationDialogActionListener implements CammentDialog.ActionListener {

    private final CUserInfo userInfo;

    public RemovalConfirmationDialogActionListener(CUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.REMOVAL_CONFIRMATION) {
            UserInfoProvider.deleteUserInfoByIdentityId(userInfo.getUserCognitoIdentityId(), userInfo.getGroupUuid());

            ApiManager.getInstance().getInvitationApi().removeUserFromGroup(userInfo);
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
