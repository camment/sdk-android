package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Userinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserGroupChainedComparator;
import tv.camment.cammentsdk.data.model.CUserGroupPublicComparator;
import tv.camment.cammentsdk.data.model.CUserGroupTimestampComparator;


final class UserGroupAdapter extends RecyclerView.Adapter {

    private static final int USER_GROUP = 0;
    private static final int SPECIAL_SECTION = 1;
    private static final int CHAT_SECTION = 2;

    private static long hashCode;

    private final ActionListener actionListener;

    private List<CUserGroup> userGroups;

    private int firstSpecialChatGroupIndex = -1;
    private int firstNormalChatGroupIndex = -1;

    UserGroupAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CUserGroup> userGroups) {
        if (userGroups == null) {
            this.userGroups = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }

        if (this.userGroups != null
                && this.userGroups.size() == userGroups.size()
                && hashCode == userGroups.hashCode()) {
            return;
        }

        firstSpecialChatGroupIndex = -1;
        firstNormalChatGroupIndex = -1;

        hashCode = userGroups.hashCode();

        int index = 0;
        int removed = 0;
        for (Iterator<CUserGroup> iterator = userGroups.iterator(); iterator.hasNext(); ) {
            CUserGroup userGroup = iterator.next();

            if (index > 0) {
                CUserGroup prevGroup = userGroups.get(index - 1 - removed);

                if (TextUtils.equals(userGroup.getUuid(), prevGroup.getUuid())) {
                    List<Userinfo> prevGroupUsers = new ArrayList<>(prevGroup.getUsers());
                    if (userGroup.getUsers() != null) {
                        prevGroupUsers.addAll(userGroup.getUsers());
                    }
                    prevGroup.setUsers(prevGroupUsers);
                    iterator.remove();
                    removed++;
                }
            }

            index++;
        }

        Set<CUserGroup> userGroupSet = new HashSet<>(userGroups);

        this.userGroups = new ArrayList<>(userGroupSet);

        Collections.sort(this.userGroups, new CUserGroupChainedComparator(new CUserGroupPublicComparator(), new CUserGroupTimestampComparator()));

        index = 0;
        for (CUserGroup userGroup : this.userGroups) {
            if (userGroup.getIsPublic() && firstSpecialChatGroupIndex == -1) {
                firstSpecialChatGroupIndex = index;
            } else if (!userGroup.getIsPublic() && firstNormalChatGroupIndex == -1) {
                firstNormalChatGroupIndex = index;
                break;
            }
            index++;
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        switch (getItemViewType(position)) {
            case SPECIAL_SECTION:
                return 1111L;
            case CHAT_SECTION:
                return 2222L;
        }
        return userGroups.get(getCorrectedItemPosition(position)).getUuid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == firstSpecialChatGroupIndex) {
            return SPECIAL_SECTION;
        } else if (firstSpecialChatGroupIndex == -1 && position == firstNormalChatGroupIndex) {
            return CHAT_SECTION;
        } else if (firstSpecialChatGroupIndex != -1 && position == firstNormalChatGroupIndex + 1) {
            return CHAT_SECTION;
        } else {
            return USER_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case SPECIAL_SECTION:
            case CHAT_SECTION:
                itemView = inflater.inflate(R.layout.cmmsdk_usergroup_section_item, parent, false);
                return new UserGroupSectionViewHolder(itemView);
            case USER_GROUP:
            default:
                itemView = inflater.inflate(R.layout.cmmsdk_usergroup_item, parent, false);
                return new UserGroupViewHolder(itemView, actionListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SPECIAL_SECTION:
                ((UserGroupSectionViewHolder) holder).bindData(R.string.cmmsdk_special_host);
                break;
            case CHAT_SECTION:
                ((UserGroupSectionViewHolder) holder).bindData(R.string.cmmsdk_camment_chat);
                break;
            case USER_GROUP:
                ((UserGroupViewHolder) holder).bindData(userGroups.get(getCorrectedItemPosition(position)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getUserGroupListSize() + (firstSpecialChatGroupIndex != -1 ? 1 : 0) + (firstNormalChatGroupIndex != -1 ? 1 : 0);
    }

    private int getUserGroupListSize() {
        return userGroups != null ? userGroups.size() : 0;
    }

    private int getCorrectedItemPosition(int position) {
        if (position <= firstNormalChatGroupIndex || firstNormalChatGroupIndex == -1) {
            return position - 1;
        } else {
            return firstSpecialChatGroupIndex != -1 ? position - 2 : position - 1;
        }
    }

    interface ActionListener {

        void onUserGroupClick(CUserGroup userGroup);

    }

}