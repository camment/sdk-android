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

/**
 * Created by petrushka on 03/08/2017.
 */

public class UserApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    public UserApi(ExecutorService executorService, DevcammentClient devcammentClient) {
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

    private class FbUserInfo {

        private String facebookId;
        private String name;
        private String picture;

    }

}
