package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.CammentSDK;
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
                handleOnItemClick();
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CammentBottomSheetDialog dialog = new CammentBottomSheetDialog(itemView.getContext());
                dialog.setCamment(camment);
                dialog.show();
                return true;
            }
        });

        setItemViewScale(0.5f);
    }

    private void handleOnItemClick() {
        if (camment != null && actionListener != null) {
            actionListener.onCammentClick(this, camment, textureView);
        }
    }

    public void setItemViewScale(float scale) {
        if (itemView instanceof SquareFrameLayout) {
            ((SquareFrameLayout) itemView).setCustomScale(scale);
            if (scale == 0.5f) {
                setThumbnailVisibility(View.VISIBLE);
            }
        }
    }

    public void setThumbnailVisibility(int visibility) {
        ivThumbnail.setVisibility(visibility);
    }

    public float getItemViewScale() {
        if (itemView instanceof SquareFrameLayout) {
            return ((SquareFrameLayout) itemView).getCustomScale();
        }
        return 1.0f;
    }

    public void bindData(Camment camment) {
        if (camment == null)
            return;

        this.camment = camment;

        Glide.with(CammentSDK.getInstance().getApplicationContext()).load(camment.getThumbnail()).into(ivThumbnail);
    }

}
