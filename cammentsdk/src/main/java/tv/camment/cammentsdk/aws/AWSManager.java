package tv.camment.cammentsdk.aws;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.camment.clientsdk.DevcammentClient;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.DevcammentClientDev;
import tv.camment.cammentsdk.api.DevcammentClientProd;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.FileUtils;

public final class AWSManager {

    private static AWSManager INSTANCE;

    private final Cache cache;

    public static AWSManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AWSManager();
        }
        return INSTANCE;
    }

    private AWSManager() {
        cache = new SimpleCache(FileUtils.getInstance().getUploadDirFile(), new LeastRecentlyUsedCacheEvictor(50 * 1000 * 1024)); //50 MB
    }

    private synchronized Map<String, String> getAwsLoginsMap() {
        Map<String, String> loginsMap = new HashMap<>();
//        if (AccessToken.getCurrentAccessToken() != null
//                && !TextUtils.isEmpty(AccessToken.getCurrentAccessToken().getToken())) {
//            loginsMap.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
//        }
        //TODO add check if userId (e.g. email was not yet set by host app)
        //loginsMap.put("cognito-identity.amazonaws.com", "eyJraWQiOiJldS1jZW50cmFsLTExIiwidHlwIjoiSldTIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJldS1jZW50cmFsLTE6NDNjYTZhZTMtMmI0Ny00YzYxLWEzZGQtNzA3MWQ1ZTllZjE3IiwiYXVkIjoiZXUtY2VudHJhbC0xOmFhOTYwOTBjLTA0MjMtNDZkMS1hNTg0LTIwMDg2YzBlMTM0ZCIsImFtciI6WyJhdXRoZW50aWNhdGVkIiwibG9naW4uY2FtbWVudC50diIsImxvZ2luLmNhbW1lbnQudHY6ZXUtY2VudHJhbC0xOmFhOTYwOTBjLTA0MjMtNDZkMS1hNTg0LTIwMDg2YzBlMTM0ZDpwZXRyYUBjYW1tZW50LnR2Il0sImlzcyI6Imh0dHBzOi8vY29nbml0by1pZGVudGl0eS5hbWF6b25hd3MuY29tIiwiZXhwIjoxNTEwNjcwMDc0LCJpYXQiOjE1MTA2NjkxNzR9.sUR_LhKpeGC4qkgIQgDr8pwm1ZYNeVj6jIt8GvfWIjpu0XKeb3oXkjDCqSjJyra92fdJ7QDV8cNtsyxrNSExIHjJ6JQhBxUNIgCT8LbIOx6dX-zQwtjxotVUZlnqu2IK_Ow1bJhz9MWaKyp-uqAUx0hykeGzuF_CxCvtjvl0xbWFgez69875duKETNdDr1Y0JJHsIDyHyLTeVOriUXSAKgXJ0xHRxFKqy_FVhcyai3Me2mHUYsqP1DzwikDzuMUZzcGOJ8EQuNVwNu5pKAQskjpkeh1q4Vcs7v-C8-1k-skXFuXItyi97mfwa5fQz8t8CyY4DDHNdWd51ZMYVCq4Gw");

        return loginsMap;
    }

    public CammentAuthenticationProvider getCammentAuthenticationProvider() {
        return new CammentAuthenticationProvider(null, AWSConfig.getIdentityPool(), Regions.EU_CENTRAL_1);
    }

    public CognitoCachingCredentialsProvider getCognitoCachingCredentialsProvider() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                CammentSDK.getInstance().getApplicationContext(),
                getCammentAuthenticationProvider(),
                Regions.EU_CENTRAL_1);
        credentialsProvider.setLogins(getAwsLoginsMap());
        credentialsProvider.registerIdentityChangedListener(CammentSDK.getInstance());
        return credentialsProvider;
    }

    public CognitoSyncManager getCognitoSyncManager() {
        return new CognitoSyncManager(
                CammentSDK.getInstance().getApplicationContext(),
                Regions.EU_CENTRAL_1,
                getCognitoCachingCredentialsProvider());
    }

    private ApiClientFactory getApiClientFactory(boolean useCredentialsProvider) {
        ApiClientFactory apiClientFactory = new ApiClientFactory();

        if (useCredentialsProvider) {
            apiClientFactory.credentialsProvider(getCognitoCachingCredentialsProvider());
        }

        apiClientFactory.apiKey(CammentSDK.getInstance().getApiKey());
        return apiClientFactory;
    }

    public DevcammentClient getDevcammentClient(boolean useCredentialsProvider) {
        switch (BuildConfig.BUILD_TYPE) {
            case "debugDev":
            case "releaseDev":
                return getApiClientFactory(useCredentialsProvider).build(DevcammentClientDev.class);
            case "debug":
            case "release":
            default:
                return getApiClientFactory(useCredentialsProvider).build(DevcammentClientProd.class);
        }
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
        return new AWSIotMqttManager(IdentityPreferences.getInstance().getIdentityId(), AWSConfig.getIotEndpoint());
    }

    private KeyStore getClientKeyStore() {
        return AWSIotKeystoreHelper.getIotKeystore(AWSConfig.CERT_ID, FileUtils.getInstance().getRootDirectory(),
                AWSConfig.getCertKeystoreName(), AWSConfig.getCertKeystorePwd());
    }

    public IoTHelper getIoTHelper() {
        return new IoTHelper(Executors.newSingleThreadExecutor(), getClientKeyStore());
    }

    public Cache getExoPlayerCache() {
        return cache;
    }

}
