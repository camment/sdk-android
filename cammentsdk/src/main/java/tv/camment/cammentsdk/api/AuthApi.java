package tv.camment.cammentsdk.api;

import android.net.Uri;
import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.FacebookFriendList;
import com.camment.clientsdk.model.OpenIdToken;
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
import java.util.concurrent.Future;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.events.FbLoginChangedEvent;
import tv.camment.cammentsdk.helpers.MixpanelHelper;


public final class AuthApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    AuthApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
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

    public Future<OpenIdToken> getOpenIdToken(final String login) {
        return submitTask(new Callable<OpenIdToken>() {
            @Override
            public OpenIdToken call() throws Exception {
                return devcammentClient.usersGetOpenIdTokenGet(login);
            }
        }, getOpenIdTokenCallback());
    }

    private CammentCallback<OpenIdToken> getOpenIdTokenCallback() {
        return new CammentCallback<OpenIdToken>() {
            @Override
            public void onSuccess(OpenIdToken result) {
                Log.d("onSuccess", "getOpenIdToken");
                if (result != null) {
                    Log.d("onSuccess", "getOpenIdToken identityId: " + result.getIdentityId());
                    Log.d("onSuccess", "getOpenIdToken token: " + result.getToken());
                    AWSManager.getInstance().getCammentAuthenticationProvider().updateCredentials(result.getIdentityId(), result.getToken());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getOpenIdToken", exception);
            }
        };
    }

}
