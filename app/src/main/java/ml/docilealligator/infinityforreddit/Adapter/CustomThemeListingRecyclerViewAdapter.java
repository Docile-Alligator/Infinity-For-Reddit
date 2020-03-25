package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.R;

public class CustomThemeListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PREDEFINED_THEME = 0;
    private static final int VIEW_TYPE_USER_THME = 1;
    private static final int VIEW_TYPE_PREDEFINED_THEME_DIVIDER = 2;
    private static final int VIEW_TYPE_USER_THEME_DIVIDER = 3;

    private Context context;
    private ArrayList<CustomTheme> predefinedCustomThemes;
    private ArrayList<CustomTheme> userCustomThemes;

    public CustomThemeListingRecyclerViewAdapter(Context context, ArrayList<CustomTheme> predefinedCustomThemes) {
        this.context = context;
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
                return new PredefinedCustomThemeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_predefined_custom_theme, parent, false));
            case VIEW_TYPE_USER_THEME_DIVIDER:
                return new UserThemeDividerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_type_divider, parent, false));
            default:
                return new UserCustomThemeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_custom_theme, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PredefinedCustomThemeViewHolder) {
            CustomTheme customTheme = predefinedCustomThemes.get(position - 1);
            ((PredefinedCustomThemeViewHolder) holder).colorPrimaryView.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((PredefinedCustomThemeViewHolder) holder).nameTextView.setText(customTheme.name);
            ((PredefinedCustomThemeViewHolder) holder).itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                intent.putExtra(CustomizeThemeActivity.EXTRA_IS_PREDEFIINED_THEME, true);
                context.startActivity(intent);
            });
        } else if (holder instanceof UserCustomThemeViewHolder) {
            CustomTheme customTheme = userCustomThemes.get(position - predefinedCustomThemes.size() - 2);
            ((UserCustomThemeViewHolder) holder).colorPrimaryView.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((UserCustomThemeViewHolder) holder).nameTextView.setText(customTheme.name);
            ((UserCustomThemeViewHolder) holder).deleteImageView.setOnClickListener(view -> {

            });
            ((UserCustomThemeViewHolder) holder).shareImageView.setOnClickListener(view -> {

            });
            ((UserCustomThemeViewHolder) holder).itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                context.startActivity(intent);
            });
        } else if (holder instanceof PreDefinedThemeDividerViewHolder) {
            ((TextView) ((PreDefinedThemeDividerViewHolder) holder).itemView).setText(R.string.predefined_themes);
        } else if (holder instanceof UserThemeDividerViewHolder) {
            ((TextView) ((UserThemeDividerViewHolder) holder).itemView).setText(R.string.user_themes);
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

        @BindView(R.id.color_primary_item_predefined_custom_theme)
        View colorPrimaryView;
        @BindView(R.id.name_text_view_item_predefined_custom_theme)
        TextView nameTextView;

        public PredefinedCustomThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class UserCustomThemeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.color_primary_item_user_custom_theme)
        View colorPrimaryView;
        @BindView(R.id.name_text_view_item_user_custom_theme)
        TextView nameTextView;
        @BindView(R.id.delete_image_view_item_user_custom_theme)
        ImageView deleteImageView;
        @BindView(R.id.share_image_view_item_user_custom_theme)
        ImageView shareImageView;

        public UserCustomThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class PreDefinedThemeDividerViewHolder extends RecyclerView.ViewHolder {

        public PreDefinedThemeDividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class UserThemeDividerViewHolder extends RecyclerView.ViewHolder {

        public UserThemeDividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
