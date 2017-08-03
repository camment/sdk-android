package tv.camment.cammentsdk.api;

import com.camment.clientsdk.DevcammentClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.aws.AWSManager;


public class ApiManager {

    private static ApiManager INSTANCE;

    private DevcammentClient devcammentClient;
    private ExecutorService executorService;

    public static ApiManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiManager();
        }
        return INSTANCE;
    }

    private ApiManager() {
        devcammentClient = AWSManager.getInstance().getDevcammentClient();
        executorService = Executors.newSingleThreadExecutor();
    }

    public ShowApi getShowApi() {
        return new ShowApi(executorService, devcammentClient);
    }

}
