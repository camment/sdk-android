package tv.camment.cammentsdk.api;

import com.camment.clientsdk.DevcammentClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.aws.AWSManager;


public final class ApiManager {

    private static ApiManager INSTANCE;

    private final DevcammentClient devcammentClient;
    private final ExecutorService executorService;

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

    public UserApi getUserApi() {
        return new UserApi(executorService, devcammentClient);
    }

    public GroupApi getGroupApi() {
        return new GroupApi(executorService, devcammentClient);
    }

    public InvitationApi getInvitationApi() {
        return new InvitationApi(executorService, devcammentClient);
    }

    public CammentApi getCammentApi() {
        return new CammentApi(executorService, devcammentClient);
    }

}
