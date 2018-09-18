package tv.camment.cammentsdk.events;

import tv.camment.cammentsdk.api.ApiCallType;

public final class ApiResultEvent {

    private final ApiCallType apiCallType;
    private final boolean success;


    public ApiResultEvent(ApiCallType apiCallType, boolean success) {
        this.apiCallType = apiCallType;
        this.success = success;
    }

    public ApiCallType getApiCallType() {
        return apiCallType;
    }

    public boolean isSuccess() {
        return success;
    }

}
