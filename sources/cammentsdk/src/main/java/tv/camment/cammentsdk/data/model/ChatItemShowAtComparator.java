package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public final class ChatItemShowAtComparator implements Comparator<ChatItem> {

    @Override
    public int compare(ChatItem lhs, ChatItem rhs) {
        return Integer.compare(rhs.getShowAt(), lhs.getShowAt());
    }

}
