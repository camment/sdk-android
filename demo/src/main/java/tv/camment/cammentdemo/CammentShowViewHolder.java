package tv.camment.cammentdemo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Show;

import tv.camment.cammentsdk.CammentSDK;


class CammentShowViewHolder extends RecyclerView.ViewHolder {

    private final CammentShowsAdapter.ActionListener actionListener;

    private ImageView ivShowThumbnail;
    private Show show;

    CammentShowViewHolder(View itemView, CammentShowsAdapter.ActionListener actionListener) {
        super(itemView);
        this.actionListener = actionListener;

        ivShowThumbnail = (ImageView) itemView.findViewById(R.id.iv_show_thumbnail);

        ivShowThumbnail.setOnClickListener(clickListener);
    }

    void bindData(Show show) {
        this.show = show;

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(show.getThumbnail())
                .into(ivShowThumbnail);
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (actionListener != null) {
                actionListener.onShowClick(show);
            }
        }
    };

}
