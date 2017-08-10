package tv.camment.cammentsdk.aws;


import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.camment.clientsdk.DevcammentClient;
import com.facebook.AccessToken;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.SDKConfig;
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
                "eu-central-1:a5d9d157-be3b-4a86-9bc9-ebd1890bcd62",
                Regions.EU_CENTRAL_1);
        credentialsProvider.setLogins(getAwsLoginsMap());
        return credentialsProvider;
    }

    public String getUserIdentityId() {
        return getCognitoCachingCredentialsProvider().getIdentityId();
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

    public S3UploadHelper getS3UploadHelper() {
        return new S3UploadHelper(Executors.newSingleThreadExecutor(), getTransferUtility());
    }

    public AWSIotMqttManager getAWSIotMqttManager() {
        return new AWSIotMqttManager(getUserIdentityId(), SDKConfig.IOT_ENDPOINT);
    }

    public AWSIotClient getAWSIotClient() {
        AWSIotClient awsIotClient = new AWSIotClient(getCognitoCachingCredentialsProvider());
        awsIotClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
        return awsIotClient;
    }

    public KeyStore getClientKeyStore() {
        return AWSIotKeystoreHelper.getIotKeystore(SDKConfig.CERT_ID, FileUtils.getInstance().getRootDirectory(),
                SDKConfig.CERT_KEYSTORE_NAME, SDKConfig.CERT_KEYSTORE_PWD);
    }

    public IoTHelper getIoTHelper(IoTHelper.IoTMessageArrivedListener ioTMessageArrivedListener) {
        return new IoTHelper(Executors.newSingleThreadExecutor(), getAWSIotMqttManager(),
                getClientKeyStore(), ioTMessageArrivedListener);
    }

}
