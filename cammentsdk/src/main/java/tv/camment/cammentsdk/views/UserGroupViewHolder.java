package tv.camment.cammentsdk.views;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserGroup;


final class UserGroupViewHolder extends RecyclerView.ViewHolder {

    private final UserGroupAdapter.ActionListener actionListener;
    private CUserGroup usergroup;

    private TextView tvGroup;

    UserGroupViewHolder(final View itemView, final UserGroupAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        tvGroup = (TextView) itemView.findViewById(R.id.cmmsdk_tv_group);

        itemView.setOnClickListener(itemOnClickListener);
    }

    void bindData(CUserGroup usergroup) {
        if (usergroup == null)
            return;

        this.usergroup = usergroup;

        tvGroup.setText(usergroup.getUuid());
        tvGroup.setTypeface(tvGroup.getTypeface(), usergroup.isActive() ? Typeface.BOLD : Typeface.NORMAL);
    }

    private View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (actionListener != null) {
                actionListener.onUserGroupClick(usergroup);
            }
        }
    };

}