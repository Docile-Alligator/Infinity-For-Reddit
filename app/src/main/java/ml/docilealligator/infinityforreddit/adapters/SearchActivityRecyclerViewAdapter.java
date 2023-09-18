package ml.docilealligator.infinityforreddit.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemRecentSearchQueryBinding;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SearchActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private List<RecentSearchQuery> recentSearchQueries;
    private int primaryTextColor;
    private Drawable historyIcon;
    private Drawable deleteIcon;
    private ItemOnClickListener itemOnClickListener;

    public interface ItemOnClickListener {
        void onClick(String query);
        void onDelete(RecentSearchQuery recentSearchQuery);
    }

    public SearchActivityRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                             ItemOnClickListener itemOnClickListener) {
        this.activity = activity;
        this.primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        this.historyIcon = Utils.getTintedDrawable(activity, R.drawable.ic_history_24dp, customThemeWrapper.getPrimaryIconColor());
        this.deleteIcon = Utils.getTintedDrawable(activity, R.drawable.ic_delete_24dp, customThemeWrapper.getPrimaryIconColor());
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
                ((RecentSearchQueryViewHolder) holder).binding.recentSearchQueryTextViewItemRecentSearchQuery.setText(recentSearchQueries.get(position).getSearchQuery());
            }
        }
    }

    @Override
    public int getItemCount() {
        return recentSearchQueries == null ? 0 : recentSearchQueries.size();
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

            binding.recentSearchQueryTextViewItemRecentSearchQuery.setTextColor(primaryTextColor);
            binding.recentSearchQueryTextViewItemRecentSearchQuery.setCompoundDrawablesWithIntrinsicBounds(historyIcon, null, null, null);
            binding.deleteButtonItemRecentSearchQuery.setIcon(deleteIcon);

            if (activity.typeface != null) {
                binding.recentSearchQueryTextViewItemRecentSearchQuery.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                    itemOnClickListener.onClick(recentSearchQueries.get(getBindingAdapterPosition()).getSearchQuery());
                }
            });

            binding.deleteButtonItemRecentSearchQuery.setOnClickListener(view -> {
                itemOnClickListener.onDelete(recentSearchQueries.get(getBindingAdapterPosition()));
            });
        }
    }
}
