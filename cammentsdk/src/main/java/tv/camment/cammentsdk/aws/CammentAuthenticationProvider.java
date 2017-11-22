package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;
import com.camment.clientsdk.model.OpenIdToken;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;


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
        Log.d("AUTH", "refresh");

        setToken(null);

        if (!this.loginsMap.isEmpty()) {
            OpenIdToken openIdToken = retrieveCredentialsFromCammentServer();
            if (openIdToken != null) {
                update(openIdToken.getIdentityId(), openIdToken.getToken());
            } else {
                update(null, null);
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
        Log.d("AUTH", "getIdentityId");

        identityId = AWSManager.getInstance().getCognitoCachingCredentialsProvider().getCachedIdentityId();

        if (identityId == null) {
            if (!this.loginsMap.isEmpty()) {
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

    public OpenIdToken retrieveCredentialsFromCammentServer() {
        if (AuthHelper.getInstance().isHostAppLoggedIn()
                && TextUtils.isEmpty(identityId)) {
            CammentAuthInfo authInfo = AuthHelper.getInstance().getAuthInfo();
            if (authInfo != null) {
                switch (authInfo.getAuthType()) {
                    case FACEBOOK:
                        CammentFbAuthInfo fbAuthInfo = (CammentFbAuthInfo) authInfo;
                        Future<OpenIdToken> openIdToken = ApiManager.getInstance().getAuthApi().getOpenIdToken(fbAuthInfo.getToken());
                        try {
                            Log.d("openIdToken", openIdToken.get().getIdentityId());
                            return openIdToken.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e("openIdToken", "failed", e);
                        }
                        break;
                }
            }
        }
        return null;
    }

}
