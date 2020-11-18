package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.R;

public class SelectedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private CustomThemeWrapper customThemeWrapper;
    private ArrayList<String> subreddits;

    public SelectedSubredditsRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper, ArrayList<String> subreddits) {
        this.customThemeWrapper = customThemeWrapper;
        if (subreddits == null) {
            this.subreddits = new ArrayList<>();
        } else {
            this.subreddits = subreddits;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_subreddit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            ((SubredditViewHolder) holder).subredditNameTextView.setText(subreddits.get(holder.getAdapterPosition()));
            ((SubredditViewHolder) holder).deleteButton.setOnClickListener(view -> {
                subreddits.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return subreddits.size();
    }

    public void addSubreddits(ArrayList<String> newSubreddits) {
        int oldSize = subreddits.size();
        subreddits.addAll(newSubreddits);
        notifyItemRangeInserted(oldSize, newSubreddits.size());
    }

    public ArrayList<String> getSubreddits() {
        return subreddits;
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subreddit_name_item_selected_subreddit)
        TextView subredditNameTextView;
        @BindView(R.id.delete_image_view_item_selected_subreddit)
        ImageView deleteButton;

        public SubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            subredditNameTextView.setTextColor(customThemeWrapper.getPrimaryIconColor());
            deleteButton.setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
