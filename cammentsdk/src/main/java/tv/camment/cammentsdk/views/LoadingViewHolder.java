package tv.camment.cammentsdk.views;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;


final class LoadingViewHolder extends RecyclerView.ViewHolder {

    LoadingViewHolder(View itemView) {
        super(itemView);

        ProgressBar loadingBar = (ProgressBar) itemView.findViewById(R.id.cmmsdk_loadingbar);
        loadingBar.getIndeterminateDrawable().setColorFilter(itemView.getResources().getColor(R.color.cmmsdk_camment_grey),
                PorterDuff.Mode.SRC_IN);
        loadingBar.setScaleX(0.5f);
        loadingBar.setScaleY(0.5f);
    }

}
