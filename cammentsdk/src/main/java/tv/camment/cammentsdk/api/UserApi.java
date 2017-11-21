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


public final class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void updateUserInfo(final CammentUserInfo userInfo) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (userInfo != null
                        && userInfo.getAuthType() != null) {
                    switch (userInfo.getAuthType()) {
                        case FACEBOOK:
                            if (userInfo instanceof CammentFbUserInfo) {
                                FbUserInfo fbUserInfo = new FbUserInfo();
                                fbUserInfo.facebookId = ((CammentFbUserInfo) userInfo).getFacebookUserId();
                                fbUserInfo.name = userInfo.getName();
                                fbUserInfo.picture = userInfo.getImageUrl();

                                UserinfoInRequest userinfoInRequest = new UserinfoInRequest();
                                userinfoInRequest.setUserinfojson(new Gson().toJson(fbUserInfo));

                                devcammentClient.userinfoPost(userinfoInRequest);
                            }
                            break;
                    }

                    //EventBus.getDefault().post(new UserInfoChangedEvent(userInfo));

                    //TODO where to do this
                    if (BuildConfig.USE_MIXPANEL) {
                        MixpanelHelper.getInstance().setIdentity();
                    }
                }

                return new Object();
            }
        }, updateUserInfoCallback(userInfo));
    }

    //keep public
    public class FbUserInfo {

        public String facebookId;
        public String name;
        public String picture;

    }

    private CammentCallback<Object> updateUserInfoCallback(final CammentUserInfo userInfo) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "updateUserInfo");

                CammentSDK.getInstance().connectToIoT();

//                if (handleDeeplink) {
//                    CammentSDK.getInstance().handleDeeplink("camment"); //TODO deeplink
//                }
                //AuthInfoProvider.insertUserInfo(userInfo); //TODO only after success?

                CammentSDK.getInstance().hideProgressBar();
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "updateUserInfo", exception);
                CammentSDK.getInstance().hideProgressBar();
            }
        };
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

    public void getMyUserCognitoId(CammentCallback<String> getMyUserCognitoIdCallback) {
        submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return AWSManager.getInstance().getCognitoCachingCredentialsProvider().getIdentityId();
            }
        }, getMyUserCognitoIdCallback);
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
                Log.d("onSuccess", "getMyUserGroups");
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
                Log.d("onSuccess", "getUserInfosForGroupUuid");
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
                Log.d("sendCongnitoIdChanged", "onSuccess");
            }

            @Override
            public void onException(Exception e) {
                Log.e("sendCongnitoIdChanged", "onException", e);
            }
        });
    }

}
