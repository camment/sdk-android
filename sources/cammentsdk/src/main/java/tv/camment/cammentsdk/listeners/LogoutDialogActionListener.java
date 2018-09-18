package tv.camment.cammentsdk.listeners;

import org.greenrobot.eventbus.EventBus;

import java.util.ConcurrentModificationException;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.events.HideUserInfoContainerEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class LogoutDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.LOGOUT_CONFIRMATION) {
            CammentSDK.getInstance().getAppAuthIdentityProvider().logOut();

            ApiManager.clearInstance();

            try {
                AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();

                AWSManager.getInstance().getCognitoCachingCredentialsProvider().clear();
            } catch (ConcurrentModificationException e) {
                LogUtils.debug("onException", "clear ConcurrentModificationException");
            }

            ApiManager.getInstance().getUserApi().retrieveNewIdentityId();

            DataManager.getInstance().clearDataForLogOut();

            EventBus.getDefault().post(new UserGroupChangeEvent());

            AWSManager.getInstance().getIoTHelper().subscribe();

            EventBus.getDefault().post(new HideUserInfoContainerEvent());
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
