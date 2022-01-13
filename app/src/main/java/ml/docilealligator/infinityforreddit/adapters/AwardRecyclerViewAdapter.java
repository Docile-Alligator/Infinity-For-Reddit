package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.award.Award;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import pl.droidsonroids.gif.GifImageView;

public class AwardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Award> awards;
    private BaseActivity activity;
    private RequestManager glide;
    private ItemOnClickListener itemOnClickListener;
    private int primaryTextColor;
    private int secondaryTextColor;

    public interface ItemOnClickListener {
        void onClick(Award award);
    }

    public AwardRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                    ItemOnClickListener itemOnClickListener) {
        awards = Award.getAvailableAwards();
        this.activity = activity;
        this.glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AwardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_award, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AwardViewHolder) {
            Award award = awards.get(position);
            glide.load(award.getIconUrl()).into(((AwardViewHolder) holder).iconImageView);
            ((AwardViewHolder) holder).nameTextView.setText(award.getName());
            ((AwardViewHolder) holder).descriptionTextView.setText(award.getDescription());
            ((AwardViewHolder) holder).coinTextView.setText(Integer.toString(award.getCoinPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return awards.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof AwardViewHolder) {
            glide.clear(((AwardViewHolder) holder).iconImageView);
            ((AwardViewHolder) holder).nameTextView.setText("");
            ((AwardViewHolder) holder).descriptionTextView.setText("");
        }
    }

    class AwardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon_image_view_item_award)
        GifImageView iconImageView;
        @BindView(R.id.name_text_view_item_award)
        TextView nameTextView;
        @BindView(R.id.description_text_view_item_award)
        TextView descriptionTextView;
        @BindView(R.id.coin_text_view_item_award)
        TextView coinTextView;

        public AwardViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (activity.typeface != null) {
                nameTextView.setTypeface(activity.typeface);
                descriptionTextView.setTypeface(activity.typeface);
                coinTextView.setTypeface(activity.typeface);
            }
            nameTextView.setTextColor(primaryTextColor);
            descriptionTextView.setTextColor(secondaryTextColor);
            coinTextView.setTextColor(primaryTextColor);

            itemView.setOnClickListener(view -> {
                itemOnClickListener.onClick(awards.get(getBindingAdapterPosition()));
            });
        }
    }
}
