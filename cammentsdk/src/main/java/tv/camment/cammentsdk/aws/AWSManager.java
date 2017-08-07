package tv.camment.cammentsdk.aws;


import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.camment.clientsdk.DevcammentClient;
import com.facebook.AccessToken;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;

public final class AWSManager {

    private static AWSManager INSTANCE;

    public static AWSManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AWSManager();
        }
        return INSTANCE;
    }

    private AWSManager() {

    }

    private synchronized Map<String, String> getAwsLoginsMap() {
        Map<String, String> loginsMap = new HashMap<>();
        if (AccessToken.getCurrentAccessToken() != null
                && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken())) {
            loginsMap.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        }
        return loginsMap;
    }

    public CognitoCachingCredentialsProvider getCognitoCachingCredentialsProvider() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                CammentSDK.getInstance().getApplicationContext(),
                "eu-central-1:a5d9d157-be3b-4a86-9bc9-ebd1890bcd62",
                Regions.EU_CENTRAL_1);
        credentialsProvider.setLogins(getAwsLoginsMap());
        return credentialsProvider;
    }

    public ApiClientFactory getApiClientFactory() {
        ApiClientFactory apiClientFactory = new ApiClientFactory();
        apiClientFactory.credentialsProvider(getCognitoCachingCredentialsProvider());
        apiClientFactory.apiKey("Zxp1xJXY9ya6MUovcRkiD6lufkg51fhw2x3ATSvi"); //TODO extract to gradle
        return apiClientFactory;
    }

    public DevcammentClient getDevcammentClient() {
        return getApiClientFactory().build(DevcammentClient.class);
    }

    public AmazonS3 getAmazonS3() {
        return new AmazonS3Client(getCognitoCachingCredentialsProvider());
    }

    public TransferUtility getTransferUtility() {
        return new TransferUtility(getAmazonS3(), CammentSDK.getInstance().getApplicationContext());
    }

    public KeystoreHelper getKeystoreHelper() {
        return new KeystoreHelper(Executors.newSingleThreadExecutor());
    }

}
