package tv.camment.cammentdemo;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camment.clientsdk.model.Show;

import java.util.List;

final class CammentShowsAdapter extends RecyclerView.Adapter {

    private static final int SHOW = 0;

    private final ActionListener actionListener;

    private List<Show> shows;

    CammentShowsAdapter(ActionListener actionListener) {
        this.actionListener = actionListener;
        setHasStableIds(true);
    }

    void setData(List<Show> shows) {
        this.shows = shows;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return SHOW;
    }

    @Override
    public long getItemId(int position) {
        return shows.get(position).getUuid().hashCode();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case SHOW:
                itemView = inflater.inflate(R.layout.camment_show_item, parent, false);
                return new CammentShowViewHolder(itemView, actionListener);
        }
        throw new IllegalArgumentException("unsupported viewholder type");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SHOW:
                ((CammentShowViewHolder) holder).bindData(shows.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getShowListSize();
    }

    private int getShowListSize() {
        return shows != null ? shows.size() : 0;
    }

    interface ActionListener {

        void onShowClick(Show show);

    }

}
