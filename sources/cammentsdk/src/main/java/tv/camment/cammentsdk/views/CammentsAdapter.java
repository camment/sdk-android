package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Camment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.data.model.ChatItemChainedComparator;
import tv.camment.cammentsdk.data.model.ChatItemShowAtComparator;
import tv.camment.cammentsdk.data.model.ChatItemTimestampComparator;


final class CammentsAdapter extends RecyclerView.Adapter {

    private static final int CAMMENT = 0;
    private static final int LOADING = 1;

    private static long hashCode;

    private final ActionListener actionListener;

    private List<ChatItem<CCamment>> camments;
    private Map<Integer, List<ChatItem<CCamment>>> showAtCamments;
    private boolean isLoading;

    private int maxShowAt;

    CammentsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<ChatItem<CCamment>> camments) {
        if (camments == null) {
            this.camments = null;
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

        Set<ChatItem<CCamment>> cammentSet = new HashSet<>(camments);

        List<ChatItem<CCamment>> sortedCamments = new ArrayList<>(cammentSet);

        if (CammentSDK.getInstance().isSyncEnabled()) {
            Collections.sort(sortedCamments, new ChatItemChainedComparator(new ChatItemShowAtComparator(), new ChatItemTimestampComparator()));

            showAtCamments = new HashMap<>();

            List<ChatItem<CCamment>> chatItems;

            for (ChatItem<CCamment> camment : sortedCamments) {
                chatItems = showAtCamments.get(camment.getShowAt());
                if (chatItems == null) {
                    chatItems = new ArrayList<>();
                    showAtCamments.put(camment.getShowAt(), chatItems);
                }
                chatItems.add(camment);
            }

            CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();

            int currentPosition = -1;
            if (cammentPlayerListener != null) {
                currentPosition = Math.round(cammentPlayerListener.getCurrentPosition() / 1000f);
            }

            selectCammentsBasedOnShowAt(currentPosition);
        } else {
            Collections.sort(sortedCamments, new ChatItemTimestampComparator());

            int prevHash = -1;
            if (this.camments != null) {
                prevHash = this.camments.hashCode();
            }

            this.camments = sortedCamments;

            if (prevHash != this.camments.hashCode()) {
                notifyDataSetChanged();
            }
        }
    }

    private void selectCammentsBasedOnShowAt(int currentPosition) {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            int prevHash = -1;
            if (camments != null) {
                prevHash = camments.hashCode();
            }

            camments = new ArrayList<>();

            for (Map.Entry<Integer, List<ChatItem<CCamment>>> entry : showAtCamments.entrySet()) {
                if (entry.getKey() <= currentPosition) {
                    camments.addAll(entry.getValue());

                    if (maxShowAt < entry.getKey()) {
                        maxShowAt = entry.getKey();
                    }
                }
            }

            Collections.sort(camments, new ChatItemChainedComparator(new ChatItemShowAtComparator(), new ChatItemTimestampComparator()));

            if (prevHash != camments.hashCode()) {
                notifyDataSetChanged();
            }
        }
    }

    void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        if (isLoading) {
            notifyItemInserted(getItemCount());
        } else {
            notifyItemRemoved(getItemCount());
        }
    }

    @Override
    public long getItemId(int position) {
        if (camments != null && position < camments.size()) {
            return camments.get(position).getUuid().hashCode();
        } else {
            return 1111L;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (camments != null && position < camments.size()) {
            return CAMMENT;
        } else {
            return LOADING;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
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
                ((CammentViewHolder) holder).bindData(camments.get(position).getContent());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (camments != null ? camments.size() : 0) + (isLoading ? 1 : 0);
    }

    public void checkDisplayedCamments() {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();

            int currentPosition = -1;
            if (cammentPlayerListener != null) {
                currentPosition = Math.round(cammentPlayerListener.getCurrentPosition() / 1000f);
            }

            selectCammentsBasedOnShowAt(currentPosition);
        }
    }

    interface ActionListener {

        void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView);

        void onCammentBottomSheetDisplayed();

        void stopCammentIfPlaying(Camment camment);

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