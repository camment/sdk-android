package tv.camment.cammentsdk.api;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.ShowUuid;
import com.camment.clientsdk.model.UpdateUserStateInGroupRequest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.dialogs.ShareCammentDialog;


public final class InvitationApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    InvitationApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    @Deprecated
    void sendInvitation(final CammentCallback<Object> sendInvitationCallback) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final String userGroupUuid = UserGroupProvider.getActiveUserGroup().getUuid();

                devcammentClient.usergroupsGroupUuidUsersPost(userGroupUuid, null);

                return new Object();
            }
        }, sendInvitationCallback);
    }

    public void sendInvitationForDeeplink(final String groupUuid, final String showUuid) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ShowUuid showuuid = new ShowUuid();
                showuuid.setShowUuid(showUuid);
                devcammentClient.usergroupsGroupUuidUsersPost(groupUuid, showuuid);

                return new Object();
            }
        }, sendInvitationForDeeplinkCallback(showUuid));
    }

    private CammentCallback<Object> sendInvitationForDeeplinkCallback(final String showUuid) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                final OnDeeplinkOpenShowListener onDeeplinkOpenShowListener = CammentSDK.getInstance().getOnDeeplinkOpenShowListener();
                if (onDeeplinkOpenShowListener != null
                        && !TextUtils.isEmpty(showUuid)) {
                    onDeeplinkOpenShowListener.onOpenShowWithUuid(showUuid);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "sendInvitationForDeeplink", exception);
            }
        };
    }

    void getDeeplinkToShare() {
        CammentSDK.getInstance().showProgressBar();

        submitTask(new Callable<Deeplink>() {
            @Override
            public Deeplink call() throws Exception {
                final String showUuid = GeneralPreferences.getInstance().getActiveShowUuid();
                final String userGroupUuid = UserGroupProvider.getActiveUserGroup().getUuid();

                ShowUuid show = new ShowUuid();
                show.setShowUuid(showUuid);

                return devcammentClient.usergroupsGroupUuidDeeplinkPost(userGroupUuid, show);
            }
        }, getDeeplinkToShareCallback());
    }

    private CammentCallback<Deeplink> getDeeplinkToShareCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(final Deeplink result) {
                CammentSDK.getInstance().hideProgressBar();

                if (result != null
                        && !TextUtils.isEmpty(result.getUrl())) {
                    BaseMessage message = new BaseMessage();
                    message.type = MessageType.SHARE;

                    ShareCammentDialog.createInstance(message, result).show();
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getDeeplinkToShare", exception);
                CammentSDK.getInstance().hideProgressBar();
            }
        };
    }

    public void getDeferredDeepLink(CammentCallback<Deeplink> deferredDeepLinkCallback) {
        submitBgTask(new Callable<Deeplink>() {
            @Override
            public Deeplink call() throws Exception {
                String androidVersion = Build.VERSION.RELEASE;

                StringBuilder sb = new StringBuilder();
                sb.append("Android");
                sb.append("|");
                sb.append(TextUtils.isEmpty(androidVersion) ? "" : androidVersion);

                String md5 = DeeplinkUtils.calculateMD5(sb.toString());

                return devcammentClient.deferredDeeplinkGet(md5, "Android");
            }
        }, deferredDeepLinkCallback);
    }

    public void removeUserFromGroup(final CUserInfo userInfo) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.usergroupsGroupUuidUsersUserIdDelete(userInfo.getUserCognitoIdentityId(), userInfo.getGroupUuid());
                return new Object();
            }
        }, removeUserFromGroupCallback(userInfo));
    }

    private CammentCallback<Object> removeUserFromGroupCallback(final CUserInfo userInfo) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "removeUserFromGroup");
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "removeUserFromGroup", exception);

                Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                        String.format(CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_fail_to_remove_from_group), userInfo.getName()),
                        Toast.LENGTH_LONG)
                        .show();

                UserInfoProvider.insertUserInfo(userInfo, userInfo.getGroupUuid());
            }
        };
    }

    public void blockUser(final String userUuid, final String groupUuid) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UpdateUserStateInGroupRequest updateRequest = new UpdateUserStateInGroupRequest();
                updateRequest.setState(UserState.BLOCKED.getStringValue());

                devcammentClient.usergroupsGroupUuidUsersUserIdPut(userUuid, groupUuid, updateRequest);
                return new Object();
            }
        }, blockUserCallback());
    }

    private CammentCallback<Object> blockUserCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "blockUser");
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "blockUser", exception);
            }
        };
    }

    public void unblockUser(final String userUuid, final String groupUuid) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UpdateUserStateInGroupRequest updateRequest = new UpdateUserStateInGroupRequest();
                updateRequest.setState(UserState.ACTIVE.getStringValue());

                devcammentClient.usergroupsGroupUuidUsersUserIdPut(userUuid, groupUuid, updateRequest);
                return new Object();
            }
        }, unblockUserCallback());
    }

    private CammentCallback<Object> unblockUserCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("onSuccess", "unblockUser");
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "unblockUser", exception);
            }
        };
    }

}
