package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;

public final class ForceWrapImageView extends AppCompatImageView {

    public ForceWrapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        //noinspection SuspiciousNameCombination
        setMeasuredDimension(width, height);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width > 0 && height > 0) {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.width = width;
                params.height = height;
                setLayoutParams(params);
            }
        }
        super.setImageDrawable(drawable);
    }

}
