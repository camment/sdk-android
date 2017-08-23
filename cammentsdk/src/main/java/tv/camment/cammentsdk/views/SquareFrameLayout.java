package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public final class SquareFrameLayout extends FrameLayout {

    private float customScale = 1.0f;

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) (MeasureSpec.getSize(widthMeasureSpec) * customScale);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCustomScale(float customScale) {
        this.customScale = customScale;
        requestLayout();
    }

    public float getCustomScale() {
        return customScale;
    }

}
