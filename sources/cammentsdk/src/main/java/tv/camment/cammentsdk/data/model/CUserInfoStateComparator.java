package tv.camment.cammentsdk.data.model;


import java.util.Comparator;

public final class CUserInfoStateComparator implements Comparator<CUserInfo> {

    @Override
    public int compare(CUserInfo lhs, CUserInfo rhs) {
        return lhs.getUserState().compareTo(rhs.getUserState());
    }

}
