package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemRecentSearchQueryBinding;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;

public class SearchActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final BaseActivity activity;
    private List<RecentSearchQuery> recentSearchQueries;
    private final int filledCardViewBackgroundColor;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final int subredditTextColor;
    private final int userTextColor;
    private final ItemOnClickListener itemOnClickListener;

    public interface ItemOnClickListener {
        void onClick(RecentSearchQuery recentSearchQuery);
        void onDelete(RecentSearchQuery recentSearchQuery);
    }

    public SearchActivityRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                             ItemOnClickListener itemOnClickListener) {
        this.activity = activity;
        this.filledCardViewBackgroundColor = customThemeWrapper.getFilledCardViewBackgroundColor();
        this.primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        this.secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        this.subredditTextColor = customThemeWrapper.getSubreddit();
        this.userTextColor = customThemeWrapper.getUsername();
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentSearchQueryViewHolder(ItemRecentSearchQueryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentSearchQueryViewHolder) {
            if (recentSearchQueries != null && !recentSearchQueries.isEmpty() && position < recentSearchQueries.size()) {
                RecentSearchQuery recentSearchQuery = recentSearchQueries.get(position);
                ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryTextViewItemRecentSearchQuery.setText(recentSearchQuery.getSearchQuery());
                if (recentSearchQuery.getSearchInSubredditOrUserName() != null && !recentSearchQuery.getSearchInSubredditOrUserName().isEmpty()) {
                    if (recentSearchQuery.isSearchInIsUser()) {
                        ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                                .setTextColor(userTextColor);
                        ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                                .setText("u/" + recentSearchQuery.getSearchQuery());
                    } else {
                        ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                                .setTextColor(subredditTextColor);
                        ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                                .setText("r/" + recentSearchQuery.getSearchQuery());
                    }
                } else {
                    ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                            .setTextColor(secondaryTextColor);
                    ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery
                            .setText(R.string.all_subreddits);
                }
                holder.itemView.postDelayed(() -> {
                    ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryTextViewItemRecentSearchQuery.setSelected(true);
                    ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery.setSelected(true);
                }, 1000);
            }
        }
    }

    @Override
    public int getItemCount() {
        return recentSearchQueries == null ? 0 : recentSearchQueries.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof RecentSearchQueryViewHolder) {
            ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryTextViewItemRecentSearchQuery.setSelected(false);
            ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryWhereTextViewItemRecentSearchQuery.setSelected(false);
        }
    }

    public void setRecentSearchQueries(List<RecentSearchQuery> recentSearchQueries) {
        this.recentSearchQueries = recentSearchQueries;
        notifyDataSetChanged();
    }

    class RecentSearchQueryViewHolder extends RecyclerView.ViewHolder {
        ItemRecentSearchQueryBinding binding;

        public RecentSearchQueryViewHolder(@NonNull ItemRecentSearchQueryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setBackgroundTintList(ColorStateList.valueOf(filledCardViewBackgroundColor));

            binding.recentSearchQueryTextViewItemRecentSearchQuery.setTextColor(primaryTextColor);

            if (activity.typeface != null) {
                binding.recentSearchQueryTextViewItemRecentSearchQuery.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                    itemOnClickListener.onClick(recentSearchQueries.get(getBindingAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(view -> {
                itemOnClickListener.onDelete(recentSearchQueries.get(getBindingAdapterPosition()));
                return true;
            });
        }
    }
}