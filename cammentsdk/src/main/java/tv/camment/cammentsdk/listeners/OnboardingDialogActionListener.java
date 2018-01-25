package tv.camment.cammentsdk.listeners;

import org.greenrobot.eventbus.EventBus;

import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.events.OnboardingEvent;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class OnboardingDialogActionListener implements CammentDialog.ActionListener {

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        EventBus.getDefault().post(new OnboardingEvent(true));
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {
        EventBus.getDefault().post(new OnboardingEvent(false));
    }

}
