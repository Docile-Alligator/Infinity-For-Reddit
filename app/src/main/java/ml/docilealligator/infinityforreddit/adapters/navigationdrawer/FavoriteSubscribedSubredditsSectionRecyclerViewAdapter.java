package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuGroupTitleBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerSubscribedThingBinding;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class FavoriteSubscribedSubredditsSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT = 2;

    private final BaseActivity baseActivity;
    private final RequestManager glide;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private boolean collapseFavoriteSubredditsSection;
    private final boolean hideFavoriteSubredditsSection;
    private final NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;
    private ArrayList<SubscribedSubredditData> favoriteSubscribedSubreddits = new ArrayList<>();

    public FavoriteSubscribedSubredditsSectionRecyclerViewAdapter(BaseActivity baseActivity, RequestManager glide,
                                                                  CustomThemeWrapper customThemeWrapper,
                                                                  SharedPreferences navigationDrawerSharedPreferences,
                                                                  NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        collapseFavoriteSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_FAVORITE_SUBREDDITS_SECTION, false);
        hideFavoriteSubredditsSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAVORITE_SUBREDDITS_SECTION, false);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_MENU_GROUP_TITLE : VIEW_TYPE_FAVORITE_SUBSCRIBED_SUBREDDIT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MENU_GROUP_TITLE) {
            return new MenuGroupTitleViewHolder(ItemNavDrawerMenuGroupTitleBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new FavoriteSubscribedThingViewHolder(ItemNavDrawerSubscribedThingBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuGroupTitleViewHolder) {
            ((MenuGroupTitleViewHolder) holder).binding.titleTextViewItemNavDrawerMenuGroupTitle.setText(R.string.favorites);
            if (collapseFavoriteSubredditsSection) {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
            }

            holder.itemView.setOnClickListener(view -> {
                if (collapseFavoriteSubredditsSection) {
                    collapseFavoriteSubredditsSection = !collapseFavoriteSubredditsSection;
                    notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, favoriteSubscribedSubreddits.size());
                } else {
                    collapseFavoriteSubredditsSection = !collapseFavoriteSubredditsSection;
                    notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, favoriteSubscribedSubreddits.size());
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof FavoriteSubscribedThingViewHolder) {
            SubscribedSubredditData subreddit = favoriteSubscribedSubreddits.get(position - 1);
            String subredditName = subreddit.getName();
            String iconUrl = subreddit.getIconUrl();
            ((FavoriteSubscribedThingViewHolder) holder).binding.thingNameTextViewItemNavDrawerSubscribedThing.setText(subredditName);
            if (iconUrl != null && !iconUrl.equals("")) {
                glide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((FavoriteSubscribedThingViewHolder) holder).binding.thingIconGifImageViewItemNavDrawerSubscribedThing);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteSubscribedThingViewHolder) holder).binding.thingIconGifImageViewItemNavDrawerSubscribedThing);
            }

            holder.itemView.setOnClickListener(view -> {
                itemClickListener.onSubscribedSubredditClick(subredditName);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (hideFavoriteSubredditsSection) {
            return 0;
        }
        return favoriteSubscribedSubreddits.isEmpty() ? 0 : (collapseFavoriteSubredditsSection ? 1 : favoriteSubscribedSubreddits.size() + 1);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof FavoriteSubscribedThingViewHolder) {
            glide.clear(((FavoriteSubscribedThingViewHolder) holder).binding.thingIconGifImageViewItemNavDrawerSubscribedThing);
        }
    }

    public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubreddits) {
        this.favoriteSubscribedSubreddits = (ArrayList<SubscribedSubredditData>) favoriteSubscribedSubreddits;
        notifyDataSetChanged();
    }

    class MenuGroupTitleViewHolder extends RecyclerView.ViewHolder {
        ItemNavDrawerMenuGroupTitleBinding binding;

        MenuGroupTitleViewHolder(@NonNull ItemNavDrawerMenuGroupTitleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (baseActivity.typeface != null) {
                binding.titleTextViewItemNavDrawerMenuGroupTitle.setTypeface(baseActivity.typeface);
            }
            binding.titleTextViewItemNavDrawerMenuGroupTitle.setTextColor(secondaryTextColor);
            binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setColorFilter(secondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class FavoriteSubscribedThingViewHolder extends RecyclerView.ViewHolder {
        ItemNavDrawerSubscribedThingBinding binding;

        FavoriteSubscribedThingViewHolder(@NonNull ItemNavDrawerSubscribedThingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (baseActivity.typeface != null) {
                binding.thingNameTextViewItemNavDrawerSubscribedThing.setTypeface(baseActivity.typeface);
            }
            binding.thingNameTextViewItemNavDrawerSubscribedThing.setTextColor(primaryTextColor);
        }
    }
}
