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
import tv.camment.cammentsdk.data.model.UserState;


final class UserInfoAdapter extends RecyclerView.Adapter {

    private static final int ACTIVE_USER = 0;
    private static final int BLOCKED_SECTION = 1;
    private static final int BLOCKED_USER = 2;

    private final ActionListener actionListener;

    private List<CUserInfo> userInfos;
    private int sectionPosition = -1;
    private boolean isMyGroup;

    UserInfoAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CUserInfo> userInfos, boolean isMyGroup) {
        this.isMyGroup = isMyGroup;

        sectionPosition = -1;

        if (userInfos != null) {

            Set<CUserInfo> userInfoSet = new HashSet<>();
            userInfoSet.addAll(userInfos);

            this.userInfos = new ArrayList<>();

            this.userInfos.addAll(userInfoSet);

            Collections.sort(this.userInfos, new CUserInfoChainedComparator(new CUserInfoNameComparator(), new CUserInfoStateComparator()));

            for (int i = 0; i < this.userInfos.size(); i++) {
                if (this.userInfos.get(i).getUserState() == UserState.BLOCKED) {
                    sectionPosition = i;
                    break;
                }
            }

        } else {
            this.userInfos = new ArrayList<>();
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (position == sectionPosition) {
            return 12345;
        }
        if (sectionPosition > -1 && position > sectionPosition) {
            return userInfos.get(position - 1).getUserCognitoIdentityId().hashCode();
        }
        return userInfos.get(position).getUserCognitoIdentityId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == sectionPosition) {
            return BLOCKED_SECTION;
        }
        if (sectionPosition > -1 && position > sectionPosition) {
            return BLOCKED_USER;
        }
        return ACTIVE_USER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case BLOCKED_SECTION:
                itemView = inflater.inflate(R.layout.cmmsdk_userinfo_blocked_section, parent, false);
                return new EmptyViewHolder(itemView);
            case BLOCKED_USER:
                itemView = inflater.inflate(R.layout.cmmsdk_userinfo_blocked_item, parent, false);
                return new UserInfoBlockedViewHolder(itemView, actionListener);
            case ACTIVE_USER:
            default:
                itemView = inflater.inflate(R.layout.cmmsdk_userinfo_active_item, parent, false);
                return new UserInfoActiveViewHolder(itemView, actionListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ACTIVE_USER:
                ((UserInfoActiveViewHolder) holder).bindData(userInfos.get(position), isMyGroup);
                break;
            case BLOCKED_USER:
                ((UserInfoBlockedViewHolder) holder).bindData(userInfos.get(position - 1), isMyGroup);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getUserInfoListSize() + (sectionPosition > -1 ? 1 : 0);
    }

    private int getUserInfoListSize() {
        return userInfos != null ? userInfos.size() : 0;
    }

    interface ActionListener {

        void onUserBlockClick(CUserInfo userInfo);

    }

}