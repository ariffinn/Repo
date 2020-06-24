package my.doubleclick.repo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL  = 1;

    private final List<Item> mItems;

    private boolean isLoaderVisible = false;
    private Context mCtx;

    public Adapter(List<Item> items) {
        this.mItems = items;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mCtx = parent.getContext();
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                                                    .inflate(R.layout.item_post,
                                                             parent,
                                                             false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                    LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_loading, parent,
                                           false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder,
                                 final int position) {
        holder.onBind(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mItems.get(position).getUrl()));
                mCtx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == mItems.size() - 1? VIEW_TYPE_LOADING:
                VIEW_TYPE_NORMAL;
        }
        else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null? 0: mItems.size();
    }

    /**
     * Inserts new items.
     *
     * @param items the list of items.
     */
    public void addItems(List<Item> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Displays progress bar, adds new item and refresh the recycle view.
     */
    public void addLoading() {
        isLoaderVisible = true;
        mItems.add(new Item());
        notifyItemInserted(mItems.size() - 1);
    }

    /**
     * Hides progress bar and remove item from recycle view.
     */
    public void removeLoading() {
        isLoaderVisible = false;
        int position = mItems.size() - 1;
        Item item = getItem(position);
        if (item != null) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Clears item list and refresh recycle view.
     */
    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Gets item by position.
     *
     * @param position the list position.
     * @return the item.
     */
    Item getItem(int position) {
        return mItems.get(position);
    }

    /**
     * The ViewHolder design pattern to access each list item view without the
     * need for the look up, saving valuable processor cycles.
     */
    public class ViewHolder extends BaseViewHolder {
        final TextView tvName;
        final TextView tvUpdatedDt;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvUpdatedDt = itemView.findViewById(R.id.tvUpdatedDt);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            Item item = mItems.get(position);
            tvName.setText(item.getFullName());
            tvUpdatedDt.setText(item.getUpdatedDate());
        }
    }

    public static class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }
}
