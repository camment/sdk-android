package tv.camment.cammentsdk.api;

import android.util.Log;

import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.OpenIdToken;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Handler;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.CognitoSyncClientManager;
import tv.camment.cammentsdk.helpers.AuthHelper;


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

    //synchronous
    public OpenIdToken getOpenIdTokenSync(final String fbToken) {
        try {
            return devcammentClient.usersGetOpenIdTokenGet(fbToken);
        } catch (ApiClientException e) {
            Log.e("onException", "getOpenIdTokenSync", e);
        }
        return null;
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

    public void logIn() {
        CammentSDK.getInstance().showProgressBar();

        CammentAuthInfo authInfo = AuthHelper.getInstance().getAuthInfo();
        if (authInfo != null) {
            switch (authInfo.getAuthType()) {
                case FACEBOOK:
                    final CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) authInfo;
                    submitBgTask(new Callable<OpenIdToken>() {
                        @Override
                        public OpenIdToken call() throws Exception {
                            return devcammentClient.usersGetOpenIdTokenGet(fbAuthInfo.getToken());
                        }
                    }, new CammentCallback<OpenIdToken>() {
                        @Override
                        public void onSuccess(OpenIdToken result) {
                            Log.d("onSuccess1", "getOpenIdToken");
                            if (result != null) {
                                Log.d("onSuccess1", "getOpenIdToken identityId: " + result.getIdentityId());
                                Log.d("onSuccess1", "getOpenIdToken token: " + result.getToken());
                                CognitoSyncClientManager.getInstance().addLogins("login.camment.tv", result.getToken());
                                AWSManager.getInstance().getCognitoCachingCredentialsProvider().getIdentityProvider().refresh();
                            }
                        }

                        @Override
                        public void onException(Exception exception) {
                            Log.e("onException1", "getOpenIdToken", exception);
                            CammentSDK.getInstance().hideProgressBar();
                        }
                    });
                    break;
            }
        }
    }
}
