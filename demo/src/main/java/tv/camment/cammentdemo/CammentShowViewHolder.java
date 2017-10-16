package tv.camment.cammentdemo;

import android.graphics.PorterDuff;
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

        itemView.setOnClickListener(clickListener);
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
                ivShowThumbnail.setColorFilter(itemView.getResources().getColor(R.color.cmmsdk_camment_button_grey), PorterDuff.Mode.MULTIPLY);
                itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivShowThumbnail.clearColorFilter();
                    }
                }, 100);
                actionListener.onShowClick(show);
            }
        }
    };

}
