package tv.camment.cammentsdk.events;

import tv.camment.cammentsdk.auth.CammentUserInfo;

public final class UserInfoChangedEvent {

    private final CammentUserInfo userInfo;

    public UserInfoChangedEvent(CammentUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public CammentUserInfo getUserInfo() {
        return userInfo;
    }

}