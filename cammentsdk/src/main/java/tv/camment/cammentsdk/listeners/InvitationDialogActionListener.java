package tv.camment.cammentsdk.listeners;

import android.widget.Toast;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class InvitationDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.JOIN_GROUP);

        InvitationMessage invitationMessage = (InvitationMessage) baseMessage;

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null) {
            UserGroupProvider.setActive(activeUserGroup.getUuid(), false);
        }

        UserGroupProvider.setActive(invitationMessage.body.groupUuid, true);

        EventBus.getDefault().post(new UserGroupChangeEvent());

        //ApiManager.getInstance().getCammentApi().getUserGroupCamments();

        ApiManager.getInstance().getInvitationApi().sendInvitationForDeeplink(invitationMessage.body.groupUuid, invitationMessage.body.showUuid);

        Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                R.string.cmmsdk_joined_private_chat, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
