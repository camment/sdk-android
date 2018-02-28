package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Camment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.data.model.ChatItemComparator;


final class CammentsAdapter extends RecyclerView.Adapter {

    private static final int CAMMENT = 0;
    private static final int AD = 1;
    private static final int LOADING = 2;

    private static long hashCode;

    private final ActionListener actionListener;

    private List<ChatItem> items;

    private List<ChatItem<CCamment>> camments;
    private List<ChatItem<AdMessage>> ads;
    private boolean isLoading;

    CammentsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<ChatItem<CCamment>> camments) {
        if (camments == null) {
            this.camments = null;
            this.ads = null;
            this.items = null;
            notifyDataSetChanged();
            return;
        }

        if (this.camments != null
                && this.camments.size() == camments.size()
                && hashCode == camments.hashCode()) {
            return;
        }

        hashCode = camments.hashCode();

        int beforeCount = this.camments == null ? 0 : this.camments.size();
        int afterCount = camments.size();

        if (beforeCount > afterCount
                && beforeCount - afterCount == 1
                && (beforeCount % SDKConfig.CAMMENT_PAGE_SIZE) == 0
                && actionListener != null) {
            actionListener.onLoadMoreIfPossible();
        }

        Set<ChatItem<CCamment>> cammentSet = new HashSet<>();
        cammentSet.addAll(camments);

        this.camments = new ArrayList<>();

        this.camments.addAll(cammentSet);
        Collections.sort(this.camments, new ChatItemComparator());

        //start allItems
        items = new ArrayList<>();
        items.addAll(this.camments);
        if (ads != null) {
            items.addAll(ads);
        }
        Collections.sort(this.items, new ChatItemComparator());
        //end allItems

        notifyDataSetChanged();
    }

    void addAdToList(AdMessage adMessage, long timestamp) {
        if (ads == null) {
            ads = new ArrayList<>();
        }

        ChatItem<AdMessage> ad = new ChatItem<>(ChatItem.ChatItemType.AD, UUID.randomUUID().toString(), timestamp, adMessage);
        ads.add(ad);

        //start allItems
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(ad);
        Collections.sort(this.items, new ChatItemComparator());
        //end allItems

        notifyDataSetChanged();
    }


    void removeAdFromList(ChatItem chatItem) {
        if (ads != null) {
            ads.remove(chatItem);
        }

        if (items != null) {
            items.remove(chatItem);
        }

        notifyDataSetChanged();
    }

    void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (items != null && position < items.size()) {
            return items.get(position).getUuid().hashCode();
        } else {
            return 1111L;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items != null && position < items.size()) {
            switch (items.get(position).getType()) {
                case AD:
                    return AD;
                case CAMMENT:
                default:
                    return CAMMENT;
            }
        } else {
            return LOADING;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case AD:
                itemView = inflater.inflate(R.layout.cmmsdk_ad_item, parent, false);
                return new AdViewHolder(itemView, actionListener);
            case LOADING:
                itemView = inflater.inflate(R.layout.cmmsdk_loading_item, parent, false);
                return new LoadingViewHolder(itemView);
            case CAMMENT:
            default:
                itemView = inflater.inflate(R.layout.cmmsdk_camment_item, parent, false);
                return new CammentViewHolder(itemView, actionListener);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CAMMENT:
                ((CammentViewHolder) holder).bindData((CCamment) items.get(position).getContent());
                break;
            case AD:
                ((AdViewHolder) holder).bindData(items.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (items != null ? items.size() : 0) + (isLoading ? 1 : 0);
    }

    interface ActionListener {

        void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView);

        void onCammentBottomSheetDisplayed();

        void stopCammentIfPlaying(Camment camment);

        void onAdClick(AdMessage adMessage);

        void onCloseAdClick(ChatItem chatItem);

        void onLoadMoreIfPossible();

    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {

    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        switch (holder.getItemViewType()) {
            case CAMMENT:
                ((CammentViewHolder) holder).setItemViewScale(SDKConfig.CAMMENT_SMALL);
                ((CammentViewHolder) holder).stopCammentIfPlaying();
                break;
        }
    }

}