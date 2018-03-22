package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.utils.CommonUtils;

public final class SquareFrameLayout extends FrameLayout {

    private float customScale = SDKConfig.CAMMENT_SMALL;

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) (CommonUtils.dpToPx(CammentSDK.getInstance().getApplicationContext(), SDKConfig.CAMMENT_BIG_DP) * customScale);
        //int size = (int) (SDKConfig.CAMMENT_BIG_DP * customScale);

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCustomScale(float customScale) {
        if (this.customScale != customScale) {
            this.customScale = customScale;
            requestLayout();
        }
    }

    public float getCustomScale() {
        return customScale;
    }

}
