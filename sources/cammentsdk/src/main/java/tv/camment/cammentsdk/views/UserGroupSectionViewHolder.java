package tv.camment.cammentsdk.views;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tv.camment.cammentsdk.R;

final class UserGroupSectionViewHolder extends RecyclerView.ViewHolder {

    private TextView tvSection;

    public UserGroupSectionViewHolder(View itemView) {
        super(itemView);

        tvSection = itemView.findViewById(R.id.cmmsdk_tv_section);
    }

    void bindData(@StringRes int stringRes) {
        tvSection.setText(stringRes);
    }

}
