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

import SubscribedSubredditDatabase.SubscribedSubredditData;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;

class SubscribedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<SubscribedSubredditData> mSubscribedSubredditData;
    private RequestManager glide;

    SubscribedSubredditsRecyclerViewAdapter(Context context) {
        mContext = context;
        glide = Glide.with(context.getApplicationContext());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SubredditViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_thing, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        viewHolder.itemView.setOnClickListener(view -> {
            if(viewHolder.getAdapterPosition() >= 0) {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, mSubscribedSubredditData.get(viewHolder.getAdapterPosition()).getName());
                mContext.startActivity(intent);
            }
        });
        if(!mSubscribedSubredditData.get(i).getIconUrl().equals("")) {
            glide.load(mSubscribedSubredditData.get(i).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) viewHolder).iconGifImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(((SubredditViewHolder) viewHolder).iconGifImageView);
        }
        ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(mSubscribedSubredditData.get(i).getName());
    }

    @Override
    public int getItemCount() {
        if(mSubscribedSubredditData != null) {
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
