package tv.camment.cammentsdk.views.pullable;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

public final class PullableView extends BasePullableView {

    public PullableView(Context context) {
        super(context);
    }

    public PullableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addBoundView(BoundView boundView) {
        super.addBoundView(boundView);
    }

    public void setListener(PullListener listener) {
        super.setListener(listener);
    }

    public interface PullListener extends BasePullListener {

    }

}
