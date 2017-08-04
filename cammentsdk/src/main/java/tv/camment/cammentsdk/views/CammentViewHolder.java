package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Camment;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import tv.camment.cammentsdk.R;


public class CammentViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private Camment camment;

    private ImageView ivThumbnail;
    private TextureView textureView;

    public CammentViewHolder(View itemView, final CammentsAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
        textureView = itemView.findViewById(R.id.texture_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camment != null && actionListener != null) {
                    actionListener.onCammentClick(camment, textureView);
                }
            }
        });
    }

    public void bindData(Camment camment) {
        if (camment == null)
            return;

        this.camment = camment;

        Glide.with(itemView.getContext()).load(camment.getThumbnail()).into(ivThumbnail);
    }

}
