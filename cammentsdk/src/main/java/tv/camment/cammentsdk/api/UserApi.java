package tv.camment.cammentsdk.api;

import android.net.Uri;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriendList;
import com.camment.clientsdk.model.UserinfoInRequest;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;
import com.google.gson.Gson;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;


public final class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void updateUserInfo() {
        submitTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UserinfoInRequest userinfoInRequest = new UserinfoInRequest();
                Profile profile = Profile.getCurrentProfile();

                FbUserInfo fbUserInfo = new FbUserInfo();
                fbUserInfo.facebookId = profile.getId();
                fbUserInfo.name = profile.getName();

                Uri pictureUri = ImageRequest.getProfilePictureUri(profile.getId(), 270, 270);

                fbUserInfo.picture = pictureUri.toString();

                userinfoInRequest.setUserinfojson(new Gson().toJson(fbUserInfo));

                devcammentClient.userinfoPost(userinfoInRequest);

                return new Object();
            }
        }, updateUserInfoCallback());
    }

    //keep public
    public class FbUserInfo {

        public String facebookId;
        public String name;
        public String picture;

    }

    private CammentCallback<Object> updateUserInfoCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "updateUserInfo");
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "updateUserInfo", exception);
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

}
