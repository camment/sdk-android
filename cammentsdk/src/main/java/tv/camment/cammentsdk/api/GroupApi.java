package tv.camment.cammentsdk.api;

import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriend;
import com.camment.clientsdk.model.Usergroup;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;


public final class GroupApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    GroupApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createEmptyUsergroupIfNeededAndSendInvitation(final List<FacebookFriend> fbFriends,
                                                              final CammentCallback<Object> sendInvitationCallback) {
        Usergroup usergroup = UserGroupProvider.getUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            if (BuildConfig.USE_DEEPLINK) {
                ApiManager.getInstance().getInvitationApi().getDeeplinkToShare(fbFriends);
            } else {
                ApiManager.getInstance().getInvitationApi().sendInvitation(fbFriends, sendInvitationCallback);
            }
        } else {
            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    return devcammentClient.usergroupsPost();
                }
            }, createEmptyUsergroupInvitationCallback(fbFriends, sendInvitationCallback));
        }
    }

    public void createEmptyUsergroupIfNeededAndUploadCamment(final CCamment camment) {
        Usergroup usergroup = UserGroupProvider.getUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            camment.setUserGroupUuid(usergroup.getUuid());

            CammentProvider.updateCammentGroupId(camment, usergroup.getUuid());

            AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
        } else {
            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    return devcammentClient.usergroupsPost();
                }
            }, createEmptyUsergroupUploadCallback(camment));
        }
    }

    private CammentCallback<Usergroup> createEmptyUsergroupInvitationCallback(final List<FacebookFriend> fbFriends,
                                                                              final CammentCallback<Object> sendInvitationCallback) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(usergroup);
                if (BuildConfig.USE_DEEPLINK) {
                    ApiManager.getInstance().getInvitationApi().getDeeplinkToShare(fbFriends);
                } else {
                    ApiManager.getInstance().getInvitationApi().sendInvitation(fbFriends, sendInvitationCallback);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

    private CammentCallback<Usergroup> createEmptyUsergroupUploadCallback(final CCamment camment) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(usergroup);

                camment.setUserGroupUuid(usergroup.getUuid());

                CammentProvider.updateCammentGroupId(camment, usergroup.getUuid());

                AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

}
