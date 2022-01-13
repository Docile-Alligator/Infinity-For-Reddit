package ml.docilealligator.infinityforreddit.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.customviews.ColorPickerDialog;

public class CustomizeThemeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_COLOR = 1;
    private static final int VIEW_TYPE_SWITCH = 2;
    private static final int VIEW_TYPE_THEME_NAME = 3;
    private BaseActivity activity;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private String themeName;
    private boolean isPredefinedTheme;

    public CustomizeThemeRecyclerViewAdapter(BaseActivity activity, String themeName,
                                             boolean isPredefinedTheme) {
        this.activity = activity;
        customThemeSettingsItems = new ArrayList<>();
        this.themeName = themeName;
        this.isPredefinedTheme = isPredefinedTheme;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_THEME_NAME;
        } else if (position > 3 && position < customThemeSettingsItems.size() - 2) {
            return VIEW_TYPE_COLOR;
        }

        return VIEW_TYPE_SWITCH;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SWITCH) {
            return new ThemeSwitchItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_switch_item, parent, false));
        } else if (viewType == VIEW_TYPE_THEME_NAME) {
            return new ThemeNameItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_name, parent, false));
        }

        return new ThemeColorItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme_color_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThemeColorItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position - 1);
            ((ThemeColorItemViewHolder) holder).themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeColorItemViewHolder) holder).themeItemInfoTextView.setText(customThemeSettingsItem.itemDetails);
            ((ThemeColorItemViewHolder) holder).colorImageView.setBackgroundTintList(ColorStateList.valueOf(customThemeSettingsItem.colorValue));
            holder.itemView.setOnClickListener(view -> {
                new ColorPickerDialog(activity, customThemeSettingsItem.colorValue, color -> {
                    customThemeSettingsItem.colorValue = color;
                    ((ThemeColorItemViewHolder) holder).colorImageView.setBackgroundTintList(ColorStateList.valueOf(color));
                }).show();
            });
        } else if (holder instanceof ThemeSwitchItemViewHolder) {
            CustomThemeSettingsItem customThemeSettingsItem = customThemeSettingsItems.get(position - 1);
            ((ThemeSwitchItemViewHolder) holder).themeItemNameTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).themeItemInfoTextView.setText(customThemeSettingsItem.itemName);
            ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.setChecked(customThemeSettingsItem.isEnabled);
            ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.setOnClickListener(view -> customThemeSettingsItem.isEnabled = ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.isChecked());
            holder.itemView.setOnClickListener(view -> ((ThemeSwitchItemViewHolder) holder).themeItemSwitch.performClick());
        } else if (holder instanceof ThemeNameItemViewHolder) {
            ((ThemeNameItemViewHolder) holder).themeNameTextView.setText(themeName);
            holder.itemView.setOnClickListener(view -> {
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
                EditText themeNameEditText = dialogView.findViewById(R.id.name_edit_text_edit_name_dialog);
                themeNameEditText.setText(themeName);
                themeNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_theme_name)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                            }
                            themeName = themeNameEditText.getText().toString();
                            ((ThemeNameItemViewHolder) holder).themeNameTextView.setText(themeName);
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                            }
                        })
                        .setOnDismissListener(dialogInterface -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(themeNameEditText.getWindowToken(), 0);
                            }
                        })
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return customThemeSettingsItems.size() + 1;
    }

    public void setCustomThemeSettingsItem(ArrayList<CustomThemeSettingsItem> customThemeSettingsItems) {
        this.customThemeSettingsItems.clear();
        notifyDataSetChanged();
        this.customThemeSettingsItems.addAll(customThemeSettingsItems);
        notifyDataSetChanged();
    }

    public String getThemeName() {
        return themeName;
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
            if (activity.typeface != null) {
                themeItemNameTextView.setTypeface(activity.typeface);
                themeItemInfoTextView.setTypeface(activity.typeface);
            }
        }
    }

    class ThemeSwitchItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.theme_item_name_text_view_item_custom_theme_switch_item)
        TextView themeItemNameTextView;
        @BindView(R.id.theme_item_info_text_view_item_custom_theme_switch_item)
        TextView themeItemInfoTextView;
        @BindView(R.id.theme_item_switch_item_custom_theme_switch_item)
        SwitchMaterial themeItemSwitch;

        ThemeSwitchItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (activity.typeface != null) {
                themeItemNameTextView.setTypeface(activity.typeface);
                themeItemInfoTextView.setTypeface(activity.typeface);
            }
        }
    }

    class ThemeNameItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.theme_name_text_view_item_theme_name)
        TextView themeNameTextView;
        @BindView(R.id.description_text_view_item_theme_name)
        TextView descriptionTextView;
        public ThemeNameItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (activity.typeface != null) {
                themeNameTextView.setTypeface(activity.typeface);
                descriptionTextView.setTypeface(activity.typeface);
            }
        }
    }
}
