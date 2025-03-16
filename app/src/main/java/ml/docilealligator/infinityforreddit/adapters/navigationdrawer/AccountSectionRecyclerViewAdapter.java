package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.InboxActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuGroupTitleBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuItemBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class AccountSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MENU_GROUP_TITLE = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;
    private static final int ACCOUNT_SECTION_ITEMS = 5;
    private static final int ANONYMOUS_ACCOUNT_SECTION_ITEMS = 3;

    private final BaseActivity baseActivity;
    private int inboxCount;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final int primaryIconColor;
    private boolean collapseAccountSection;
    private final boolean isLoggedIn;
    private final NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

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
            return new MenuGroupTitleViewHolder(ItemNavDrawerMenuGroupTitleBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new MenuItemViewHolder(ItemNavDrawerMenuItemBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MenuGroupTitleViewHolder) {
            ((MenuGroupTitleViewHolder) holder).binding.titleTextViewItemNavDrawerMenuGroupTitle.setText(R.string.label_account);
            if (collapseAccountSection) {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24dp);
            } else {
                ((MenuGroupTitleViewHolder) holder).binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24dp);
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
                        drawableId = R.drawable.ic_account_circle_day_night_24dp;
                        break;
                    case 2:
                        stringId = R.string.subscriptions;
                        drawableId = R.drawable.ic_subscriptions_bottom_app_bar_day_night_24dp;
                        break;
                    case 3:
                        stringId = R.string.multi_reddit;
                        drawableId = R.drawable.ic_multi_reddit_day_night_24dp;
                        break;
                    case 4:
                        setOnClickListener = false;
                        if (inboxCount > 0) {
                            ((MenuItemViewHolder) holder).binding.textViewItemNavDrawerMenuItem.setText(baseActivity.getString(R.string.inbox_with_count, inboxCount));
                        } else {
                            ((MenuItemViewHolder) holder).binding.textViewItemNavDrawerMenuItem.setText(R.string.inbox);
                        }
                        ((MenuItemViewHolder) holder).binding.imageViewItemNavDrawerMenuItem.setImageDrawable(ContextCompat.getDrawable(baseActivity, R.drawable.ic_inbox_day_night_24dp));
                        holder.itemView.setOnClickListener(view -> {
                            Intent intent = new Intent(baseActivity, InboxActivity.class);
                            baseActivity.startActivity(intent);
                        });
                        break;
                    default:
                        stringId = R.string.history;
                        drawableId = R.drawable.ic_history_day_night_24dp;

                }
            } else {
                switch (position) {
                    case 1:
                        stringId = R.string.subscriptions;
                        drawableId = R.drawable.ic_subscriptions_bottom_app_bar_day_night_24dp;
                        break;
                    case 2:
                        stringId = R.string.multi_reddit;
                        drawableId = R.drawable.ic_multi_reddit_day_night_24dp;
                        break;
                    default:
                        stringId = R.string.history;
                        drawableId = R.drawable.ic_history_day_night_24dp;
                }
            }

            if (stringId != 0) {
                ((MenuItemViewHolder) holder).binding.textViewItemNavDrawerMenuItem.setText(stringId);
                ((MenuItemViewHolder) holder).binding.imageViewItemNavDrawerMenuItem.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
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
        if (inboxCount < 0) {
            this.inboxCount = Math.max(0, this.inboxCount + inboxCount);
        } else {
            this.inboxCount = inboxCount;
        }
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

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ItemNavDrawerMenuItemBinding binding;

        MenuItemViewHolder(@NonNull ItemNavDrawerMenuItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (baseActivity.typeface != null) {
                binding.textViewItemNavDrawerMenuItem.setTypeface(baseActivity.typeface);
            }
            binding.textViewItemNavDrawerMenuItem.setTextColor(primaryTextColor);
            binding.imageViewItemNavDrawerMenuItem.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
