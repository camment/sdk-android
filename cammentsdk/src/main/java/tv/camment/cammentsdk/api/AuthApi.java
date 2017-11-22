package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.OpenIdToken;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;


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

    public Future<OpenIdToken> getOpenIdToken(final String fbToken) {
        return submitTask(new Callable<OpenIdToken>() {
            @Override
            public OpenIdToken call() throws Exception {
                return devcammentClient.usersGetOpenIdTokenGet(fbToken);
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
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getOpenIdToken", exception);
            }
        };
    }

}
