package ml.docilealligator.infinityforreddit.Adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.R;

public class CustomizeThemeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_COLOR = 1;
    private static final int VIEW_TYPE_SWITCH = 2;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;

    public CustomizeThemeRecyclerViewAdapter() {
        customThemeSettingsItems = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < customThemeSettingsItems.size() - 3) {
            return VIEW_TYPE_COLOR;
        }

        return VIEW_TYPE_SWITCH;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SWITCH) {
            return new ThemeSwitchItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_switch_item, parent, false));
        }

        return new ThemeColorItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_color_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThemeColorItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position);
            ((ThemeColorItemViewHolder) holder).themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeColorItemViewHolder) holder).themeItemInfoTextView.setText(customThemeSettingsItem.itemDetails);
            ((ThemeColorItemViewHolder) holder).colorImageView.setBackgroundTintList(ColorStateList.valueOf(customThemeSettingsItem.colorValue));
            holder.itemView.setOnClickListener(view -> {

            });
        } else if (holder instanceof ThemeSwitchItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position);
            ((ThemeSwitchItemViewHolder) holder).themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).themeItemInfoTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.setChecked(customThemeSettingsItem.isEnabled);
            ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.setOnClickListener(view -> customThemeSettingsItem.isEnabled = ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.isChecked());
            holder.itemView.setOnClickListener(view -> ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.performClick());
        }
    }

    @Override
    public int getItemCount() {
        return customThemeSettingsItems.size();
    }

    public void setCustomThemeSettingsItem(ArrayList<CustomThemeSettingsItem> customThemeSettingsItems) {
        this.customThemeSettingsItems.clear();
        notifyDataSetChanged();
        this.customThemeSettingsItems.addAll(customThemeSettingsItems);
        notifyDataSetChanged();
    }

    class ThemeColorItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.color_image_view_item_custom_theme_color_item)
        View colorImageView;
        @BindView(R.id.theme_item_name_text_view_item_custom_theme_color_item)
        TextView themeItemNameTextView;
        @BindView(R.id.theme_item_info_text_view_item_custom_theme_color_item)
        TextView themeItemInfoTextView;

        ThemeColorItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ThemeSwitchItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.theme_item_name_text_view_item_custom_theme_switch_item)
        TextView themeItemNameTextView;
        @BindView(R.id.theme_item_info_text_view_item_custom_theme_switch_item)
        TextView themeItemInfoTextView;
        @BindView(R.id.theme_item_switch_item_custom_theme_switch_item)
        Switch themeItemSwitch;

        ThemeSwitchItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
