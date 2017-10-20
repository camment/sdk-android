package tv.camment.cammentsdk.aws;


import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.camment.clientsdk.DevcammentClient;
import com.facebook.AccessToken;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.FileUtils;

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
                AWSConfig.IDENTITY_POOL,
                Regions.EU_CENTRAL_1);
        credentialsProvider.setLogins(getAwsLoginsMap());
        return credentialsProvider;
    }

    private ApiClientFactory getApiClientFactory() {
        ApiClientFactory apiClientFactory = new ApiClientFactory();
        apiClientFactory.credentialsProvider(getCognitoCachingCredentialsProvider());
        apiClientFactory.apiKey(CammentSDK.getInstance().getApiKey());
        return apiClientFactory;
    }

    public DevcammentClient getDevcammentClient() {
        return getApiClientFactory().build(DevcammentClient.class);
    }

    private AmazonS3 getAmazonS3() {
        return new AmazonS3Client(getCognitoCachingCredentialsProvider());
    }

    private TransferUtility getTransferUtility() {
        return TransferUtility.builder().s3Client(getAmazonS3()).context(CammentSDK.getInstance().getApplicationContext()).build();
    }

    private KeystoreHelper getKeystoreHelper() {
        return new KeystoreHelper(Executors.newSingleThreadExecutor());
    }

    public void checkKeyStore() {
        getKeystoreHelper().checkKeyStore();
    }

    public S3UploadHelper getS3UploadHelper() {
        return new S3UploadHelper(Executors.newSingleThreadExecutor(), getTransferUtility());
    }

    AWSIotMqttManager getAWSIotMqttManager() {
        return new AWSIotMqttManager(IdentityPreferences.getInstance().getIdentityId(), AWSConfig.IOT_ENDPOINT);
    }

    private KeyStore getClientKeyStore() {
        return AWSIotKeystoreHelper.getIotKeystore(AWSConfig.CERT_ID, FileUtils.getInstance().getRootDirectory(),
                AWSConfig.CERT_KEYSTORE_NAME, AWSConfig.CERT_KEYSTORE_PWD);
    }

    public IoTHelper getIoTHelper() {
        return new IoTHelper(Executors.newSingleThreadExecutor(), getClientKeyStore());
    }

}
