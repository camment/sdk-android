package tv.camment.cammentsdk.api;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriendList;
import com.camment.clientsdk.model.UsergroupList;
import com.camment.clientsdk.model.Userinfo;
import com.camment.clientsdk.model.UserinfoList;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.aws.messages.InvitationMessage;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
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
        String indentityId = IdentityPreferences.getInstance().getIdentityId();

        int apiHashCode = indentityId.hashCode();

        if (ApiCallManager.getInstance().canCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode)) {
            submitBgTask(new Callable<UsergroupList>() {
                @Override
                public UsergroupList call() throws Exception {
                    return devcammentClient.meGroupsGet();
                }
            }, getMyUserGroupsCallback(apiHashCode));
        }
    }

    private CammentCallback<UsergroupList> getMyUserGroupsCallback(final int apiHashCode) {
        return new CammentCallback<UsergroupList>() {
            @Override
            public void onSuccess(UsergroupList result) {
                LogUtils.debug("onSuccess", "getMyUserGroups");
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode);

                if (result != null
                        && result.getItems() != null) {
                    UserGroupProvider.insertUserGroups(result.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getMyUserGroups", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_MY_USER_GROUPS, apiHashCode);

            }
        };
    }

    public void getUserInfosForGroupUuidAndHandleBlockedUser(final String groupUuid, final InvitationMessage message) {
        if (ApiCallManager.getInstance().canCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode())) {
            submitBgTask(new Callable<UserinfoList>() {
                @Override
                public UserinfoList call() throws Exception {
                    return devcammentClient.usergroupsGroupUuidUsersGet(groupUuid);
                }
            }, getUserInfosForGroupUuidAndHandleBlockedUserCallback(groupUuid, message));
        }
    }

    private CammentCallback<UserinfoList> getUserInfosForGroupUuidAndHandleBlockedUserCallback(final String groupUuid, final InvitationMessage message) {
        return new CammentCallback<UserinfoList>() {
            @Override
            public void onSuccess(UserinfoList result) {
                LogUtils.debug("onSuccess", "getUserInfosForGroupUuidAndHandleBlockedUser");
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode());

                if (result != null
                        && result.getItems() != null) {
                    UserInfoProvider.insertUserInfos(result.getItems(), groupUuid);

                    String identityId = IdentityPreferences.getInstance().getIdentityId();

                    boolean blocked = false;

                    for (Userinfo userinfo : result.getItems()) {
                        if (TextUtils.equals(identityId, userinfo.getUserCognitoIdentityId())
                                && TextUtils.equals(userinfo.getState(), UserState.BLOCKED.getStringValue())) {
                            blocked = true;
                            break;
                        }
                    }

                    if (blocked) {
                        runOnUiThreadDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CammentSDK.getInstance().hideProgressBar();
                                Toast.makeText(CammentSDK.getInstance().getApplicationContext(), R.string.cmmsdk_cant_join_blocked, Toast.LENGTH_LONG).show();
                            }
                        }, 1000);
                    } else {
                        ApiManager.getInstance().getGroupApi().getUserGroupByUuid(message.body.groupUuid, message);
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserInfosForGroupUuidAndHandleBlockedUser", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode());
            }
        };
    }

    public void getUserInfosForGroupUuid(final String groupUuid) {
        if (ApiCallManager.getInstance().canCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode())) {
            submitBgTask(new Callable<UserinfoList>() {
                @Override
                public UserinfoList call() throws Exception {
                    return devcammentClient.usergroupsGroupUuidUsersGet(groupUuid);
                }
            }, getUserInfosForGroupUuidCallback(groupUuid));
        }
    }

    private CammentCallback<UserinfoList> getUserInfosForGroupUuidCallback(final String groupUuid) {
        return new CammentCallback<UserinfoList>() {
            @Override
            public void onSuccess(UserinfoList result) {
                LogUtils.debug("onSuccess", "getUserInfosForGroupUuid");
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode());

                if (result != null
                        && result.getItems() != null) {
                    UserInfoProvider.insertUserInfos(result.getItems(), groupUuid);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserInfosForGroupUuid", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_USER_INFOS, groupUuid.hashCode());
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
