package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserGroupComparator;


final class UserGroupAdapter extends RecyclerView.Adapter {

    private static final int USER_GROUP = 0;

    private final ActionListener actionListener;

    private List<CUserGroup> userGroups;

    UserGroupAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CUserGroup> userGroups) {
        Set<CUserGroup> userGroupSet = new HashSet<>();
        userGroupSet.addAll(userGroups);

        this.userGroups = new ArrayList<>();

        this.userGroups.addAll(userGroupSet);
        Collections.sort(this.userGroups, new CUserGroupComparator());

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return userGroups.get(position).getUuid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return USER_GROUP;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case USER_GROUP:
                itemView = inflater.inflate(R.layout.cmmsdk_usergroup_item, parent, false);
                return new UserGroupViewHolder(itemView, actionListener);
        }
        throw new IllegalArgumentException("unsupported viewholder type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case USER_GROUP:
                ((UserGroupViewHolder) holder).bindData(userGroups.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getUserGroupListSize();
    }

    private int getUserGroupListSize() {
        return userGroups != null ? userGroups.size() : 0;
    }

    interface ActionListener {

        void onUserGroupClick(CUserGroup userGroup);

    }

}