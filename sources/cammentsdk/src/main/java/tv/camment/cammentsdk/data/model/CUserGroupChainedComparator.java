package tv.camment.cammentsdk.data.model;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class CUserGroupChainedComparator implements Comparator<CUserGroup> {

    private List<Comparator<CUserGroup>> comparatorList;

    public CUserGroupChainedComparator(Comparator<CUserGroup>... comparators) {
        this.comparatorList = Arrays.asList(comparators);
    }

    @Override
    public int compare(CUserGroup lhs, CUserGroup rhs) {
        for (Comparator<CUserGroup> comparator : comparatorList) {
            int result = comparator.compare(lhs, rhs);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

}
