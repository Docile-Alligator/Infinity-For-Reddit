package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.MultiReddit.FavoriteMultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class MultiRedditListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT = 1;
    private static final int VIEW_TYPE_MULTI_REDDIT_DIVIDER = 2;
    private static final int VIEW_TYPE_MULTI_REDDIT = 3;

    private Context mContext;
    private Retrofit mOauthRetrofit;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private RequestManager mGlide;

    private String mAccessToken;
    private List<MultiReddit> mMultiReddits;
    private List<MultiReddit> mFavoriteMultiReddits;

    public MultiRedditListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                                 RedditDataRoomDatabase redditDataRoomDatabase,
                                                 String accessToken) {
        mContext = context;
        mGlide = Glide.with(context.getApplicationContext());
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccessToken = accessToken;

    }

    @Override
    public int getItemViewType(int position) {
        if (mFavoriteMultiReddits != null && mFavoriteMultiReddits.size() > 0) {
            if (position == 0) {
                return VIEW_TYPE_FAVORITE_MULTI_REDDIT_DIVIDER;
            } else if (position == mFavoriteMultiReddits.size() + 1) {
                return VIEW_TYPE_MULTI_REDDIT_DIVIDER;
            } else if (position <= mFavoriteMultiReddits.size()) {
                return VIEW_TYPE_FAVORITE_MULTI_REDDIT;
            } else {
                return VIEW_TYPE_MULTI_REDDIT;
            }
        } else {
            return VIEW_TYPE_MULTI_REDDIT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FAVORITE_MULTI_REDDIT_DIVIDER:
                return new FavoriteMultiRedditsDividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_favorite_thing_divider, parent, false));
            case VIEW_TYPE_FAVORITE_MULTI_REDDIT:
                return new FavoriteMultiRedditViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_multi_reddit, parent, false));
            case VIEW_TYPE_MULTI_REDDIT_DIVIDER:
                return new AllMultiRedditsDividerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_favorite_thing_divider, parent, false));
            default:
                return new MultiRedditViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_multi_reddit, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MultiRedditViewHolder) {
            String name;
            String iconUrl;

            int offset = (mFavoriteMultiReddits != null && mFavoriteMultiReddits.size() > 0) ?
                    mFavoriteMultiReddits.size() + 2 : 0;

            name = mMultiReddits.get(holder.getAdapterPosition() - offset).getName();
            iconUrl = mMultiReddits.get(holder.getAdapterPosition() - offset).getIconUrl();
            if(mMultiReddits.get(holder.getAdapterPosition() - offset).isFavorite()) {
                ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((MultiRedditViewHolder) holder).favoriteImageView.setOnClickListener(view -> {
                if(mMultiReddits.get(holder.getAdapterPosition() - offset).isFavorite()) {
                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    mMultiReddits.get(holder.getAdapterPosition() - offset).setFavorite(false);
                    FavoriteMultiReddit.favoriteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                            false, mMultiReddits.get(holder.getAdapterPosition() - offset),
                            new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                @Override
                                public void success() {
                                    int position = holder.getAdapterPosition() - offset;
                                    if(position >= 0 && mMultiReddits.size() > position) {
                                        mMultiReddits.get(position).setFavorite(false);
                                    }
                                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = holder.getAdapterPosition() - offset;
                                    if(position >= 0 && mMultiReddits.size() > position) {
                                        mMultiReddits.get(position).setFavorite(true);
                                    }
                                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }
                            }
                    );
                } else {
                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    mMultiReddits.get(holder.getAdapterPosition() - offset).setFavorite(true);
                    FavoriteMultiReddit.favoriteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                            true, mMultiReddits.get(holder.getAdapterPosition() - offset),
                            new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                @Override
                                public void success() {
                                    int position = holder.getAdapterPosition() - offset;
                                    if(position >= 0 && mMultiReddits.size() > position) {
                                        mMultiReddits.get(position).setFavorite(true);
                                    }
                                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = holder.getAdapterPosition() - offset;
                                    if(position >= 0 && mMultiReddits.size() > position) {
                                        mMultiReddits.get(position).setFavorite(false);
                                    }
                                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }
                            }
                    );
                }
            });
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                mContext.startActivity(intent);
            });

            if (iconUrl != null && !iconUrl.equals("")) {
                mGlide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((MultiRedditViewHolder) holder).iconImageView);
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((MultiRedditViewHolder) holder).iconImageView);
            }
            ((MultiRedditViewHolder) holder).multiRedditNameTextView.setText(name);
        } else if (holder instanceof FavoriteMultiRedditViewHolder) {
            String name = mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).getName();
            String iconUrl = mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).getIconUrl();
            if(mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).isFavorite()) {
                ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setOnClickListener(view -> {
                if(mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).isFavorite()) {
                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).setFavorite(false);
                    FavoriteMultiReddit.favoriteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                            false, mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1),
                            new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                @Override
                                public void success() {
                                    int position = holder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                        mFavoriteMultiReddits.get(position).setFavorite(false);
                                    }
                                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = holder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                        mFavoriteMultiReddits.get(position).setFavorite(true);
                                    }
                                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }
                            }
                    );
                } else {
                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1).setFavorite(true);
                    FavoriteMultiReddit.favoriteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                            true, mFavoriteMultiReddits.get(holder.getAdapterPosition() - 1),
                            new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                @Override
                                public void success() {
                                    int position = holder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                        mFavoriteMultiReddits.get(position).setFavorite(true);
                                    }
                                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = holder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                        mFavoriteMultiReddits.get(position).setFavorite(false);
                                    }
                                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }
                            }
                    );
                }
            });
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                mContext.startActivity(intent);
            });

            if (iconUrl != null && !iconUrl.equals("")) {
                mGlide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((FavoriteMultiRedditViewHolder) holder).iconImageView);
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteMultiRedditViewHolder) holder).iconImageView);
            }
            ((FavoriteMultiRedditViewHolder) holder).multiRedditNameTextView.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        if (mMultiReddits != null) {
            if(mFavoriteMultiReddits != null && mFavoriteMultiReddits.size() > 0) {
                return mMultiReddits.size() > 0 ?
                        mFavoriteMultiReddits.size() + mMultiReddits.size() + 2 : 0;
            }

            return mMultiReddits.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof MultiRedditViewHolder) {
            mGlide.clear(((MultiRedditViewHolder) holder).iconImageView);
        } else if (holder instanceof FavoriteMultiRedditViewHolder) {
            mGlide.clear(((FavoriteMultiRedditViewHolder) holder).iconImageView);
        }
    }

    public void setMultiReddits(List<MultiReddit> multiReddits) {
        mMultiReddits = multiReddits;
        notifyDataSetChanged();
    }

    public void setFavoriteMultiReddits(List<MultiReddit> favoriteMultiReddits) {
        mFavoriteMultiReddits = favoriteMultiReddits;
        notifyDataSetChanged();
    }

    class MultiRedditViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.multi_reddit_icon_gif_image_view_item_multi_reddit)
        GifImageView iconImageView;
        @BindView(R.id.multi_reddit_name_text_view_item_multi_reddit)
        TextView multiRedditNameTextView;
        @BindView(R.id.favorite_image_view_item_multi_reddit)
        ImageView favoriteImageView;

        MultiRedditViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FavoriteMultiRedditViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.multi_reddit_icon_gif_image_view_item_multi_reddit)
        GifImageView iconImageView;
        @BindView(R.id.multi_reddit_name_text_view_item_multi_reddit)
        TextView multiRedditNameTextView;
        @BindView(R.id.favorite_image_view_item_multi_reddit)
        ImageView favoriteImageView;

        FavoriteMultiRedditViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FavoriteMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        FavoriteMultiRedditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            dividerTextView.setText(R.string.favorites);
        }
    }

    class AllMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        AllMultiRedditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            dividerTextView.setText(R.string.all);
        }
    }
}
