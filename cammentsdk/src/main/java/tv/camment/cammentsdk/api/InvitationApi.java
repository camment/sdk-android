package tv.camment.cammentsdk.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.ShowUuid;
import com.camment.clientsdk.model.Usergroup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.views.CammentDialog;


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

                    Activity activity = CammentSDK.getInstance().getCurrentActivity();

                    CammentDialog cammentDialog = CammentDialog.createInstance(message);
                    cammentDialog.setActionListener(new CammentDialog.ActionListener() {
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
                                intent.putExtra(Intent.EXTRA_TEXT, msg + " " + result.getUrl());
                                intent.setType("text/plain");

                                currentActivity.startActivity(Intent.createChooser(intent, currentActivity.getString(R.string.cmmsdk_invitation_sharing_options)));
                            }
                        }

                        @Override
                        public void onNegativeButtonClick(BaseMessage baseMessage) {

                        }
                    });
                    cammentDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), message.toString());
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

    public void replyToMembershipRequest(final String userId, final String groupUuid, final String showUuid, final boolean accept) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (accept) {
                    devcammentClient.usergroupsGroupUuidUsersUserIdPut(userId, groupUuid, showUuid);
                } else {
                    devcammentClient.usergroupsGroupUuidUsersUserIdDelete(userId, groupUuid);
                }
                return new Object();
            }
        }, replyToMembershipRequestCallback(groupUuid, showUuid, accept));
    }

    private CammentCallback<Object> replyToMembershipRequestCallback(final String groupUuid, final String showUuid, final boolean accepted) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                if (accepted) {
                    Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

                    if (activeUserGroup == null
                            || !TextUtils.equals(activeUserGroup.getUuid(), groupUuid)) {

                        DataManager.getInstance().clearDataForUserGroupChange();

                        CUserGroup userGroupByUuid = UserGroupProvider.getUserGroupByUuid(groupUuid); //TODO what if not in db

                        if (userGroupByUuid != null) {
                            UserGroupProvider.setActive(userGroupByUuid.getUuid(), true);

                            ApiManager.getInstance().getCammentApi().getUserGroupCamments();
                        } else {
                            ApiManager.getInstance().getGroupApi().getUserGroupByUuidAndSetAsActive(groupUuid);
                        }
                    }

                    String activeShowUuid = GeneralPreferences.getInstance().getActiveShowUuid();

                    if (!TextUtils.equals(activeShowUuid, showUuid)) {
                        final OnDeeplinkOpenShowListener onDeeplinkOpenShowListener = CammentSDK.getInstance().getOnDeeplinkOpenShowListener();
                        if (onDeeplinkOpenShowListener != null
                                && !TextUtils.isEmpty(showUuid)) {
                            onDeeplinkOpenShowListener.onOpenShowWithUuid(showUuid);
                        }
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "replyToMembershipRequest", exception);
            }
        };
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
                Log.d("onSuccess", "removeUserFromGroup");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "removeUserFromGroup", exception);

                Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                        String.format(CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_fail_to_remove_from_group), userInfo.getName()),
                        Toast.LENGTH_LONG)
                        .show();

                UserInfoProvider.insertUserInfo(userInfo, userInfo.getGroupUuid());
            }
        };
    }

}
