package tv.camment.cammentsdk.listeners;

import android.os.Handler;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.helpers.SnackbarType;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class InvitationDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        InvitationMessage invitationMessage = (InvitationMessage) baseMessage;

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null) {
            UserGroupProvider.setActive(activeUserGroup.getUuid(), false);
        }

        UserGroupProvider.setActive(invitationMessage.body.groupUuid, true);

        EventBus.getDefault().post(new UserGroupChangeEvent());

        ApiManager.getInstance().getInvitationApi().sendInvitationForDeeplink(invitationMessage.body.groupUuid, invitationMessage.body.showUuid, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SnackbarQueueHelper.getInstance().addSnackbar(new SnackbarQueueHelper.Snackbar(SnackbarType.YOU_JOINED_GROUP, SnackbarQueueHelper.SHORT));
            }
        }, 1000);
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
