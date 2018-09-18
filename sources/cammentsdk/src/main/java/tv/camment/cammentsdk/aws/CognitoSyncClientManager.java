package tv.camment.cammentsdk.aws;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.Map;

import tv.camment.cammentsdk.CammentSDK;

public final class CognitoSyncClientManager {

    private static CognitoSyncClientManager INSTANCE;
    private final CognitoCachingCredentialsProvider credentialsProvider;

    private CognitoSyncManager syncClient;
    private final CammentAuthenticationProvider cammentAuthenticationProvider;

    public static CognitoSyncClientManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CognitoSyncClientManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CognitoSyncClientManager();
                }
            }
        }
        return INSTANCE;
    }

    private CognitoSyncClientManager() {
        cammentAuthenticationProvider = AWSManager.getInstance().getCammentAuthenticationProvider();
        credentialsProvider = AWSManager.getInstance().getCognitoCachingCredentialsProvider();
        syncClient = new CognitoSyncManager(CammentSDK.getInstance().getApplicationContext(),
                Regions.EU_CENTRAL_1, credentialsProvider);
    }

    public void addLogins(String providerName, String token) {
        Map<String, String> logins = credentialsProvider.getLogins();
        if (logins == null) {
            logins = new HashMap<>();
        }
        logins.put(providerName, token);
        credentialsProvider.setLogins(logins);
    }

    public CognitoSyncManager getSyncClient() {
        return syncClient;
    }

}
