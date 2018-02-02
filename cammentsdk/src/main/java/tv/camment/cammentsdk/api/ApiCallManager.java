package tv.camment.cammentsdk.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class ApiCallManager {

    private static ApiCallManager INSTANCE;

    private Map<ApiCallType, Set<Integer>> apiCallMap;

    static ApiCallManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiCallManager();
        }
        return INSTANCE;
    }

    private ApiCallManager() {
        apiCallMap = new HashMap<>();
    }

    boolean canCall(ApiCallType apiCallType, int apiCallHash) {
        boolean canCall = true;

        if (apiCallMap != null
                && apiCallMap.containsKey(apiCallType)) {
            Set<Integer> hashSet = apiCallMap.get(apiCallType);
            if (hashSet.contains(apiCallHash)) {
                canCall = false;
            }
        }

        if (canCall) {
            addCall(apiCallType, apiCallHash);
        }

        return canCall;
    }

    private void addCall(ApiCallType apiCallType, int apiCallHash) {
        if (apiCallMap == null) {
            apiCallMap = new HashMap<>();
        }

        Set<Integer> hashSet;

        if (apiCallMap.containsKey(apiCallType)) {
            hashSet = apiCallMap.get(apiCallType);
            if (hashSet == null) {
                hashSet = new HashSet<>();
            }
            hashSet.add(apiCallHash);
        } else {
            hashSet = new HashSet<>();
            hashSet.add(apiCallHash);
        }

        apiCallMap.put(apiCallType, hashSet);
    }

    void removeCall(ApiCallType apiCallType, int apiCallHash) {
        if (apiCallMap == null)
            return;

        if (apiCallMap.containsKey(apiCallType)) {
            Set<Integer> hashSet = apiCallMap.get(apiCallType);
            if (hashSet.contains(apiCallHash)) {
                hashSet.remove(apiCallHash);
            }
        }
    }

}
