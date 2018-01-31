package tv.camment.cammentsdk.aws;


import android.text.TextUtils;

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
import java.util.UUID;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.DevcammentClientDev;
import tv.camment.cammentsdk.api.DevcammentClientProd;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.utils.FileUtils;

public final class AWSManager {

    private static AWSManager INSTANCE;

    private final Cache cache;
    private CammentAuthenticationProvider cammentAuthenticationProvider;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private IoTHelper iotHelper;

    public static AWSManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AWSManager();
        }
        return INSTANCE;
    }

    private AWSManager() {
        cache = new SimpleCache(FileUtils.getInstance().getUploadDirFile(), new LeastRecentlyUsedCacheEvictor(50 * 1000 * 1024)); //50 MB
    }

    public CammentAuthenticationProvider getCammentAuthenticationProvider() {
        if (cammentAuthenticationProvider == null) {
            cammentAuthenticationProvider = new CammentAuthenticationProvider(null,
                    AWSConfig.getIdentityPool(),
                    Regions.EU_CENTRAL_1);
        }
        return cammentAuthenticationProvider;
    }

    public CognitoCachingCredentialsProvider getCognitoCachingCredentialsProvider() {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    CammentSDK.getInstance().getApplicationContext(),
                    getCammentAuthenticationProvider(),
                    Regions.EU_CENTRAL_1);
            credentialsProvider.registerIdentityChangedListener(CammentSDK.getInstance());
        }
        //credentialsProvider.setLogins(getAwsLoginsMap());
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
        switch (BuildConfig.FLAVOR) {
            case "devApi":
                return getApiClientFactory(useCredentialsProvider).build(DevcammentClientDev.class);
            case "prodApi":
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
        return new AWSIotMqttManager(getIoTId(), AWSConfig.getIotEndpoint());
    }

    private String getIoTId() {
        String iotId = GeneralPreferences.getInstance().getIotId();
        if (TextUtils.isEmpty(iotId)) {
            iotId = UUID.randomUUID().toString();
            GeneralPreferences.getInstance().setIotId(iotId);
        }
        return iotId;
    }

    private KeyStore getClientKeyStore() {
        return AWSIotKeystoreHelper.getIotKeystore(AWSConfig.CERT_ID, FileUtils.getInstance().getRootDirectory(),
                AWSConfig.getCertKeystoreName(), AWSConfig.getCertKeystorePwd());
    }

    public IoTHelper getIoTHelper() {
        if (iotHelper == null) {
            iotHelper = new IoTHelper(Executors.newSingleThreadExecutor(), getClientKeyStore());
        }
        return iotHelper;
    }

    public Cache getExoPlayerCache() {
        return cache;
    }

}
