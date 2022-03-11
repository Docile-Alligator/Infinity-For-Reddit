package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class RedditSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;
    private static final int REDDIT_SECTION_ITEMS = 2;

    private BaseActivity baseActivity;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int primaryIconColor;
    private boolean collapseRedditSection;
    private NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

    public RedditSectionRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                            SharedPreferences navigationDrawerSharedPreferences,
                                            NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        primaryIconColor = customThemeWrapper.getPrimaryIconColor();
        collapseRedditSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_REDDIT_SECTION, false);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_MENU_GROUP_TITLE : VIEW_TYPE_MENU_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MENU_GROUP_TITLE) {
            return new MenuGroupTitleViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nav_drawer_menu_group_title, parent, false));
        } else {
            return new MenuItemViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nav_drawer_menu_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuGroupTitleViewHolder) {
            ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_reddit);
            if (collapseRedditSection) {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
            }

            holder.itemView.setOnClickListener(view -> {
                if (collapseRedditSection) {
                    collapseRedditSection = !collapseRedditSection;
                    notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, REDDIT_SECTION_ITEMS);
                } else {
                    collapseRedditSection = !collapseRedditSection;
                    notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, REDDIT_SECTION_ITEMS);
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;

            switch (position) {
                case 1:
                    stringId = R.string.rpan;
                    drawableId = R.drawable.ic_rpan_24dp;
                    break;
                case 2:
                    stringId = R.string.trending;
                    drawableId = R.drawable.ic_trending_24dp;
                    break;
            }

            ((MenuItemViewHolder) holder).menuTextView.setText(stringId);
            ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
            int finalStringId = stringId;
            holder.itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
        }
    }

    @Override
    public int getItemCount() {
        return collapseRedditSection ? 1 : REDDIT_SECTION_ITEMS + 1;
    }

    class MenuGroupTitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title_text_view_item_nav_drawer_menu_group_title)
        TextView titleTextView;
        @BindView(R.id.collapse_indicator_image_view_item_nav_drawer_menu_group_title)
        ImageView collapseIndicatorImageView;

        MenuGroupTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                titleTextView.setTypeface(baseActivity.typeface);
            }
            titleTextView.setTextColor(secondaryTextColor);
            collapseIndicatorImageView.setColorFilter(secondaryTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view_item_nav_drawer_menu_item)
        ImageView imageView;
        @BindView(R.id.text_view_item_nav_drawer_menu_item)
        TextView menuTextView;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                menuTextView.setTypeface(baseActivity.typeface);
            }
            menuTextView.setTextColor(primaryTextColor);
            imageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
