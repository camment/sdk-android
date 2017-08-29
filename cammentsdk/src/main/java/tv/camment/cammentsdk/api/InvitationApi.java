package tv.camment.cammentsdk.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.AcceptInvitationRequest;
import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.FacebookFriend;
import com.camment.clientsdk.model.UserFacebookIdListInRequest;
import com.camment.clientsdk.model.Usergroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.helpers.GeneralPreferences;


public final class InvitationApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    InvitationApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    void sendInvitation(final List<FacebookFriend> fbFriends,
                        final CammentCallback<Object> sendInvitationCallback) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final String showUuid = ShowProvider.getShow().getUuid();
                final String userGroupUuid = UserGroupProvider.getUserGroup().getUuid();

                UserFacebookIdListInRequest userInAddToGroupRequest = new UserFacebookIdListInRequest();
                userInAddToGroupRequest.setShowUuid(showUuid);

                List<String> fbUserIdsStrings = new ArrayList<>();
                for (FacebookFriend fbFriend : fbFriends) {
                    fbUserIdsStrings.add(String.valueOf(fbFriend.getId()));
                }
                userInAddToGroupRequest.setUserFacebookIdList(fbUserIdsStrings);

                devcammentClient.usergroupsGroupUuidUsersPost(userGroupUuid, userInAddToGroupRequest);

                return new Object();
            }
        }, sendInvitationCallback);
    }

    public void acceptInvitation(final InvitationMessage invitationMessage) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AcceptInvitationRequest acceptInvitationRequest = new AcceptInvitationRequest();
                acceptInvitationRequest.setInvitationKey(invitationMessage.body.key);
                devcammentClient.usergroupsGroupUuidInvitationsPut(invitationMessage.body.groupUuid, acceptInvitationRequest);

                return new Object();
            }
        }, acceptInvitationCallback(invitationMessage));
    }

    private CammentCallback<Object> acceptInvitationCallback(final InvitationMessage invitationMessage) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                DataManager.getInstance().clearDataForUserGroupChange();

                Usergroup usergroup = new Usergroup();
                usergroup.setUuid(invitationMessage.body.groupUuid);

                UserGroupProvider.insertUserGroup(usergroup);

                ApiManager.getInstance().getCammentApi().getUserGroupCamments();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "acceptInvitation", exception);
            }
        };
    }

    public void getDeeplinkToShare(final List<FacebookFriend> fbFriends) {
        submitTask(new Callable<Deeplink>() {
            @Override
            public Deeplink call() throws Exception {
                final String showUuid = ShowProvider.getShow().getUuid();
                final String userGroupUuid = UserGroupProvider.getUserGroup().getUuid();

                UserFacebookIdListInRequest userInAddToGroupRequest = new UserFacebookIdListInRequest();
                userInAddToGroupRequest.setShowUuid(showUuid);

                List<String> fbUserIdsStrings = new ArrayList<>();
                for (FacebookFriend fbFriend : fbFriends) {
                    fbUserIdsStrings.add(String.valueOf(fbFriend.getId()));
                }
                userInAddToGroupRequest.setUserFacebookIdList(fbUserIdsStrings);

                return devcammentClient.usergroupsGroupUuidDeeplinkPost(userGroupUuid, userInAddToGroupRequest);
            }
        }, getDeeplinkToShareCalback());
    }

    private CammentCallback<Deeplink> getDeeplinkToShareCalback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
                if (result != null
                        && !TextUtils.isEmpty(result.getUrl())) {
                    Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
                    if (currentActivity != null) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, result.getUrl());
                        intent.setType("text/plain");

                        currentActivity.startActivity(Intent.createChooser(intent, currentActivity.getString(R.string.cmmsdk_share)));
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getDeeplinkToShare", exception);
            }
        };
    }

    public void getDeferredDeepLink() {
        if (!GeneralPreferences.getInstance().wasInitialDeepLinkRead()) {

            submitBgTask(new Callable<Deeplink>() {
                @Override
                public Deeplink call() throws Exception {
                    String ipAddress = DeeplinkUtils.getMyExternalIP();

                    String androidVersion = Build.VERSION.RELEASE;

                    StringBuilder sb = new StringBuilder();
                    sb.append(TextUtils.isEmpty(ipAddress) ? "" : ipAddress);
                    sb.append("|");
                    sb.append("Android");
                    sb.append("|");
                    sb.append(TextUtils.isEmpty(androidVersion) ? "" : androidVersion);
                    
                    String md5 = DeeplinkUtils.calculateMD5(sb.toString());

                    return devcammentClient.deferredDeeplinkDeeplinkHashGet(md5);
                }
            }, getDeferredDeepLinkCallback());
        }
    }

    private CammentCallback<Deeplink> getDeferredDeepLinkCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
                GeneralPreferences.getInstance().recordInitialDeeplinkRead();
                if (!TextUtils.isEmpty(result.getUrl())) {
                    String[] split = result.getUrl().split("/");
                    if (split.length > 0) {
                        Usergroup usergroup = new Usergroup();
                        usergroup.setUuid(split[split.length - 1]);
                        UserGroupProvider.insertUserGroup(usergroup);
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                GeneralPreferences.getInstance().recordInitialDeeplinkRead();
            }
        };
    }

}
