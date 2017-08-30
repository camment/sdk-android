package tv.camment.cammentdemo;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


public class CammentPrecachingLayoutManager extends LinearLayoutManager {

    private static final int EXTRA_LAYOUT_SPACE = 600;

    public CammentPrecachingLayoutManager(Context context) {
        super(context);
    }

    public CammentPrecachingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CammentPrecachingLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return EXTRA_LAYOUT_SPACE;
    }

}
