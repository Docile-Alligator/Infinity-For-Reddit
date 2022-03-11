package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
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

public class PreferenceSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;
    private static final int PREFERENCES_SECTION_ITEMS = 3;

    private BaseActivity baseActivity;
    private Resources resources;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int primaryIconColor;
    private boolean isNSFWEnabled;
    private boolean collapsePreferencesSection;
    private NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

    public PreferenceSectionRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                                String accountName, SharedPreferences nsfwAndSpoilerSharedPreferences,
                                                SharedPreferences navigationDrawerSharedPreferences,
                                                NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        resources = baseActivity.getResources();
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        primaryIconColor = customThemeWrapper.getPrimaryIconColor();
        isNSFWEnabled = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        collapsePreferencesSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_PREFERENCES_SECTION, false);
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
            ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_preferences);
            if (collapsePreferencesSection) {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
            }

            holder.itemView.setOnClickListener(view -> {
                if (collapsePreferencesSection) {
                    collapsePreferencesSection = !collapsePreferencesSection;
                    notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, PREFERENCES_SECTION_ITEMS);
                } else {
                    collapsePreferencesSection = !collapsePreferencesSection;
                    notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, PREFERENCES_SECTION_ITEMS);
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;
            boolean setOnClickListener = true;

            switch (position) {
                case 1:
                    if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                        stringId = R.string.dark_theme;
                        drawableId = R.drawable.ic_dark_theme_24dp;
                    } else {
                        stringId = R.string.light_theme;
                        drawableId = R.drawable.ic_light_theme_24dp;
                    }
                    break;
                case 2:
                    setOnClickListener = false;
                    if (isNSFWEnabled) {
                        stringId = R.string.disable_nsfw;
                        drawableId = R.drawable.ic_nsfw_off_24dp;
                    } else {
                        stringId = R.string.enable_nsfw;
                        drawableId = R.drawable.ic_nsfw_on_24dp;
                    }

                    holder.itemView.setOnClickListener(view -> {
                        if (isNSFWEnabled) {
                            isNSFWEnabled = false;
                            ((MenuItemViewHolder) holder).menuTextView.setText(R.string.enable_nsfw);
                            ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_on_24dp));
                            itemClickListener.onMenuClick(R.string.disable_nsfw);
                        } else {
                            isNSFWEnabled = true;
                            ((MenuItemViewHolder) holder).menuTextView.setText(R.string.disable_nsfw);
                            ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_nsfw_off_24dp));
                            itemClickListener.onMenuClick(R.string.enable_nsfw);
                        }
                    });
                    break;
                case 3:
                    stringId = R.string.settings;
                    drawableId = R.drawable.ic_settings_24dp;
            }

            if (stringId != 0) {
                ((MenuItemViewHolder) holder).menuTextView.setText(stringId);
                ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
                if (setOnClickListener) {
                    int finalStringId = stringId;
                    holder.itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return collapsePreferencesSection ? 1 : PREFERENCES_SECTION_ITEMS + 1;
    }

    public void setNSFWEnabled(boolean isNSFWEnabled) {
        this.isNSFWEnabled = isNSFWEnabled;
        notifyItemChanged(3);
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
