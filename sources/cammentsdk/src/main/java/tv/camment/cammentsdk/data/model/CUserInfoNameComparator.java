package tv.camment.cammentsdk.data.model;


import java.util.Comparator;

public final class CUserInfoNameComparator implements Comparator<CUserInfo> {

    @Override
    public int compare(CUserInfo lhs, CUserInfo rhs) {
        return lhs.getName().compareToIgnoreCase(rhs.getName());
    }

}
