package tv.camment.cammentsdk.aws;

import tv.camment.cammentsdk.BuildConfig;

final class AWSConfig {

    static final String IOT_TOPIC = "camment/app";

    static final String IOT_GROUP_TOPIC = "camment/group/";

    static final String IOT_USER_TOPIC = "camment/user/";

    private static final String IDENTITY_POOL_DEV = "eu-central-1:aa96090c-0423-46d1-a584-20086c0e134d";

    private static final String IDENTITY_POOL_PROD = "eu-central-1:a57fbb10-a8ff-49e0-a69b-b6903a058868";

    private static final String BUCKET_ID_DEV = "camment-dev-camments";

    private static final String BUCKET_ID_PROD = "camment-prod-camments";

    private static final String IOT_ENDPOINT_DEV = "a33ob3cdhetxbp.iot.eu-central-1.amazonaws.com";

    private static final String IOT_ENDPOINT_PROD = "a1ptuiv94cuwdn.iot.eu-central-1.amazonaws.com";

    private static final String CERT_KEYSTORE_NAME_DEV = "awsiot-store-dev.bks";

    private static final String CERT_KEYSTORE_NAME_PROD = "awsiot-store-prod.bks";

    private static final String CERT_KEYSTORE_PWD_DEV = "?ko8z7re$X=X=22e";

    private static final String CERT_KEYSTORE_PWD_PROD = "{4?Z9p.8#cGpDK7H";

    static final String CERT_ID = "1";

    static String getIdentityPool() {
        if ("dev".equals(BuildConfig.ENVIRONMENT)) {
            return IDENTITY_POOL_DEV;
        } else {
            return IDENTITY_POOL_PROD;
        }
    }

    static String getBucketId() {
        if ("dev".equals(BuildConfig.ENVIRONMENT)) {
            return BUCKET_ID_DEV;
        } else {
            return BUCKET_ID_PROD;
        }
    }

    static String getIotEndpoint() {
        if ("dev".equals(BuildConfig.ENVIRONMENT)) {
            return IOT_ENDPOINT_DEV;
        } else {
            return IOT_ENDPOINT_PROD;
        }
    }

    static String getCertKeystoreName() {
        if ("dev".equals(BuildConfig.ENVIRONMENT)) {
            return CERT_KEYSTORE_NAME_DEV;
        } else {
            return CERT_KEYSTORE_NAME_PROD;
        }
    }

    static String getCertKeystorePwd() {
        if ("dev".equals(BuildConfig.ENVIRONMENT)) {
            return CERT_KEYSTORE_PWD_DEV;
        } else {
            return CERT_KEYSTORE_PWD_PROD;
        }
    }

}
