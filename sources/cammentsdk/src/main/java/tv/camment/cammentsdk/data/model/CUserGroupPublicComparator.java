package tv.camment.cammentsdk.data.model;

import java.util.Comparator;

public final class CUserGroupPublicComparator implements Comparator<CUserGroup> {

    @Override
    public int compare(CUserGroup lhs, CUserGroup rhs) {
        return rhs.getIsPublic().compareTo(lhs.getIsPublic());
    }

}
