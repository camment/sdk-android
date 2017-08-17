package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public class CCammentComparator implements Comparator<CCamment> {

    @Override
    public int compare(CCamment lhs, CCamment rhs) {
        return Long.compare(rhs.getTimestamp(), lhs.getTimestamp());
    }

}
