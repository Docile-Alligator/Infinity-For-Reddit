package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import pl.droidsonroids.gif.GifImageView;

public class SubredditAutocompleteRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private List<SubredditData> subreddits;
    private RequestManager glide;
    private CustomThemeWrapper customThemeWrapper;
    private ItemOnClickListener itemOnClickListener;

    public SubredditAutocompleteRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                                    ItemOnClickListener itemOnClickListener) {
        this.activity = activity;
        glide = Glide.with(activity);
        this.customThemeWrapper = customThemeWrapper;
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            glide.load(subreddits.get(position).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) holder).iconImageView);
            ((SubredditViewHolder) holder).subredditNameTextView.setText(subreddits.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return subreddits == null ? 0 : subreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).iconImageView);
        }
    }

    public void setSubreddits(List<SubredditData> subreddits) {
        this.subreddits = subreddits;
        notifyDataSetChanged();
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {

        GifImageView iconImageView;
        TextView subredditNameTextView;
        ImageView subscribeImageView;
        MaterialCheckBox checkBox;

        public SubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.subreddit_icon_gif_image_view_item_subreddit_listing);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subreddit_listing);
            subscribeImageView = itemView.findViewById(R.id.subscribe_image_view_item_subreddit_listing);
            checkBox = itemView.findViewById(R.id.checkbox_item_subreddit_listing);

            subscribeImageView.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);

            subredditNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                subredditNameTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                itemOnClickListener.onClick(subreddits.get(getBindingAdapterPosition()));
            });
        }
    }

    public interface ItemOnClickListener {
        void onClick(SubredditData subredditData);
    }
}
