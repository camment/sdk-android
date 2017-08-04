package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class SquareTextureView extends TextureView {

    public SquareTextureView(Context context) {
        super(context);
    }

    public SquareTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

}
