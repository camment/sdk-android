package tv.camment.cammentsdk.api;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriend;
import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.NewUserInGroupMessage;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.dialogs.FirstUserJoinedCammentDialog;


public final class GroupApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    GroupApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createEmptyUsergroupIfNeededAndGetDeeplink() {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            ApiManager.getInstance().getInvitationApi().getDeeplinkToShare();
        } else {
            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    return devcammentClient.usergroupsPost();
                }
            }, createEmptyUsergroupInvitationCallback());
        }
    }

    @Deprecated
    public void createEmptyUsergroupIfNeededAndSendInvitation(final List<FacebookFriend> fbFriends,
                                                              final CammentCallback<Object> sendInvitationCallback) {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            ApiManager.getInstance().getInvitationApi().sendInvitation(sendInvitationCallback);
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
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            camment.setUserGroupUuid(usergroup.getUuid());

            CammentProvider.updateCammentGroupId(camment, usergroup.getUuid());

            runOnUiThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
                }
            }, 250);
        } else {
            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    return devcammentClient.usergroupsPost();
                }
            }, createEmptyUsergroupUploadCallback(camment));
        }
    }

    private CammentCallback<Usergroup> createEmptyUsergroupInvitationCallback() {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(usergroup, true);

                ApiManager.getInstance().getInvitationApi().getDeeplinkToShare();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

    private CammentCallback<Usergroup> createEmptyUsergroupInvitationCallback(final List<FacebookFriend> fbFriends,
                                                                              final CammentCallback<Object> sendInvitationCallback) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(usergroup, true);

                ApiManager.getInstance().getInvitationApi().sendInvitation(sendInvitationCallback);
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
                LogUtils.debug("onSuccess", "createEmptyUsergroup " + usergroup.getUuid());

                UserGroupProvider.insertUserGroup(usergroup, true);

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

    public void getUserGroupByUuidAndSetAsActive(final String uuid) {
        submitTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                return devcammentClient.usergroupsGroupUuidGet(uuid);
            }
        }, getUserGroupByUuidCallback());
    }

    private CammentCallback<Usergroup> getUserGroupByUuidCallback() {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                LogUtils.debug("onSuccess", "getUserGroupByUuid");
                UserGroupProvider.insertUserGroup(usergroup, true);

                //ApiManager.getInstance().getCammentApi().getUserGroupCamments();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupByUuid", exception);
            }
        };
    }

    public void getUserGroupByUuid(final String uuid, BaseMessage message) {
        submitTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                return devcammentClient.usergroupsGroupUuidGet(uuid);
            }
        }, getUserGroupByUuidCallback(message));
    }

    private CammentCallback<Usergroup> getUserGroupByUuidCallback(final BaseMessage message) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                if (usergroup == null)
                    return;

                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                boolean isCurrentGroup = activeUserGroup != null && TextUtils.equals(activeUserGroup.getUuid(), usergroup.getUuid());

                if (TextUtils.equals(usergroup.getUserCognitoIdentityId(), IdentityPreferences.getInstance().getIdentityId())) {
                    if (!isCurrentGroup) {
                        UserGroupProvider.insertUserGroup(usergroup, true);
                    }

                    if (message instanceof InvitationMessage) {
                        final String showUuid = ((InvitationMessage) message).body.showUuid;
                        final OnDeeplinkOpenShowListener onDeeplinkOpenShowListener = CammentSDK.getInstance().getOnDeeplinkOpenShowListener();
                        if (onDeeplinkOpenShowListener != null
                                && !TextUtils.isEmpty(showUuid)) {
                            onDeeplinkOpenShowListener.onOpenShowWithUuid(showUuid);
                        }
                    } else if (message instanceof NewUserInGroupMessage) {
                        final String showUuid = ((NewUserInGroupMessage) message).body.showUuid;
                        final OnDeeplinkOpenShowListener onDeeplinkOpenShowListener = CammentSDK.getInstance().getOnDeeplinkOpenShowListener();
                        if (onDeeplinkOpenShowListener != null
                                && !TextUtils.isEmpty(showUuid)) {
                            onDeeplinkOpenShowListener.onOpenShowWithUuid(showUuid);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int connectedUsersCountByGroupUuid = UserInfoProvider.getConnectedUsersCountByGroupUuid(((NewUserInGroupMessage) message).body.groupUuid);
                                if (connectedUsersCountByGroupUuid == 1
                                        && TextUtils.equals(((NewUserInGroupMessage) message).body.groupOwnerCognitoIdentityId, IdentityPreferences.getInstance().getIdentityId())) {
                                    message.type = MessageType.FIRST_USER_JOINED;
                                    FirstUserJoinedCammentDialog.createInstance(message).show();
                                }
                            }
                        }, 1000);
                    }

                    if (message instanceof InvitationMessage) {
                        Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                                R.string.cmmsdk_joined_private_chat, Toast.LENGTH_LONG).show();
                    }

                    CammentSDK.getInstance().hideProgressBar();
                    return;
                }

                UserGroupProvider.insertUserGroup(usergroup, false);
                AWSManager.getInstance().getIoTHelper().showInvitationDialog(message);
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupByUuid", exception);
                CammentSDK.getInstance().hideProgressBar();
            }
        };
    }

}