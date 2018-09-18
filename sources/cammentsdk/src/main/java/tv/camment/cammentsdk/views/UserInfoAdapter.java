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
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.data.model.CUserInfoChainedComparator;
import tv.camment.cammentsdk.data.model.CUserInfoNameComparator;
import tv.camment.cammentsdk.data.model.CUserInfoStateComparator;


final class UserInfoAdapter extends RecyclerView.Adapter {

    private static final int USER = 0;

    private final ActionListener actionListener;

    private List<CUserInfo> userInfos;
    private boolean isMyGroup;
    private String hostId;

    UserInfoAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CUserInfo> userInfos, boolean isMyGroup) {
        this.isMyGroup = isMyGroup;

        if (userInfos != null) {

            Set<CUserInfo> userInfoSet = new HashSet<>(userInfos);

            this.userInfos = new ArrayList<>();

            this.userInfos.addAll(userInfoSet);

            Collections.sort(this.userInfos, new CUserInfoChainedComparator(new CUserInfoStateComparator(), new CUserInfoNameComparator()));
        } else {
            this.userInfos = new ArrayList<>();
        }

        notifyDataSetChanged();
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return userInfos.get(position).getUserCognitoIdentityId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return USER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case USER:
            default:
                itemView = inflater.inflate(R.layout.cmmsdk_userinfo_item, parent, false);
                return new UserInfoViewHolder(itemView, actionListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case USER:
                ((UserInfoViewHolder) holder).bindData(userInfos.get(position), isMyGroup, hostId);
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

        void onUserBlockClick(CUserInfo userInfo);

    }

}