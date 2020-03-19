package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.R;

public class CustomThemeListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<CustomTheme> customThemes;

    public CustomThemeListingRecyclerViewAdapter(Context context) {
        this.context = context;
        customThemes = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomThemeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CustomThemeViewHolder) {
            CustomTheme customTheme = customThemes.get(position);
            ((CustomThemeViewHolder) holder).colorPrimaryView.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimary));
            ((CustomThemeViewHolder) holder).nameTextView.setText(customTheme.name);
            ((CustomThemeViewHolder) holder).itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_NAME, customTheme.name);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class CustomThemeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.color_primary_item_custom_theme)
        View colorPrimaryView;
        @BindView(R.id.name_text_view_item_custom_theme)
        TextView nameTextView;

        public CustomThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
