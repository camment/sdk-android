package tv.camment.cammentsdk.listeners;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.views.dialogs.CammentDialog;


public final class ShareDialogActionListener implements CammentDialog.ActionListener {

    private final String url;

    public ShareDialogActionListener(String url) {
        this.url = url;
    }

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.INVITE);

        Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
        if (currentActivity != null) {
            String msg;
            String customInvitationText = GeneralPreferences.getInstance().getInvitationText();

            if (TextUtils.isEmpty(customInvitationText)) {
                msg = currentActivity.getString(R.string.cmmsdk_invitation_default_msg);
            } else {
                msg = customInvitationText;
            }

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, msg + " " + url);
            intent.setType("text/plain");

            currentActivity.startActivity(Intent.createChooser(intent, currentActivity.getString(R.string.cmmsdk_invitation_sharing_options)));
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

}
