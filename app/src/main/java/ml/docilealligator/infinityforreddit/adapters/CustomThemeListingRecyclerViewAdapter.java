package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CustomThemeListingActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CustomThemeOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.ItemPredefinedCustomThemeBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemUserCustomThemeBinding;

public class CustomThemeListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PREDEFINED_THEME = 0;
    private static final int VIEW_TYPE_USER_THME = 1;
    private static final int VIEW_TYPE_PREDEFINED_THEME_DIVIDER = 2;
    private static final int VIEW_TYPE_USER_THEME_DIVIDER = 3;

    private final BaseActivity activity;
    private final ArrayList<CustomTheme> predefinedCustomThemes;
    private ArrayList<CustomTheme> userCustomThemes;

    public CustomThemeListingRecyclerViewAdapter(BaseActivity activity, ArrayList<CustomTheme> predefinedCustomThemes) {
        this.activity = activity;
        this.predefinedCustomThemes = predefinedCustomThemes;
        userCustomThemes = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_PREDEFINED_THEME_DIVIDER;
        } else if (position < 1 + predefinedCustomThemes.size()) {
            return VIEW_TYPE_PREDEFINED_THEME;
        } else if (position == 1 + predefinedCustomThemes.size()) {
            return VIEW_TYPE_USER_THEME_DIVIDER;
        } else {
            return VIEW_TYPE_USER_THME;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PREDEFINED_THEME_DIVIDER:
                return new PreDefinedThemeDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_type_divider, parent, false));
            case VIEW_TYPE_PREDEFINED_THEME:
                return new PredefinedCustomThemeViewHolder(ItemPredefinedCustomThemeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_USER_THEME_DIVIDER:
                return new UserThemeDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_type_divider, parent, false));
            default:
                return new UserCustomThemeViewHolder(ItemUserCustomThemeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PredefinedCustomThemeViewHolder) {
            CustomTheme customTheme = predefinedCustomThemes.get(position - 1);
            ((PredefinedCustomThemeViewHolder) holder).binding.colorPrimaryItemPredefinedCustomTheme.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((PredefinedCustomThemeViewHolder) holder).binding.nameTextViewItemPredefinedCustomTheme.setText(customTheme.name);
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
                intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
                activity.startActivity(intent);
            });
        } else if (holder instanceof UserCustomThemeViewHolder) {
            CustomTheme customTheme = userCustomThemes.get(position - predefinedCustomThemes.size() - 2);
            ((UserCustomThemeViewHolder) holder).binding.colorPrimaryItemUserCustomTheme.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((UserCustomThemeViewHolder) holder).binding.nameTextViewItemUserCustomTheme.setText(customTheme.name);
            ((UserCustomThemeViewHolder) holder).binding.addImageViewItemUserCustomTheme.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                intent.putExtra(CustomizeThemeActivity.EXTRA_CREATE_THEME, true);
                activity.startActivity(intent);
            });
            ((UserCustomThemeViewHolder) holder).binding.shareImageViewItemUserCustomTheme.setOnClickListener(view -> {
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
        } else if (holder instanceof PreDefinedThemeDividerViewHolder) {
            ((TextView) holder.itemView).setText(R.string.predefined_themes);
        } else if (holder instanceof UserThemeDividerViewHolder) {
            ((TextView) holder.itemView).setText(R.string.user_themes);
        }
    }

    @Override
    public int getItemCount() {
        return predefinedCustomThemes.size() + userCustomThemes.size() + 2;
    }

    public void setUserThemes(List<CustomTheme> userThemes) {
        userCustomThemes = (ArrayList<CustomTheme>) userThemes;
        notifyDataSetChanged();
    }

    class PredefinedCustomThemeViewHolder extends RecyclerView.ViewHolder {
        ItemPredefinedCustomThemeBinding binding;

        PredefinedCustomThemeViewHolder(@NonNull ItemPredefinedCustomThemeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.nameTextViewItemPredefinedCustomTheme.setTextColor(activity.customThemeWrapper.getPrimaryTextColor());
            binding.addImageViewItemPredefinedCustomTheme.setColorFilter(activity.customThemeWrapper.getPrimaryIconColor());
            if (activity.typeface != null) {
                binding.nameTextViewItemPredefinedCustomTheme.setTypeface(activity.typeface);
            }
        }
    }

    class UserCustomThemeViewHolder extends RecyclerView.ViewHolder {
        ItemUserCustomThemeBinding binding;

        UserCustomThemeViewHolder(@NonNull ItemUserCustomThemeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.nameTextViewItemUserCustomTheme.setTextColor(activity.customThemeWrapper.getPrimaryTextColor());
            binding.addImageViewItemUserCustomTheme.setColorFilter(activity.customThemeWrapper.getPrimaryIconColor());
            binding.shareImageViewItemUserCustomTheme.setColorFilter(activity.customThemeWrapper.getPrimaryIconColor());
            if (activity.typeface != null) {
                binding.nameTextViewItemUserCustomTheme.setTypeface(activity.typeface);
            }
        }
    }

    class PreDefinedThemeDividerViewHolder extends RecyclerView.ViewHolder {

        PreDefinedThemeDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ((TextView) itemView).setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
            if (activity.typeface != null) {
                ((TextView) itemView).setTypeface(activity.typeface);
            }
        }
    }

    class UserThemeDividerViewHolder extends RecyclerView.ViewHolder {

        UserThemeDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ((TextView) itemView).setTextColor(activity.customThemeWrapper.getSecondaryTextColor());
            if (activity.typeface != null) {
                ((TextView) itemView).setTypeface(activity.typeface);
            }
        }
    }
}
