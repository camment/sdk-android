package tv.camment.cammentsdk.api;

import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.OpenIdToken;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.CognitoSyncClientManager;
import tv.camment.cammentsdk.events.HideSofaInviteProgress;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.utils.LogUtils;


public final class AuthApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    AuthApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    //synchronous
    public OpenIdToken getOpenIdTokenSync(final String fbToken) {
        try {
            return devcammentClient.usersGetOpenIdTokenGet(fbToken); //TODO check thread
        } catch (ApiClientException e) {
            Log.e("onException", "getOpenIdTokenSync", e);
        }
        return null;
    }

    public void logIn() {
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
                            LogUtils.debug("onSuccess1", "getOpenIdToken");
                            if (result != null) {
                                LogUtils.debug("onSuccess1", "getOpenIdToken identityId: " + result.getIdentityId());
                                LogUtils.debug("onSuccess1", "getOpenIdToken token: " + result.getToken());
                                CognitoSyncClientManager.getInstance().addLogins("login.camment.tv", result.getToken());
                                AWSManager.getInstance().getCognitoCachingCredentialsProvider().getIdentityProvider().refresh();
                            }
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtils.debug("onException1", "getOpenIdToken", exception);
                            if (!(exception instanceof SQLiteException)) {
                                EventBus.getDefault().post(new HideSofaInviteProgress());
                            }
                        }
                    });
                    break;
            }
        }
    }
}
