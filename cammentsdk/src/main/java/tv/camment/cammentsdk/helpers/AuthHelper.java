package tv.camment.cammentsdk.helpers;


import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.util.Date;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.views.CammentDialog;

public final class AuthHelper implements CammentDialog.ActionListener {

    private CammentAuthInfo authInfo;

    private static AuthHelper INSTANCE;

    public static AuthHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthHelper();
        }
        return INSTANCE;
    }

    public boolean isLoggedIn() {
        return isHostAppLoggedIn()
                && !TextUtils.isEmpty(AWSManager.getInstance().getCammentAuthenticationProvider().getToken())
                && !TextUtils.isEmpty(IdentityPreferences.getInstance().getIdentityId());
    }

    public boolean isHostAppLoggedIn() {
        if (authInfo != null && authInfo.getAuthType() != null) {
            switch (authInfo.getAuthType()) {
                case FACEBOOK:
                    CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) authInfo;
                    return !TextUtils.isEmpty(fbAuthInfo.getToken())
                            && fbAuthInfo.getExpires().after(new Date());
            }
        }
        return false;
    }

    public String getUserId() {
        CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();
        String userId = "";
        if (userInfo != null) {
            switch (userInfo.getAuthType()) {
                case FACEBOOK:
                    userId = ((CammentFbUserInfo) userInfo).getFacebookUserId();
                    break;
            }
        }
        return userId;
    }

    public void setAuthInfo(CammentAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public CammentAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void checkLogin(PendingActions.Action action) {
        if (isHostAppLoggedIn()) {
            if (action != null
                    && action == PendingActions.Action.HANDLE_DEEPLINK) {
                PendingActions.getInstance().addAction(action);
            }
            ApiManager.getInstance().getAuthApi().logIn();
        } else {
            if (action != null
                    && action == PendingActions.Action.HANDLE_DEEPLINK) {
                BaseMessage message = new BaseMessage();
                message.type = MessageType.LOGIN_CONFIRMATION;

                CammentDialog cammentDialog = CammentDialog.createInstance(message);
                cammentDialog.setActionListener(this);

                cammentDialog.show(message.toString());
            } else {
                CammentSDK.getInstance().getAppAuthIdentityProvider().logIn(CammentSDK.getInstance().getCurrentActivity());
            }
        }
    }

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
