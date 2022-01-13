package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subreddit.SubredditWithSelection;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import pl.droidsonroids.gif.GifImageView;

public class SubredditMultiselectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity activity;
    private ArrayList<SubredditWithSelection> subscribedSubreddits;
    private RequestManager glide;
    private int primaryTextColor;
    private int colorAccent;

    public SubredditMultiselectionRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubscribedSubredditViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscribed_subreddit_multi_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubscribedSubredditViewHolder) {
            ((SubscribedSubredditViewHolder) holder).nameTextView.setText(subscribedSubreddits.get(position).getName());
            glide.load(subscribedSubreddits.get(position).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubscribedSubredditViewHolder) holder).iconImageView);
            if (subscribedSubreddits.get(position).isSelected()) {
                ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(true);
            } else {
                ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(false);
            }
            ((SubscribedSubredditViewHolder) holder).checkBox.setOnClickListener(view -> {
                if (subscribedSubreddits.get(position).isSelected()) {
                    ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(false);
                    subscribedSubreddits.get(position).setSelected(false);
                } else {
                    ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(true);
                    subscribedSubreddits.get(position).setSelected(true);
                }
            });
            ((SubscribedSubredditViewHolder) holder).itemView.setOnClickListener(view ->
                    ((SubscribedSubredditViewHolder) holder).checkBox.performClick());
        }
    }

    @Override
    public int getItemCount() {
        return subscribedSubreddits == null ? 0 : subscribedSubreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubscribedSubredditViewHolder) {
            glide.clear(((SubscribedSubredditViewHolder) holder).iconImageView);
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        this.subscribedSubreddits = SubredditWithSelection.convertSubscribedSubreddits(subscribedSubreddits);
        notifyDataSetChanged();
    }

    public ArrayList<String> getAllSelectedSubreddits() {
        ArrayList<String> selectedSubreddits = new ArrayList<>();
        for (SubredditWithSelection s : subscribedSubreddits) {
            if (s.isSelected()) {
                selectedSubreddits.add(s.getName());
            }
        }
        return selectedSubreddits;
    }

    class SubscribedSubredditViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.icon_gif_image_view_item_subscribed_subreddit_multiselection)
        GifImageView iconImageView;
        @BindView(R.id.name_text_view_item_subscribed_subreddit_multiselection)
        TextView nameTextView;
        @BindView(R.id.checkbox_item_subscribed_subreddit_multiselection)
        CheckBox checkBox;

        SubscribedSubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
            nameTextView.setTextColor(primaryTextColor);
            checkBox.setButtonTintList(ColorStateList.valueOf(colorAccent));

            if (activity.typeface != null) {
                nameTextView.setTypeface(activity.typeface);
            }
        }
    }
}