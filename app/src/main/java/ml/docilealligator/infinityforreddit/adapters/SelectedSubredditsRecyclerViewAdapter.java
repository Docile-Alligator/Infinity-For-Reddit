package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemSelectedSubredditBinding;
import ml.docilealligator.infinityforreddit.multireddit.ExpandedSubredditInMultiReddit;

public class SelectedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final BaseActivity activity;
    private final CustomThemeWrapper customThemeWrapper;
    private final RequestManager glide;
    private final ArrayList<ExpandedSubredditInMultiReddit> subreddits;

    public SelectedSubredditsRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                                 RequestManager glide,
                                                 ArrayList<ExpandedSubredditInMultiReddit> subreddits) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.glide = glide;
        if (subreddits == null) {
            this.subreddits = new ArrayList<>();
        } else {
            this.subreddits = subreddits;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(ItemSelectedSubredditBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            glide.load(subreddits.get(holder.getBindingAdapterPosition()).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) holder).binding.iconImageViewItemSelectedSubreddit);
            ((SubredditViewHolder) holder).binding.subredditNameItemSelectedSubreddit.setText(subreddits.get(holder.getBindingAdapterPosition()).getName());
            ((SubredditViewHolder) holder).binding.deleteImageViewItemSelectedSubreddit.setOnClickListener(view -> {
                subreddits.remove(holder.getBindingAdapterPosition());
                notifyItemRemoved(holder.getBindingAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return subreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).binding.iconImageViewItemSelectedSubreddit);
        }
    }

    public void addSubreddits(ArrayList<ExpandedSubredditInMultiReddit> newSubreddits) {
        int oldSize = subreddits.size();
        subreddits.addAll(newSubreddits);
        notifyItemRangeInserted(oldSize, newSubreddits.size());
    }

    public void addUserInSubredditType(String username) {
        subreddits.add(new ExpandedSubredditInMultiReddit(username, null));
        notifyItemInserted(subreddits.size());
    }

    public ArrayList<ExpandedSubredditInMultiReddit> getSubreddits() {
        return subreddits;
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {
        ItemSelectedSubredditBinding binding;

        public SubredditViewHolder(@NonNull ItemSelectedSubredditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.subredditNameItemSelectedSubreddit.setTextColor(customThemeWrapper.getPrimaryIconColor());
            binding.deleteImageViewItemSelectedSubreddit.setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);

            if (activity.typeface != null) {
                binding.subredditNameItemSelectedSubreddit.setTypeface(activity.typeface);
            }
        }
    }
}
