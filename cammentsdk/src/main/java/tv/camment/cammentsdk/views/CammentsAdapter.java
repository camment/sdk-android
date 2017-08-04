package tv.camment.cammentsdk.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentList;

import tv.camment.cammentsdk.R;

/**
 * Created by petrushka on 04/08/2017.
 */

public class CammentsAdapter extends RecyclerView.Adapter {

    private static final int CAMMENT = 0;

    private final ActionListener actionListener;

    private CammentList cammentList;

    public CammentsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
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
                ((CammentViewHolder) holder).bindData(cammentList.getItems().get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getCammentListSize();
    }

    private int getCammentListSize() {
        return cammentList != null && cammentList.getItems() != null ? cammentList.getItems().size() : 0;
    }

    interface ActionListener {

        void onCammentClick(Camment camment);

    }

}
