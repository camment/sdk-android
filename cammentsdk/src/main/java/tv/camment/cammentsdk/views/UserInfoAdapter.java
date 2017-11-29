package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserInfo;


final class UserInfoAdapter extends RecyclerView.Adapter {

    private static final int USER_INFO = 0;

    private final ActionListener actionListener;

    private List<CUserInfo> userInfos;

    UserInfoAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CUserInfo> userInfos) {
        if (userInfos != null) {

            Set<CUserInfo> userInfoSet = new HashSet<>();
            userInfoSet.addAll(userInfos);

            this.userInfos = new ArrayList<>();

            this.userInfos.addAll(userInfoSet);
        } else {
            this.userInfos = new ArrayList<>();
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return userInfos.get(position).getUserCognitoIdentityId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return USER_INFO;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case USER_INFO:
                itemView = inflater.inflate(R.layout.cmmsdk_drawer_user_info_item, parent, false);
                return new FbUserInfoViewHolder(itemView, actionListener);
        }
        throw new IllegalArgumentException("unsupported viewholder type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case USER_INFO:
                ((FbUserInfoViewHolder) holder).bindData(userInfos.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getUserInfoListSize();
    }

    private int getUserInfoListSize() {
        return userInfos != null ? userInfos.size() : 0;
    }

    interface ActionListener {

        void onUserRemoveClick(CUserInfo userInfo);

    }

}