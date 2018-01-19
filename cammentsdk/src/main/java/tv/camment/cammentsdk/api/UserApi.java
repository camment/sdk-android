package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriendList;
import com.camment.clientsdk.model.UsergroupList;
import com.camment.clientsdk.model.UserinfoInRequest;
import com.camment.clientsdk.model.UserinfoList;
import com.google.gson.Gson;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.utils.LogUtils;


public final class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void getFacebookFriends(CammentCallback<FacebookFriendList> getFacebookFriendsCallback) {
        submitTask(new Callable<FacebookFriendList>() {
            @Override
            public FacebookFriendList call() throws Exception {
                CammentAuthInfo authInfo = AuthHelper.getInstance().getAuthInfo();
                if (authInfo != null
                        && authInfo.getAuthType() != null
                        && authInfo.getAuthType() == CammentAuthType.FACEBOOK) {
                    return devcammentClient.meFbFriendsGet(((CammentFbAuthInfo) authInfo).getToken());
                } else {
                    return new FacebookFriendList();
                }
            }
        }, getFacebookFriendsCallback);
    }

    public void getMyUserGroups() {
        submitBgTask(new Callable<UsergroupList>() {
            @Override
            public UsergroupList call() throws Exception {
                return devcammentClient.meGroupsGet();
            }
        }, getMyUserGroupsCallback());
    }

    private CammentCallback<UsergroupList> getMyUserGroupsCallback() {
        return new CammentCallback<UsergroupList>() {
            @Override
            public void onSuccess(UsergroupList result) {
                LogUtils.debug("onSuccess", "getMyUserGroups");
                if (result != null
                        && result.getItems() != null) {
                    UserGroupProvider.insertUserGroups(result.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getMyUserGroups", exception);
            }
        };
    }

    public void getUserInfosForGroupUuid(final String groupUuid) {
        submitBgTask(new Callable<UserinfoList>() {
            @Override
            public UserinfoList call() throws Exception {
                return devcammentClient.usergroupsGroupUuidUsersGet(groupUuid);
            }
        }, getUserInfosForGroupUuidCallback(groupUuid));
    }

    private CammentCallback<UserinfoList> getUserInfosForGroupUuidCallback(final String groupUuid) {
        return new CammentCallback<UserinfoList>() {
            @Override
            public void onSuccess(UserinfoList result) {
                LogUtils.debug("onSuccess", "getUserInfosForGroupUuid");
                if (result != null
                        && result.getItems() != null) {
                    UserInfoProvider.insertUserInfos(result.getItems(), groupUuid);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserInfosForGroupUuid", exception);
            }
        };
    }

    public void sendCongnitoIdChanged() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                devcammentClient.meUuidPut();
                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                LogUtils.debug("sendCongnitoIdChanged", "onSuccess");
            }

            @Override
            public void onException(Exception e) {
                Log.e("sendCongnitoIdChanged", "onException", e);
            }
        });
    }

}
