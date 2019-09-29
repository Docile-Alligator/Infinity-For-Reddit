package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;

class SubscribedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<SubscribedSubredditData> mSubscribedSubredditData;
    private RequestManager glide;
    private ItemClickListener itemClickListener;

    private String username;
    private String userIconUrl;
    private boolean hasClearSelectionRow;

    interface ItemClickListener {
        void onClick(String name, String iconUrl, boolean subredditIsUser);
    }

    SubscribedSubredditsRecyclerViewAdapter(Context context) {
        mContext = context;
        glide = Glide.with(context.getApplicationContext());
    }

    SubscribedSubredditsRecyclerViewAdapter(Context context, boolean hasClearSelectionRow, ItemClickListener itemClickListener) {
        mContext = context;
        this.hasClearSelectionRow = hasClearSelectionRow;
        glide = Glide.with(context.getApplicationContext());
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SubredditViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_thing, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        String name;
        String iconUrl;

        if(itemClickListener != null) {
            if(hasClearSelectionRow) {
                if(viewHolder.getAdapterPosition() == 0) {
                    ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(R.string.all_subreddits);
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(null, null, false));
                    return;
                } else if(viewHolder.getAdapterPosition() == 1) {
                    name = username;
                    iconUrl = userIconUrl;
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, true));
                } else {
                    name = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 2).getName();
                    iconUrl = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 2).getIconUrl();
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, false));
                }
            } else {
                if(viewHolder.getAdapterPosition() == 0) {
                    name = username;
                    iconUrl = userIconUrl;
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, true));
                } else {
                    name = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).getName();
                    iconUrl = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).getIconUrl();
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, false));
                }
            }
        } else {
            name = mSubscribedSubredditData.get(viewHolder.getAdapterPosition()).getName();
            iconUrl = mSubscribedSubredditData.get(viewHolder.getAdapterPosition()).getIconUrl();

            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                mContext.startActivity(intent);
            });
        }

        if(iconUrl != null && !iconUrl.equals("")) {
            glide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) viewHolder).iconGifImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(((SubredditViewHolder) viewHolder).iconGifImageView);
        }
        ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(name);
    }

    @Override
    public int getItemCount() {
        if(mSubscribedSubredditData != null) {
            if(itemClickListener != null) {
                if(hasClearSelectionRow) {
                    return mSubscribedSubredditData.size() + 2;
                } else {
                    return mSubscribedSubredditData.size() + 1;
                }
            }

            return mSubscribedSubredditData.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        glide.clear(((SubredditViewHolder) holder).iconGifImageView);
    }

    void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits){
        mSubscribedSubredditData = subscribedSubreddits;
        notifyDataSetChanged();
    }

    void addUser(String username, String userIconUrl) {
        this.username = username;
        this.userIconUrl = userIconUrl;
    }

    private class SubredditViewHolder extends RecyclerView.ViewHolder {
        private final GifImageView iconGifImageView;
        private final TextView subredditNameTextView;

        SubredditViewHolder(View itemView) {
            super(itemView);
            iconGifImageView = itemView.findViewById(R.id.subreddit_icon_gif_image_view_item_subscribed_subreddit);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
        }
    }
}
