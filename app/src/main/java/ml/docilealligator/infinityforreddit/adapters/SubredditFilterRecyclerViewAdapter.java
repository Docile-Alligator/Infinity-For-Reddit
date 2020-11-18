package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;

public class SubredditFilterRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SubredditFilter> subredditFilters;
    private ItemClickListener itemClickListener;

    public SubredditFilterRecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemDelete(SubredditFilter subredditFilter);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditNameViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_subreddit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditNameViewHolder) {
            ((SubredditNameViewHolder) holder).subredditRedditNameTextView.setText(subredditFilters.get(position).getSubredditName());
        }
    }

    @Override
    public int getItemCount() {
        return subredditFilters == null ? 0 : subredditFilters.size();
    }

    public void updateSubredditsName(List<SubredditFilter> subredditFilters) {
        this.subredditFilters = subredditFilters;
        notifyDataSetChanged();
    }

    class SubredditNameViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subreddit_name_item_selected_subreddit)
        TextView subredditRedditNameTextView;
        @BindView(R.id.delete_image_view_item_selected_subreddit)
        ImageView deleteImageView;

        public SubredditNameViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            deleteImageView.setOnClickListener(view -> {
                itemClickListener.onItemDelete(subredditFilters.get(getAdapterPosition()));
            });
        }
    }

}
