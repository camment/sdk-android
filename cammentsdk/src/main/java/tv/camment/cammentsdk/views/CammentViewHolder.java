package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.camment.clientsdk.model.Camment;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import tv.camment.cammentsdk.R;


public class CammentViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private Camment camment;

    private ImageView ivThumbnail;
    private SimpleExoPlayerView exoPlayerView;

    public CammentViewHolder(View itemView, final CammentsAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
        exoPlayerView = itemView.findViewById(R.id.exo_player_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camment != null && actionListener != null) {
                    actionListener.onCammentClick(camment);
                }
            }
        });
    }

    public void bindData(Camment camment) {
        if (camment == null)
            return;

        this.camment = camment;
    }

}
