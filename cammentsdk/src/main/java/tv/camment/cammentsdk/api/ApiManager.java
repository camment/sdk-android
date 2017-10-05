package tv.camment.cammentsdk.api;

import com.camment.clientsdk.DevcammentClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.aws.AWSManager;


public final class ApiManager {

    private static ApiManager INSTANCE;

    private final DevcammentClient devcammentClient;
    private final ExecutorService executorService;

    private Map<String, Callable> callableMap;
    private Set<Callable> retrySet;

    public static ApiManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiManager();
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        INSTANCE = null;
    }

    private ApiManager() {
        devcammentClient = AWSManager.getInstance().getDevcammentClient();
        executorService = Executors.newSingleThreadExecutor();
        callableMap = new HashMap<>();
        retrySet = new HashSet<>();
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

    public synchronized void putCallable(String uuid, Callable call) {
        if (callableMap != null) {
            callableMap.put(uuid, call);
        }
    }

    public synchronized void removeCallable(String uuid) {
        if (callableMap != null
                && callableMap.containsKey(uuid)) {
            callableMap.remove(uuid);
        }
    }

    public synchronized void putRetryCallable(String uuid) {
        if (retrySet != null
                && callableMap.containsKey(uuid)) {
            retrySet.add(callableMap.get(uuid));
        }
    }

    public synchronized void retryFailedCallsIfNeeded() {
        if (retrySet != null
                && retrySet.size() > 0) {
            for (Callable call : retrySet) {
                executorService.submit(call);
            }
        }
        retrySet.clear();
    }

}