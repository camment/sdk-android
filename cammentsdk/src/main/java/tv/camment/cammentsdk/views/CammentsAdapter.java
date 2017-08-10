package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentList;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.CammentApi;

/**
 * Created by petrushka on 04/08/2017.
 */

public class CammentsAdapter extends RecyclerView.Adapter implements CammentApi.CammentListListener {

    private static final int CAMMENT = 0;

    private final ActionListener actionListener;

    private List<Camment> camments;

    public CammentsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    public void setData(List<Camment> camments) {
        this.camments = camments;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
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
                itemView = inflater.inflate(R.layout.cmmsdk_item_camment, parent, false);
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

    public void addCamment(Camment camment) {
        if (camments == null) {
            camments = new ArrayList<>();
        }
        camments.add(camment);
        notifyDataSetChanged();
    }

    @Override
    public void onCammentListRetrieved(CammentList cammentList) {
        if (cammentList != null) {
            if (camments == null) {
                camments = new ArrayList<>();
            }

            camments.addAll(cammentList.getItems());
            notifyDataSetChanged();
        }
    }

    interface ActionListener {

        void onCammentClick(Camment camment, TextureView textureView, ImageView ivThumbnail);

    }

}
