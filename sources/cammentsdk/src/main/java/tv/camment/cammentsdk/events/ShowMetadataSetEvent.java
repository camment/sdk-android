package tv.camment.cammentsdk.events;


import tv.camment.cammentsdk.api.ApiManager;

public final class ShowMetadataSetEvent {

    public ShowMetadataSetEvent() {
        ApiManager.getInstance().getUserApi().getMyUserGroups();
    }
    
}
