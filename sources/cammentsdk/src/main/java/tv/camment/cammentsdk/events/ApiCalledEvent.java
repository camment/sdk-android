package tv.camment.cammentsdk.events;

import tv.camment.cammentsdk.api.ApiCallType;

public final class ApiCalledEvent {

    private final ApiCallType apiCallType;

    public ApiCalledEvent(ApiCallType apiCallType) {
        this.apiCallType = apiCallType;
    }

    public ApiCallType getApiCallType() {
        return apiCallType;
    }

}
