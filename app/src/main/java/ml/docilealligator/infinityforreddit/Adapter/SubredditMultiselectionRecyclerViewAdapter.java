package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubredditWithSelection;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import pl.droidsonroids.gif.GifImageView;

public class SubredditMultiselectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SUBSCRIBED_SUBREDDIT_VIEW_TYPE = 0;
    private static final int OTHER_SUBREDDIT_VIEW_TYPE = 1;

    private ArrayList<SubredditWithSelection> subscribedSubreddits;
    private ArrayList<SubredditWithSelection> otherSubreddits;
    private ArrayList<SubredditWithSelection> selectedSubscribedSubreddits;
    private ArrayList<SubredditWithSelection> selectedOtherSubreddits;
    private Context context;
    private RequestManager glide;

    public SubredditMultiselectionRecyclerViewAdapter(Context context,
                                                      ArrayList<SubredditWithSelection> selectedSubscribedSubreddits,
                                                      ArrayList<SubredditWithSelection> otherSubreddits) {
        this.context = context;
        glide = Glide.with(context);
        subscribedSubreddits = new ArrayList<>();
        this.otherSubreddits = new ArrayList<>();
        this.selectedSubscribedSubreddits = new ArrayList<>();
        selectedOtherSubreddits = new ArrayList<>();

        if (selectedSubscribedSubreddits != null) {
            this.selectedSubscribedSubreddits.addAll(selectedSubscribedSubreddits);
        }

        if (otherSubreddits != null) {
            this.selectedOtherSubreddits.addAll(otherSubreddits);
            this.otherSubreddits.addAll(otherSubreddits);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (otherSubreddits.size() > 0) {
            if (position >= otherSubreddits.size()) {
                return SUBSCRIBED_SUBREDDIT_VIEW_TYPE;
            }

            return OTHER_SUBREDDIT_VIEW_TYPE;
        }

        return SUBSCRIBED_SUBREDDIT_VIEW_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SUBSCRIBED_SUBREDDIT_VIEW_TYPE) {
            return new SubscribedSubredditViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subscribed_subreddit_multi_selection, parent, false));
        } else {
            return new OtherSubredditViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subscribed_subreddit_multi_selection, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubscribedSubredditViewHolder) {
            int offset = otherSubreddits.size();
            ((SubscribedSubredditViewHolder) holder).nameTextView.setText(subscribedSubreddits.get(position - offset).getName());
            glide.load(subscribedSubreddits.get(position - offset).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubscribedSubredditViewHolder) holder).iconImageView);
            if (subscribedSubreddits.get(position - offset).isSelected()) {
                ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(true);
            } else {
                ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(false);
            }
            ((SubscribedSubredditViewHolder) holder).checkBox.setOnClickListener(view -> {
                if (subscribedSubreddits.get(position - offset).isSelected()) {
                    ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(false);
                    subscribedSubreddits.get(position - offset).setSelected(false);
                    selectedSubscribedSubreddits.remove(subscribedSubreddits.get(position - offset));
                } else {
                    ((SubscribedSubredditViewHolder) holder).checkBox.setChecked(true);
                    subscribedSubreddits.get(position - offset).setSelected(true);
                    insertIntoSelectedSubredditsArrayListAscend(selectedSubscribedSubreddits,
                            subscribedSubreddits.get(position - offset));
                }
            });
            ((SubscribedSubredditViewHolder) holder).itemView.setOnClickListener(view ->
                    ((SubscribedSubredditViewHolder) holder).checkBox.performClick());
        } else if (holder instanceof OtherSubredditViewHolder) {
            SubredditWithSelection subreddit = otherSubreddits.get(position);
            ((OtherSubredditViewHolder) holder).nameTextView.setText(subreddit.getName());
            glide.load(subreddit.getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((OtherSubredditViewHolder) holder).iconImageView);
            if (subreddit.isSelected()) {
                ((OtherSubredditViewHolder) holder).checkBox.setChecked(true);
            } else {
                ((OtherSubredditViewHolder) holder).checkBox.setChecked(false);
            }
            ((OtherSubredditViewHolder) holder).checkBox.setOnClickListener(view -> {
                if (subreddit.isSelected()) {
                    ((OtherSubredditViewHolder) holder).checkBox.setChecked(false);
                    subreddit.setSelected(false);
                    selectedOtherSubreddits.remove(subreddit);
                } else {
                    ((OtherSubredditViewHolder) holder).checkBox.setChecked(true);
                    subreddit.setSelected(true);
                    insertIntoSelectedSubredditsArrayListAscend(selectedOtherSubreddits, subreddit);
                }
            });
            ((OtherSubredditViewHolder) holder).itemView.setOnClickListener(view ->
                    ((OtherSubredditViewHolder) holder).checkBox.performClick());
        }
    }

    @Override
    public int getItemCount() {
        return subscribedSubreddits.size() + otherSubreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubscribedSubredditViewHolder) {
            glide.clear(((SubscribedSubredditViewHolder) holder).iconImageView);
        } else if (holder instanceof OtherSubredditViewHolder) {
            glide.clear(((OtherSubredditViewHolder) holder).iconImageView);
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        if (this.subscribedSubreddits.isEmpty()) {
            ArrayList<SubredditWithSelection> temp =
                    SubredditWithSelection.convertSubscribedSubreddits(subscribedSubreddits);
            this.subscribedSubreddits.addAll(temp);
            checkAllSelectedSubreddits(0, 0);
            notifyDataSetChanged();
        }
    }

    public void addOtherSubreddit(SubredditData subreddit) {
        SubredditWithSelection subredditWithSelection =
                SubredditWithSelection.convertSubreddit(subreddit);
        subredditWithSelection.setSelected(true);
        int i = subscribedSubreddits.indexOf(subredditWithSelection);
        if (i > 0) {
            subscribedSubreddits.get(i).setSelected(true);
            insertIntoSelectedSubredditsArrayListAscend(selectedSubscribedSubreddits, subredditWithSelection);
            Toast.makeText(context, context.getString(R.string.subreddit_selected, subreddit.getName()), Toast.LENGTH_SHORT).show();
            return;
        }
        insertIntoOtherSubredditsArrayListAscend(subredditWithSelection);
        insertIntoSelectedSubredditsArrayListAscend(selectedOtherSubreddits, subredditWithSelection);
    }

    public ArrayList<SubredditWithSelection> getAllSelectedSubscribedSubreddits() {
        return new ArrayList<>(selectedSubscribedSubreddits);
    }

    public ArrayList<SubredditWithSelection> getAllSelectedOtherSubreddits() {
        return new ArrayList<>(selectedOtherSubreddits);
    }

    public ArrayList<SubredditWithSelection> getAllOtherSubreddits() {
        return new ArrayList<>(otherSubreddits);
    }

    private void insertIntoSelectedSubredditsArrayListAscend(ArrayList<SubredditWithSelection> subreddits,
                                                             SubredditWithSelection subreddit) {
        for (int i = 0; i < subreddits.size(); i++) {
            if (subreddits.get(i).compareName(subreddit) < 0) {
                continue;
            }
            if (subreddits.get(i).compareName(subreddit) == 0) {
                return;
            }
            subreddits.add(i, subreddit);
            return;
        }
        subreddits.add(subreddit);
    }

    private void insertIntoOtherSubredditsArrayListAscend(SubredditWithSelection subreddit) {
        for (int i = 0; i < otherSubreddits.size(); i++) {
            if (otherSubreddits.get(i).compareName(subreddit) < 0) {
                continue;
            }
            if (otherSubreddits.get(i).compareName(subreddit) == 0) {
                return;
            }
            otherSubreddits.add(i, subreddit);
            notifyItemInserted(i);
            return;
        }
        otherSubreddits.add(subreddit);
        notifyItemInserted(otherSubreddits.size() - 1);
    }

    private void checkAllSelectedSubreddits(int i1, int i2) {
        if (selectedSubscribedSubreddits.size() <= i1 || subscribedSubreddits.size() <= i2) {
            return;
        }

        if (selectedSubscribedSubreddits.get(i1).compareName(subscribedSubreddits.get(i2)) == 0) {
            subscribedSubreddits.get(i2).setSelected(true);
            checkAllSelectedSubreddits(i1 + 1, i2 + 1);
        } else if (selectedSubscribedSubreddits.get(i1).compareName(subscribedSubreddits.get(i2)) < 0) {
            //Insert to other subreddits
            insertIntoOtherSubredditsArrayListAscend(selectedSubscribedSubreddits.get(i1));
            insertIntoSelectedSubredditsArrayListAscend(selectedOtherSubreddits, selectedSubscribedSubreddits.get(i1));
            checkAllSelectedSubreddits(i1 + 1, i2);
        } else {
            checkAllSelectedSubreddits(i1, i2 + 1);
        }
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
        }
    }

    class OtherSubredditViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.icon_gif_image_view_item_subscribed_subreddit_multiselection)
        GifImageView iconImageView;
        @BindView(R.id.name_text_view_item_subscribed_subreddit_multiselection)
        TextView nameTextView;
        @BindView(R.id.checkbox_item_subscribed_subreddit_multiselection)
        CheckBox checkBox;

        OtherSubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}