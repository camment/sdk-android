package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.FacebookFriend;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.R;

class FbFriendsAdapter extends RecyclerView.Adapter
        implements FbFriendViewHolder.ActionListener {

    private static final int FB_FRIEND = 0;

    private final ActionListener actionListener;

    private List<FacebookFriend> facebookFriends;
    private List<FacebookFriend> selectedFacebookFriends;

    FbFriendsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    void setFacebookFriends(List<FacebookFriend> facebookFriends) {
        this.facebookFriends = facebookFriends;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return FB_FRIEND;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case FB_FRIEND:
                itemView = inflater.inflate(R.layout.cmmsdk_fb_friends_bottom_sheet_item, parent, false);
                return new FbFriendViewHolder(itemView, this);
        }
        throw new IllegalArgumentException("unsupported viewholder type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case FB_FRIEND:
                ((FbFriendViewHolder) holder).bindData(facebookFriends.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return facebookFriends == null ? 0 : facebookFriends.size();
    }

    @Override
    public void onFbFriendClick(FacebookFriend facebookFriend) {
        if (selectedFacebookFriends == null) {
            selectedFacebookFriends = new ArrayList<>();
        }

        if (selectedFacebookFriends.contains(facebookFriend)) {
            selectedFacebookFriends.remove(facebookFriend);
        } else {
            selectedFacebookFriends.add(facebookFriend);
        }

        if (actionListener != null) {
            actionListener.onFbFriendsSelectedChanged(selectedFacebookFriends.size() > 0);
        }
    }

    List<FacebookFriend> getSelectedFacebookFriends() {
        return selectedFacebookFriends;
    }

    interface ActionListener {

        void onFbFriendsSelectedChanged(boolean atLeastOneSelected);

    }

}
