package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.R;


public class CammentViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private Camment camment;

    private ImageView ivThumbnail;
    private TextureView textureView;

    public CammentViewHolder(final View itemView, final CammentsAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        itemView.setPivotX(0);
        itemView.setPivotY(0);

        ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
        textureView = itemView.findViewById(R.id.texture_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camment != null && actionListener != null) {
                    setItemViewScale(getItemViewScale() == 0.5f ? 1.0f : 0.5f);

                    actionListener.onCammentClick(camment, textureView, ivThumbnail);
                }
            }
        });

        setItemViewScale(0.5f);
    }

    private void setItemViewScale(float scale) {
        if (itemView instanceof SquareFrameLayout) {
            ((SquareFrameLayout) itemView).setScale(scale);
        }
    }

    private float getItemViewScale() {
        if (itemView instanceof SquareFrameLayout) {
            return ((SquareFrameLayout) itemView).getScale();
        }
        return 1.0f;
    }

    public void bindData(Camment camment) {
        if (camment == null)
            return;

        this.camment = camment;

        Glide.with(itemView.getContext()).load(camment.getThumbnail()).into(ivThumbnail);
    }

}
