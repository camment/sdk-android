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

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.CCammentComparator;


final class CammentsAdapter extends RecyclerView.Adapter {

    private static final int CAMMENT = 0;

    private static long hashCode;

    private final ActionListener actionListener;

    private List<CCamment> camments;

    CammentsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<CCamment> camments) {
        if (camments == null) {
            this.camments = null;
            notifyDataSetChanged();
            return;
        }

        if (this.camments != null
                && this.camments.size() == camments.size()
                && hashCode == camments.hashCode()) {
            Log.d("CAMMENT", "will not update");
            return;
        }

        hashCode = camments.hashCode();

        Set<CCamment> cammentSet = new HashSet<>();
        cammentSet.addAll(camments);

        this.camments = new ArrayList<>();

        this.camments.addAll(cammentSet);
        Collections.sort(this.camments, new CCammentComparator());

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return camments.get(position).getUuid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return CAMMENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case CAMMENT:
                itemView = inflater.inflate(R.layout.cmmsdk_camment_item, parent, false);
                return new CammentViewHolder(itemView, actionListener);
        }
        throw new IllegalArgumentException("unsupported viewholder type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CAMMENT:
                ((CammentViewHolder) holder).bindData(camments.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getCammentListSize();
    }

    private int getCammentListSize() {
        return camments != null ? camments.size() : 0;
    }

    interface ActionListener {

        void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView);

        void onCammentBottomSheetDisplayed();

        void stopCammentIfPlaying(Camment camment);

    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {

    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof CammentViewHolder) {
            ((CammentViewHolder) holder).setItemViewScale(SDKConfig.CAMMENT_SMALL);
            ((CammentViewHolder) holder).stopCammentIfPlaying();
        }
    }

}