package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemSubscribedSubredditMultiSelectionBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditWithSelection;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class SubredditMultiselectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final BaseActivity activity;
    private ArrayList<SubredditWithSelection> subscribedSubreddits;
    private final RequestManager glide;
    private final int primaryTextColor;
    private final int colorAccent;

    public SubredditMultiselectionRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper) {
        this.activity = activity;
        glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubscribedSubredditViewHolder(ItemSubscribedSubredditMultiSelectionBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubscribedSubredditViewHolder) {
            ((SubscribedSubredditViewHolder) holder).binding.nameTextViewItemSubscribedSubredditMultiselection.setText(subscribedSubreddits.get(position).getName());
            glide.load(subscribedSubreddits.get(position).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubscribedSubredditViewHolder) holder).binding.iconGifImageViewItemSubscribedSubredditMultiselection);
            ((SubscribedSubredditViewHolder) holder).binding.checkboxItemSubscribedSubredditMultiselection.setChecked(subscribedSubreddits.get(position).isSelected());
            ((SubscribedSubredditViewHolder) holder).binding.checkboxItemSubscribedSubredditMultiselection.setOnClickListener(view -> {
                if (subscribedSubreddits.get(position).isSelected()) {
                    ((SubscribedSubredditViewHolder) holder).binding.checkboxItemSubscribedSubredditMultiselection.setChecked(false);
                    subscribedSubreddits.get(position).setSelected(false);
                } else {
                    ((SubscribedSubredditViewHolder) holder).binding.checkboxItemSubscribedSubredditMultiselection.setChecked(true);
                    subscribedSubreddits.get(position).setSelected(true);
                }
            });
            ((SubscribedSubredditViewHolder) holder).itemView.setOnClickListener(view ->
                    ((SubscribedSubredditViewHolder) holder).binding.checkboxItemSubscribedSubredditMultiselection.performClick());
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
            glide.clear(((SubscribedSubredditViewHolder) holder).binding.iconGifImageViewItemSubscribedSubredditMultiselection);
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
        ItemSubscribedSubredditMultiSelectionBinding binding;

        SubscribedSubredditViewHolder(@NonNull ItemSubscribedSubredditMultiSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.nameTextViewItemSubscribedSubredditMultiselection.setTextColor(primaryTextColor);
            binding.checkboxItemSubscribedSubredditMultiselection.setButtonTintList(ColorStateList.valueOf(colorAccent));

            if (activity.typeface != null) {
                binding.nameTextViewItemSubscribedSubredditMultiselection.setTypeface(activity.typeface);
            }
        }
    }
}