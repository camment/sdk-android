package tv.camment.cammentsdk.aws;

import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;

import tv.camment.cammentsdk.api.ApiManager;
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

        //setToken(null); //TODO override existing token, what if request to server fails?

        retrieveCredentialsFromCammentServer();

        return token;
    }

    // If the app has a valid identityId return it, otherwise get a valid
    // identityId from your backend.
    @Override
    public String getIdentityId() {
        Log.d("AUTH", "getIdentityId");

        identityId = AWSManager.getInstance().getCognitoCachingCredentialsProvider().getCachedIdentityId();

        retrieveCredentialsFromCammentServer();

        return identityId;
    }

    private void retrieveCredentialsFromCammentServer() {
        if (TextUtils.isEmpty(identityId)) {
            ApiManager.getInstance().getAuthApi().getOpenIdToken("petra@camment.tv");
        }
    }

    public void updateCredentials(String identityId, String token) {
        update(identityId, token);
        IdentityPreferences.getInstance().saveIdentityId(identityId);
    }

}
