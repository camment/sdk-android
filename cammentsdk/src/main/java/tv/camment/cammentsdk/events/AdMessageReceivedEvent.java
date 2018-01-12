package tv.camment.cammentsdk.events;


import tv.camment.cammentsdk.aws.messages.AdMessage;

public final class AdMessageReceivedEvent {

    private final AdMessage adMessage;
    private final long timestamp;

    public AdMessageReceivedEvent(AdMessage adMessage, long timestamp) {
        this.adMessage = adMessage;
        this.timestamp = timestamp;
    }

    public AdMessage getAdMessage() {
        return adMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
