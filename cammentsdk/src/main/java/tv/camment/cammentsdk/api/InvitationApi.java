package tv.camment.cammentsdk.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.AcceptInvitationRequest;
import com.camment.clientsdk.model.Deeplink;
import com.camment.clientsdk.model.FacebookFriend;
import com.camment.clientsdk.model.ShowUuid;
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
import tv.camment.cammentsdk.data.DataManager;
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
                final String showUuid = GeneralPreferences.getInstance().getActiveShowUuid();
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

    public void acceptInvitation(final String groupUuid, final String key) {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AcceptInvitationRequest acceptInvitationRequest = new AcceptInvitationRequest();
                acceptInvitationRequest.setInvitationKey(key);
                devcammentClient.usergroupsGroupUuidInvitationsPut(groupUuid, acceptInvitationRequest);

                return new Object();
            }
        }, acceptInvitationCallback(groupUuid));
    }

    private CammentCallback<Object> acceptInvitationCallback(final String groupUuid) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                DataManager.getInstance().clearDataForUserGroupChange();

                Usergroup usergroup = new Usergroup();
                usergroup.setUuid(groupUuid);

                UserGroupProvider.insertUserGroup(usergroup);

                ApiManager.getInstance().getCammentApi().getUserGroupCamments();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "acceptInvitation", exception);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CammentSDK.getInstance().getApplicationContext(),
                                CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_invitation_error),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }

    void getDeeplinkToShare() {
        submitTask(new Callable<Deeplink>() {
            @Override
            public Deeplink call() throws Exception {
                final String showUuid = GeneralPreferences.getInstance().getActiveShowUuid();
                final String userGroupUuid = UserGroupProvider.getUserGroup().getUuid();

                ShowUuid show = new ShowUuid();
                show.setShowUuid(showUuid);

                return devcammentClient.usergroupsGroupUuidDeeplinkPost(userGroupUuid, show);
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

                        currentActivity.startActivity(Intent.createChooser(intent, currentActivity.getString(R.string.cmmsdk_invite)));
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

    private CammentCallback<Deeplink> getDeferredDeepLinkCallback() {
        return new CammentCallback<Deeplink>() {
            @Override
            public void onSuccess(Deeplink result) {
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

            }
        };
    }

}
