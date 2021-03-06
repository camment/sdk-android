package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public final class ChatItemTimestampComparator implements Comparator<ChatItem> {

    @Override
    public int compare(ChatItem lhs, ChatItem rhs) {
        return Long.compare(rhs.getTimestamp(), lhs.getTimestamp());
    }

}
