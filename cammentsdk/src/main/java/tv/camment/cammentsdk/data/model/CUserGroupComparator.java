package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public final class CUserGroupComparator implements Comparator<CUserGroup> {

    @Override
    public int compare(CUserGroup lhs, CUserGroup rhs) {
        return Long.compare(Long.valueOf(rhs.getTimestamp()), Long.valueOf(lhs.getTimestamp()));
    }

}
