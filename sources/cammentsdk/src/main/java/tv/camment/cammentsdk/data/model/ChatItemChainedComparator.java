package tv.camment.cammentsdk.data.model;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class ChatItemChainedComparator implements Comparator<ChatItem> {

    private List<Comparator<ChatItem>> comparatorList;

    public ChatItemChainedComparator(Comparator<ChatItem>... comparators) {
        this.comparatorList = Arrays.asList(comparators);
    }

    @Override
    public int compare(ChatItem lhs, ChatItem rhs) {
        for (Comparator<ChatItem> comparator : comparatorList) {
            int result = comparator.compare(lhs, rhs);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

}
