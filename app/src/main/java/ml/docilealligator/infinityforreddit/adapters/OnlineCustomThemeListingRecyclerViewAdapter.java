package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomThemeListingActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CustomThemeOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.ItemUserCustomThemeBinding;

public class OnlineCustomThemeListingRecyclerViewAdapter extends PagingDataAdapter<CustomTheme, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER_THEME = 1;
    private static final int VIEW_TYPE_USER_THEME_DIVIDER = 2;

    private final BaseActivity activity;

    public OnlineCustomThemeListingRecyclerViewAdapter(BaseActivity activity) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull CustomTheme oldItem, @NonNull CustomTheme newItem) {
                return oldItem.name.equals(newItem.name);
            }

            @Override
            public boolean areContentsTheSame(@NonNull CustomTheme oldItem, @NonNull CustomTheme newItem) {
                return false;
            }
        });
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_USER_THEME;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_USER_THEME_DIVIDER:
                return new OnlineCustomThemeDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_type_divider, parent, false));
            default:
                return new OnlineCustomThemeViewHolder(ItemUserCustomThemeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OnlineCustomThemeViewHolder) {
            CustomTheme customTheme = getItem(position);
            ((OnlineCustomThemeViewHolder) holder).binding.colorPrimaryItemUserCustomTheme.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((OnlineCustomThemeViewHolder) holder).binding.nameTextViewItemUserCustomTheme.setText(customTheme.name);
            ((OnlineCustomThemeViewHolder) holder).binding.addImageViewItemUserCustomTheme.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
                activity.startActivity(intent);
            });
            ((OnlineCustomThemeViewHolder) holder).binding.shareImageViewItemUserCustomTheme.setOnClickListener(view -> {
                ((CustomThemeListingActivity) activity).shareTheme(customTheme);
            });
            holder.itemView.setOnClickListener(view -> {
                CustomThemeOptionsBottomSheetFragment customThemeOptionsBottomSheetFragment = new CustomThemeOptionsBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(CustomThemeOptionsBottomSheetFragment.EXTRA_THEME_NAME, customTheme.name);
                customThemeOptionsBottomSheetFragment.setArguments(bundle);
                customThemeOptionsBottomSheetFragment.show(activity.getSupportFragmentManager(), customThemeOptionsBottomSheetFragment.getTag());
            });
            holder.itemView.setOnLongClickListener(view -> {
                holder.itemView.performClick();
                return true;
            });
        }
    }

    class OnlineCustomThemeViewHolder extends RecyclerView.ViewHolder {
        ItemUserCustomThemeBinding binding;

        OnlineCustomThemeViewHolder(@NonNull ItemUserCustomThemeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (activity.typeface != null) {
                binding.nameTextViewItemUserCustomTheme.setTypeface(activity.typeface);
            }
        }
    }

    class OnlineCustomThemeDividerViewHolder extends RecyclerView.ViewHolder {

        OnlineCustomThemeDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            if (activity.typeface != null) {
                ((TextView) itemView).setTypeface(activity.typeface);
            }
        }
    }
}
