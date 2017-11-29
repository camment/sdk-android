package tv.camment.cammentsdk.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import tv.camment.cammentsdk.SDKConfig;


public final class CammentRecyclerView extends RecyclerView {

    public CammentRecyclerView(Context context) {
        super(context);
    }

    public CammentRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CammentRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void show() {
        animate().translationX(0).alpha(1.0f).start();
    }

    public void hide() {
        animate().translationX(-getWidth() * 2).alpha(0.0f).start();
    }

    public void showSmallThumbnailsForAllChildren() {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            RecyclerView.ViewHolder holder = getChildViewHolder(getChildAt(i));
            if (holder != null && holder instanceof CammentViewHolder) {
                ((CammentViewHolder) holder).setItemViewScale(SDKConfig.CAMMENT_SMALL);
            }
        }
    }

}
