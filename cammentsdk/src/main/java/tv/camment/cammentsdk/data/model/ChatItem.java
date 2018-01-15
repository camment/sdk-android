package tv.camment.cammentsdk.data.model;


public final class ChatItem<T> {

    private final ChatItemType type;
    private final String uuid;
    private final long timestamp;
    private final T content;

    public ChatItem(ChatItemType type, String uuid, long timestamp, T content) {
        this.type = type;
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.content = content;
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode() + content.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || o.getClass() != getClass())
            return false;

        ChatItem c = (ChatItem) o;

        return c.getUuid().equals(getUuid());
    }

    public ChatItemType getType() {
        return type;
    }

    public String getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public T getContent() {
        return content;
    }

    public enum ChatItemType {
        CAMMENT,
        AD
    }

}
