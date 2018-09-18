package tv.camment.cammentsdk.data.model;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class CUserInfoChainedComparator implements Comparator<CUserInfo> {

    private List<Comparator<CUserInfo>> comparatorList;

    public CUserInfoChainedComparator(Comparator<CUserInfo>... comparators) {
        this.comparatorList = Arrays.asList(comparators);
    }

    @Override
    public int compare(CUserInfo lhs, CUserInfo rhs) {
        for (Comparator<CUserInfo> comparator : comparatorList) {
            int result = comparator.compare(lhs, rhs);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

}
