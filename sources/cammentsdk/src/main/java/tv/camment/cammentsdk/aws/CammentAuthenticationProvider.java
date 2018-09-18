package tv.camment.cammentsdk.aws;

import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;
import com.camment.clientsdk.model.OpenIdToken;

import org.greenrobot.eventbus.EventBus;

import java.util.ConcurrentModificationException;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.events.LoginStatusChangedEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.utils.LogUtils;


public final class CammentAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    CammentAuthenticationProvider(String accountId, String identityPoolId, Regions region) {
        super(accountId, identityPoolId, region);
    }

    @Override
    public String getProviderName() {
        return "login.camment.tv";
    }

    // Use the refresh method to communicate with your backend to get an
    // identityId and token.
    @Override
    public String refresh() {
        setToken(null);

        if (!this.loginsMap.isEmpty()
                && this.loginsMap.containsKey(getProviderName())) {
            OpenIdToken openIdToken = retrieveCredentialsFromCammentServer();
            if (openIdToken != null) {
                try {
                    update(openIdToken.getIdentityId(), openIdToken.getToken());

                    AWSManager.getInstance().getIoTHelper().subscribe();

                    if (GeneralPreferences.getInstance().isFirstStartup()) {
                        PendingActions.getInstance().addAction(PendingActions.Action.HANDLE_DEEPLINK);
                    }

                    if (BuildConfig.USE_MIXPANEL) {
                        MixpanelHelper.getInstance().setIdentity();
                    }

                    EventBus.getDefault().post(new LoginStatusChangedEvent());
                    PendingActions.getInstance().executePendingActionsIfNeeded();

                    ApiManager.getInstance().retryFailedCallsIfNeeded();
                } catch (ConcurrentModificationException e) {
                    LogUtils.debug("onException", "refresh ConcurrentModificationException");
                }
            } else {
                try {
                    update(null, null);
                } catch (ConcurrentModificationException e) {
                    LogUtils.debug("onException1", "refresh ConcurrentModificationException");
                }
            }
            return openIdToken == null ? null : openIdToken.getToken();
        }

        this.getIdentityId();

        return null;
    }

    // If the app has a valid identityId return it, otherwise get a valid
    // identityId from your backend.
    @Override
    public String getIdentityId() {
        identityId = AWSManager.getInstance().getCognitoCachingCredentialsProvider().getCachedIdentityId();

        if (identityId == null) {
            if (!this.loginsMap.isEmpty()
                    && this.loginsMap.containsKey(getProviderName())) {
                OpenIdToken openIdToken = retrieveCredentialsFromCammentServer();
                if (openIdToken != null) {
                    update(openIdToken.getIdentityId(), openIdToken.getToken());
                } else {
                    update(null, null);
                }
                return openIdToken == null ? null : openIdToken.getIdentityId();
            } else {
                return super.getIdentityId();
            }
        }

        return identityId;
    }

    private OpenIdToken retrieveCredentialsFromCammentServer() {
        CammentAuthInfo authInfo = AuthHelper.getInstance().getAuthInfo();
        if (authInfo != null) {
            switch (authInfo.getAuthType()) {
                case FACEBOOK:
                    CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) authInfo;
                    OpenIdToken openIdToken = ApiManager.getInstance().getAuthApi().getOpenIdTokenSync(fbAuthInfo.getToken());

                    if (openIdToken == null)
                        return null;

                    try {
                        LogUtils.debug("openIdToken identity", openIdToken.getIdentityId());
                        LogUtils.debug("openIdToken token", openIdToken.getToken());
                        return openIdToken;
                    } catch (Exception e) {
                        Log.e("openIdToken", "failed", e);
                    }
                    break;
            }
        }
        return null;
    }

}
