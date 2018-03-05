package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
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

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.data.model.ChatItemComparator;


final class CammentsAdapter extends RecyclerView.Adapter {

    private static final int CAMMENT = 0;
    private static final int AD = 1;

    private static long hashCode;

    private final ActionListener actionListener;

    private List<ChatItem> items;

    private List<ChatItem<CCamment>> camments;
    private List<ChatItem<AdMessage>> ads;

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

    void setAdsData(List<ChatItem<AdMessage>> adsList) {
        if (adsList == null || adsList.size() == 0) {
            this.ads = new ArrayList<>();
            items = new ArrayList<>();
            if (camments != null) {
                items.addAll(this.camments);
            }
            notifyDataSetChanged();
            return;
        }

        Set<ChatItem<AdMessage>> adSet = new HashSet<>();
        adSet.addAll(adsList);

        this.ads = new ArrayList<>();

        this.ads.addAll(adSet);
        Collections.sort(this.ads, new ChatItemComparator());

        //start allItems
        items = new ArrayList<>();
        items.addAll(this.ads);
        if (camments != null) {
            items.addAll(camments);
        }
        Collections.sort(this.items, new ChatItemComparator());
        //end allItems

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUuid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).getType()) {
            case AD:
                return AD;
            case CAMMENT:
            default:
                return CAMMENT;
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
        return items != null ? items.size() : 0;
    }

    interface ActionListener {

        void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView);

        void onCammentBottomSheetDisplayed();

        void stopCammentIfPlaying(Camment camment);

        void onAdClick(ChatItem<AdMessage> adMessage);

        void onCloseAdClick(ChatItem<AdMessage> chatItem);
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