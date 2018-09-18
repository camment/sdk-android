package tv.camment.cammentsdk.listeners;

import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class LeaveDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.LEAVE_CONFIRMATION) {
            String userId = IdentityPreferences.getInstance().getIdentityId();
            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
            String groupUuid = null;
            if (activeUserGroup != null) {
                groupUuid = activeUserGroup.getUuid();
            }

            if (!TextUtils.isEmpty(userId)
                    && !TextUtils.isEmpty(groupUuid)) {
                ApiManager.getInstance().getInvitationApi().removeUserFromGroup(userId, groupUuid);
            }
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
