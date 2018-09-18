package tv.camment.cammentsdk.api;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.GroupUuidInRequest;
import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.UsergroupInRequest;
import com.camment.clientsdk.model.Userinfo;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.events.HideSofaInviteProgress;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.helpers.SnackbarType;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.dialogs.BlockedCammentDialog;


public final class GroupApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    GroupApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createEmptyUsergroup() {
        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.CREATE_GROUP);

        submitBgTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                UsergroupInRequest request = new UsergroupInRequest();
                request.setShowId(CammentSDK.getInstance().getShowMetadata().getUuid());
                request.setIsPublic(false);

                return devcammentClient.usergroupsPost(request);
            }
        }, createEmptyUsergroupCallback());
    }

    private CammentCallback<Usergroup> createEmptyUsergroupCallback() {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(setUserinfoIfMissing(usergroup), true, usergroup.getShowId());

                ApiManager.getInstance().getInvitationApi().getDeeplinkToShare();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

    private Usergroup setUserinfoIfMissing(Usergroup usergroup) {
        if (usergroup.getUsers() == null
                || usergroup.getUsers().size() == 0) {
            CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();

            Userinfo userinfo = new Userinfo();
            userinfo.setUserCognitoIdentityId(IdentityPreferences.getInstance().getIdentityId());
            userinfo.setState(UserState.ACTIVE.getStringValue());
            userinfo.setIsOnline(true);
            userinfo.setActiveGroup(usergroup.getUuid());

            if (userInfo != null) {
                userinfo.setName(userInfo.getName());
                userinfo.setPicture(userInfo.getImageUrl());
            }

            usergroup.setUsers(Collections.singletonList(userinfo));
        }

        return usergroup;
    }

    public void createEmptyUsergroupIfNeededAndGetDeeplink() {
        Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup != null && !TextUtils.isEmpty(usergroup.getUuid())) {
            ApiManager.getInstance().getInvitationApi().getDeeplinkToShare();
        } else {
            MixpanelHelper.getInstance().trackEvent(MixpanelHelper.CREATE_GROUP);

            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    UsergroupInRequest request = new UsergroupInRequest();
                    request.setShowId(CammentSDK.getInstance().getShowMetadata().getUuid());
                    request.setIsPublic(false);

                    return devcammentClient.usergroupsPost(request);
                }
            }, createEmptyUsergroupInvitationCallback());
        }
    }

    public void createEmptyUsergroupIfNeededAndUploadCamment(final CCamment camment) {
        long cammentDuration = camment.getEndTimestamp() - camment.getStartTimestamp();

        if (cammentDuration < SDKConfig.CAMMENT_MIN_DURATION) {
            LogUtils.debug("cammentDuration", "video too short (< 1000 ms)");
            return;
        }

        CammentProvider.setRecorded(camment, true);

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
            MixpanelHelper.getInstance().trackEvent(MixpanelHelper.CREATE_GROUP);

            submitBgTask(new Callable<Usergroup>() {
                @Override
                public Usergroup call() throws Exception {
                    UsergroupInRequest request = new UsergroupInRequest();
                    request.setShowId(CammentSDK.getInstance().getShowMetadata().getUuid());
                    request.setIsPublic(false);

                    return devcammentClient.usergroupsPost(request);
                }
            }, createEmptyUsergroupUploadCallback(camment));
        }
    }

    private CammentCallback<Usergroup> createEmptyUsergroupInvitationCallback() {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(setUserinfoIfMissing(usergroup), true, usergroup.getShowId());

                ApiManager.getInstance().getUserApi().getMyUserGroups();

                ApiManager.getInstance().getInvitationApi().getDeeplinkToShare();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);

                EventBus.getDefault().post(new HideSofaInviteProgress());
            }
        };
    }

    private CammentCallback<Usergroup> createEmptyUsergroupUploadCallback(final CCamment camment) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                LogUtils.debug("onSuccess", "createEmptyUsergroup " + usergroup.getUuid());

                UserGroupProvider.insertUserGroup(setUserinfoIfMissing(usergroup), true, usergroup.getShowId());

                camment.setUserGroupUuid(usergroup.getUuid());

                CammentProvider.updateCammentGroupId(camment, usergroup.getUuid());

                AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);

                if (AuthHelper.getInstance().isLoggedIn()) {
                    ApiManager.getInstance().getUserApi().getMyUserGroups();
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

    public void getUserGroupByUuid(final String uuid) {
        submitTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                return devcammentClient.usergroupsGroupUuidGet(uuid);
            }
        }, getUserGroupByUuidCallback(uuid));
    }

    private CammentCallback<Usergroup> getUserGroupByUuidCallback(final String uuid) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                UserGroupProvider.insertUserGroup(usergroup, false, usergroup.getShowId());
            }

            @Override
            public void onException(Exception exception) {
                if (exception instanceof ApiClientException
                        && ((ApiClientException) exception).getStatusCode() == 403) {
                    Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                    if (activeUserGroup != null
                            && TextUtils.equals(activeUserGroup.getUuid(), uuid)) {
                        DataManager.getInstance().clearDataForUserGroupChange();

                        EventBus.getDefault().post(new UserGroupChangeEvent());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BaseMessage msg = new BaseMessage();
                            msg.type = MessageType.BLOCKED;

                            BlockedCammentDialog.createInstance(msg).show();
                        }
                    });
                } else {
                    Log.e("onException", "getUserGroupByUuid", exception);
                }
            }
        };
    }

    public void getUserGroupByUuidWithGroupChange(final String uuid, BaseMessage message, boolean changeGroup) {
        submitTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                return devcammentClient.usergroupsGroupUuidGet(uuid);
            }
        }, getUserGroupByUuidWithGroupChangeCallback(message, changeGroup));
    }

    private CammentCallback<Usergroup> getUserGroupByUuidWithGroupChangeCallback(final BaseMessage message, final boolean changeGroup) {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                if (usergroup == null)
                    return;

                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
                boolean isCurrentGroup = activeUserGroup != null && TextUtils.equals(activeUserGroup.getUuid(), usergroup.getUuid());

                if (TextUtils.equals(usergroup.getUserCognitoIdentityId(), IdentityPreferences.getInstance().getIdentityId())) {
                    if (!isCurrentGroup) {
                        UserGroupProvider.insertUserGroup(usergroup, changeGroup, usergroup.getShowId());
                    }

                    if (message instanceof InvitationMessage) {
                        final String showUuid = ((InvitationMessage) message).body.showUuid;
                        final OnDeeplinkOpenShowListener onDeeplinkOpenShowListener = CammentSDK.getInstance().getOnDeeplinkOpenShowListener();
                        if (onDeeplinkOpenShowListener != null
                                && !TextUtils.isEmpty(showUuid)) {
                            onDeeplinkOpenShowListener.onOpenShowWithUuid(showUuid);
                        }
                    }

                    if (message instanceof InvitationMessage) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SnackbarQueueHelper.getInstance().addSnackbar(new SnackbarQueueHelper.Snackbar(SnackbarType.YOU_JOINED_GROUP, SnackbarQueueHelper.SHORT));
                            }
                        }, 1000);
                    }

                    CammentSDK.getInstance().hideProgressBar();
                    return;
                }

                UserGroupProvider.insertUserGroup(usergroup, false, usergroup.getShowId());
                AWSManager.getInstance().getIoTHelper().showInvitationDialog(message);
            }

            @Override
            public void onException(Exception exception) {
                if (exception instanceof ApiClientException
                        && ((ApiClientException) exception).getStatusCode() == 403) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CammentSDK.getInstance().hideProgressBar();

                            BaseMessage msg = new BaseMessage();
                            msg.type = MessageType.BLOCKED;

                            BlockedCammentDialog.createInstance(msg).show();
                        }
                    });
                } else {
                    Log.e("onException", "getUserGroupByUuidWithGroupChange", exception);
                }
                CammentSDK.getInstance().hideProgressBar();
            }
        };
    }

    public void setMyActiveGroup(final String groupUuid) {
        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.JOIN_GROUP);

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                GroupUuidInRequest groupUuidInRequest = new GroupUuidInRequest();
                groupUuidInRequest.setGroupUuid(groupUuid);

                devcammentClient.meActiveGroupPost(groupUuidInRequest);
                return new Object();
            }
        }, getSetMyActiveGroupCallback());
    }

    private CammentCallback<Object> getSetMyActiveGroupCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "setMyActiveGroup");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "setMyActiveGroup", exception);
            }
        };
    }

    public void deleteMyActiveGroup() {
        SyncHelper.getInstance().endPeriodicPositionUpdate();

        if (!AuthHelper.getInstance().isLoggedIn()) {
            return;
        }

        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.meActiveGroupDelete();
                return new Object();
            }
        }, getDeleteMyActiveGroupCallback());
    }

    private CammentCallback<Object> getDeleteMyActiveGroupCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "deleteMyActiveGroup");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "deleteMyActiveGroup", exception);
            }
        };
    }

}