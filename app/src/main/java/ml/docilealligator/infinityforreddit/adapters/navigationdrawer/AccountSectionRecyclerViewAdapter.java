package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import ml.docilealligator.infinityforreddit.activities.InboxActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class AccountSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;
    private static final int ACCOUNT_SECTION_ITEMS = 4;
    private static final int ANONYMOUS_ACCOUNT_SECTION_ITEMS = 2;

    private BaseActivity baseActivity;
    private int inboxCount;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int primaryIconColor;
    private boolean collapseAccountSection;
    private boolean isLoggedIn;
    private NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

    public AccountSectionRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                             SharedPreferences navigationDrawerSharedPreferences, boolean isLoggedIn,
                                             NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        primaryIconColor = customThemeWrapper.getPrimaryIconColor();
        collapseAccountSection = navigationDrawerSharedPreferences.getBoolean(SharedPreferencesUtils.COLLAPSE_ACCOUNT_SECTION, false);
        this.isLoggedIn = isLoggedIn;
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
            ((MenuGroupTitleViewHolder) holder).titleTextView.setText(R.string.label_account);
            if (collapseAccountSection) {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).collapseIndicatorImageView.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
            }

            holder.itemView.setOnClickListener(view -> {
                if (collapseAccountSection) {
                    collapseAccountSection = !collapseAccountSection;
                    notifyItemRangeInserted(holder.getBindingAdapterPosition() + 1, isLoggedIn ? ACCOUNT_SECTION_ITEMS : ANONYMOUS_ACCOUNT_SECTION_ITEMS);
                } else {
                    collapseAccountSection = !collapseAccountSection;
                    notifyItemRangeRemoved(holder.getBindingAdapterPosition() + 1, isLoggedIn ? ACCOUNT_SECTION_ITEMS : ANONYMOUS_ACCOUNT_SECTION_ITEMS);
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;
            boolean setOnClickListener = true;

            if (isLoggedIn) {
                switch (position) {
                    case 1:
                        stringId = R.string.profile;
                        drawableId = R.drawable.ic_account_circle_24dp;
                        break;
                    case 2:
                        stringId = R.string.subscriptions;
                        drawableId = R.drawable.ic_subscritptions_bottom_app_bar_24dp;
                        break;
                    case 3:
                        stringId = R.string.multi_reddit;
                        drawableId = R.drawable.ic_multi_reddit_24dp;
                        break;
                    default:
                        setOnClickListener = false;
                        if (inboxCount > 0) {
                            ((MenuItemViewHolder) holder).menuTextView.setText(baseActivity.getString(R.string.inbox_with_count, inboxCount));
                        } else {
                            ((MenuItemViewHolder) holder).menuTextView.setText(R.string.inbox);
                        }
                        ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_inbox_24dp));
                        holder.itemView.setOnClickListener(view -> {
                            Intent intent = new Intent(baseActivity, InboxActivity.class);
                            baseActivity.startActivity(intent);
                        });
                        break;

                }
            } else {
                if (position == 1) {
                    stringId = R.string.subscriptions;
                    drawableId = R.drawable.ic_subscritptions_bottom_app_bar_24dp;
                } else {
                    stringId = R.string.multi_reddit;
                    drawableId = R.drawable.ic_multi_reddit_24dp;
                }
            }

            if (stringId != 0) {
                ((MenuItemViewHolder) holder).menuTextView.setText(stringId);
                ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
            }
            if (setOnClickListener) {
                int finalStringId = stringId;
                holder.itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
            }
        }
    }

    @Override
    public int getItemCount() {
        return collapseAccountSection ? 1 : (isLoggedIn ? ACCOUNT_SECTION_ITEMS + 1 : ANONYMOUS_ACCOUNT_SECTION_ITEMS + 1);
    }

    public void setInboxCount(int inboxCount) {
        this.inboxCount = inboxCount;
        notifyDataSetChanged();
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
