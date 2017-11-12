package tv.camment.cammentsdk.api;

import android.net.Uri;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriendList;
import com.camment.clientsdk.model.UsergroupList;
import com.camment.clientsdk.model.UserinfoInRequest;
import com.camment.clientsdk.model.UserinfoList;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.events.FbLoginChangedEvent;
import tv.camment.cammentsdk.helpers.MixpanelHelper;


public final class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void updateUserInfo(boolean handleDeeplink) {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UserinfoInRequest userinfoInRequest = new UserinfoInRequest();
                Profile profile = Profile.getCurrentProfile();

                if (profile != null) {
                    FbUserInfo fbUserInfo = new FbUserInfo();
                    fbUserInfo.facebookId = profile.getId();
                    fbUserInfo.name = profile.getName();

                    Uri pictureUri = ImageRequest.getProfilePictureUri(profile.getId(), 270, 270);

                    fbUserInfo.picture = pictureUri.toString();

                    userinfoInRequest.setUserinfojson(new Gson().toJson(fbUserInfo));

                    devcammentClient.userinfoPost(userinfoInRequest);
                }

                EventBus.getDefault().post(new FbLoginChangedEvent());

                if (BuildConfig.USE_MIXPANEL) {
                    MixpanelHelper.getInstance().setIdentity();
                }

                return new Object();
            }
        }, updateUserInfoCallback(handleDeeplink));
    }

    //keep public
    public class FbUserInfo {

        public String facebookId;
        public String name;
        public String picture;

    }

    private CammentCallback<Object> updateUserInfoCallback(final boolean handleDeeplink) {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "updateUserInfo");

                CammentSDK.getInstance().connectToIoT();

                if (handleDeeplink) {
                    CammentSDK.getInstance().handleDeeplink("camment");
                }

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
                return devcammentClient.meFbFriendsGet(AccessToken.getCurrentAccessToken().getToken());
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

    public void refreshCognitoCredentials() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AWSManager.getInstance().getCognitoCachingCredentialsProvider().refresh();
                return new Object();
            }
        }, new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("refreshCognitoCred", "onSuccess");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("refreshCognitoCred", "onException", exception);
            }
        });
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
