package tv.camment.cammentsdk.events;


import android.text.TextUtils;

import com.camment.clientsdk.model.Userinfo;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.helpers.SnackbarType;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.views.CammentPlayerListener;

public final class UserGroupChangeEvent {

    public UserGroupChangeEvent() {
        CUserGroup activeUserGroup = UserGroupProvider.getActiveUserGroupWithUserInfo();

        if (activeUserGroup != null
                && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
            ApiManager.getInstance().getGroupApi().setMyActiveGroup(activeUserGroup.getUuid());

            if (CammentSDK.getInstance().isSyncEnabled()) {
                SyncHelper.getInstance().startPeriodicCammentCheck();

                String identityId = IdentityPreferences.getInstance().getIdentityId();
                if (!TextUtils.equals(identityId, activeUserGroup.getHostId())) {
                    //current user is not the host
                    SyncHelper.getInstance().endPeriodicPositionUpdate();

                    if (activeUserGroup.getUsers() != null) {
                        for (Userinfo userinfo : activeUserGroup.getUsers()) {
                            if (TextUtils.equals(userinfo.getUserCognitoIdentityId(), activeUserGroup.getHostId())) {
                                if (!TextUtils.isEmpty(userinfo.getName())) {
                                    SnackbarQueueHelper.Snackbar snackbar = new SnackbarQueueHelper.Snackbar(SnackbarType.SYNCING_WITH_HOST, SnackbarQueueHelper.SHORT);
                                    snackbar.setMsgVar(userinfo.getName());

                                    SnackbarQueueHelper.getInstance().addSnackbar(snackbar);
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
                //current user is the host
                if (CammentSDK.getInstance().isSyncEnabled()) {
                    CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();
                    if (cammentPlayerListener != null) {
                        SyncHelper.getInstance().onPlaybackPositionChanged(cammentPlayerListener.getCurrentPosition(), cammentPlayerListener.isPlaying());
                    }
                    SyncHelper.getInstance().startPeriodicPositionUpdate();
                }
            }
        } else {
            if (CammentSDK.getInstance().isSyncEnabled()) {
                SyncHelper.getInstance().endPeriodicCammentCheck();

                ApiManager.getInstance().getGroupApi().deleteMyActiveGroup();
            }
        }
    }
}
