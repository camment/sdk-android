package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public final class CUserGroupTimestampComparator implements Comparator<CUserGroup> {

    @Override
    public int compare(CUserGroup lhs, CUserGroup rhs) {
        return Long.compare(rhs.getLongTimestamp(), lhs.getLongTimestamp());
    }

}
