package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.List;

import SubscribedSubredditDatabase.SubscribedSubredditData;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

class SubscribedSubredditRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<SubscribedSubredditData> mSubscribedSubredditData;
    private RequestManager glide;
    private OnItemClickListener mOnItemClickListener;

    interface OnItemClickListener {
        void onClick();
    }

    SubscribedSubredditRecyclerViewAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        glide = Glide.with(context.getApplicationContext());
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SubredditViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_subreddit, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewHolder.getAdapterPosition() >= 0) {
                    mOnItemClickListener.onClick();
                    Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, mSubscribedSubredditData.get(viewHolder.getAdapterPosition()).getName());
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_VALUE_KEY, mSubscribedSubredditData.get(viewHolder.getAdapterPosition()).getId());
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_QUERY_BY_ID_KEY, true);
                    mContext.startActivity(intent);
                }
            }
        });
        if(!mSubscribedSubredditData.get(i).getIconUrl().equals("")) {
            glide.load(mSubscribedSubredditData.get(i).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if(resource instanceof Animatable) {
                                //This is a gif
                                //((Animatable) resource).start();
                                ((SubredditViewHolder) viewHolder).iconGifImageView.startAnimation();
                            }
                            return false;
                        }
                    }).into(((SubredditViewHolder) viewHolder).iconGifImageView);

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
        if(((SubredditViewHolder) holder).iconGifImageView.isAnimating()) {
            ((SubredditViewHolder) holder).iconGifImageView.stopAnimation();
        }
        glide.clear(((SubredditViewHolder) holder).iconGifImageView);
    }

    void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits){
        mSubscribedSubredditData = subscribedSubreddits;
        notifyDataSetChanged();
    }


    private class SubredditViewHolder extends RecyclerView.ViewHolder {
        private final GifImageView iconGifImageView;
        private final TextView subredditNameTextView;

        public SubredditViewHolder(View itemView) {
            super(itemView);
            iconGifImageView = itemView.findViewById(R.id.subreddit_icon_gif_image_view_item_subscribed_subreddit);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
        }
    }
}
