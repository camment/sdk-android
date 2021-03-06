package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.ChatItem;


final class AdViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private ChatItem<AdMessage> adItem;

    private SquareFrameLayout sflContainer;

    private ImageView ivThumbnail;

    private ImageButton ibClose;

    AdViewHolder(View itemView, CammentsAdapter.ActionListener actionListener) {
        super(itemView);
        this.actionListener = actionListener;

        sflContainer = itemView.findViewById(R.id.cmmsdk_sfl_container);

        sflContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOnItemClick();
            }
        });

        ivThumbnail = itemView.findViewById(R.id.cmmsdk_iv_thumbnail);

        ibClose = itemView.findViewById(R.id.cmmsdk_ib_close);

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOnCloseClick();
            }
        });
    }

    private void handleOnItemClick() {
        if (actionListener != null
                && adItem != null) {
            //actionListener.onAdClick(adItem);
        }
    }

    private void handleOnCloseClick() {
        if (actionListener != null
                && adItem != null) {
            //actionListener.onCloseAdClick(adItem);
        }
    }

    void bindData(ChatItem<AdMessage> adItem) {
        if (adItem == null)
            return;

        this.adItem = adItem;

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(adItem.getContent().body.thumbnail)
                .into(ivThumbnail);
    }

}
