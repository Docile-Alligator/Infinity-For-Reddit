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
import ml.docilealligator.infinityforreddit.FavoriteThing;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class SubscribedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_SUBREDDIT = 1;
    private static final int VIEW_TYPE_SUBREDDIT_DIVIDER = 2;
    private static final int VIEW_TYPE_SUBREDDIT = 3;

    private Context mContext;
    private Retrofit mOauthRetrofit;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private List<SubscribedSubredditData> mSubscribedSubredditData;
    private List<SubscribedSubredditData> mFavoriteSubscribedSubredditData;
    private RequestManager glide;
    private ItemClickListener itemClickListener;

    private String accessToken;
    private String username;
    private String userIconUrl;
    private boolean hasClearSelectionRow;

    public SubscribedSubredditsRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                                   String accessToken) {
        mContext = context;
        glide = Glide.with(context.getApplicationContext());
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        this.accessToken = accessToken;

    }

    public SubscribedSubredditsRecyclerViewAdapter(Context context, boolean hasClearSelectionRow, ItemClickListener itemClickListener) {
        mContext = context;
        this.hasClearSelectionRow = hasClearSelectionRow;
        glide = Glide.with(context.getApplicationContext());
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemClickListener != null) {
            return VIEW_TYPE_SUBREDDIT;
        } else {
            if (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) {
                if (position == 0) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER;
                } else if (position == mFavoriteSubscribedSubredditData.size() + 1) {
                    return VIEW_TYPE_SUBREDDIT_DIVIDER;
                } else if (position <= mFavoriteSubscribedSubredditData.size()) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT;
                } else {
                    return VIEW_TYPE_SUBREDDIT;
                }
            } else {
                return VIEW_TYPE_SUBREDDIT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER:
                return new FavoriteSubredditsDividerViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_favorite_thing_divider, viewGroup, false));
            case VIEW_TYPE_FAVORITE_SUBREDDIT:
                return new FavoriteSubredditViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_subscribed_thing, viewGroup, false));
            case VIEW_TYPE_SUBREDDIT_DIVIDER:
                return new AllSubredditsDividerViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_favorite_thing_divider, viewGroup, false));
            default:
                return new SubredditViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_subscribed_thing, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof SubredditViewHolder) {
            String name;
            String iconUrl;

            if (itemClickListener != null) {
                if (hasClearSelectionRow) {
                    if (viewHolder.getAdapterPosition() == 0) {
                        ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(R.string.all_subreddits);
                        viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(null, null, false));
                        return;
                    } else if (viewHolder.getAdapterPosition() == 1) {
                        name = username;
                        iconUrl = userIconUrl;
                        viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, true));
                    } else {
                        name = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 2).getName();
                        iconUrl = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 2).getIconUrl();
                        viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, false));
                    }
                } else {
                    if (viewHolder.getAdapterPosition() == 0) {
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
                int offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                        mFavoriteSubscribedSubredditData.size() + 2 : 0;

                name = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).getName();
                iconUrl = mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).getIconUrl();
                if(mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).isFavorite()) {
                    ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                } else {
                    ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                }

                ((SubredditViewHolder) viewHolder).favoriteImageView.setOnClickListener(view -> {
                    if(mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).isFavorite()) {
                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                        mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).setFavorite(false);
                        FavoriteThing.unfavoriteSubreddit(mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                                mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = viewHolder.getAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(false);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mContext, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = viewHolder.getAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(true);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                });
                    } else {
                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                        mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset).setFavorite(true);
                        FavoriteThing.favoriteSubreddit(mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                                mSubscribedSubredditData.get(viewHolder.getAdapterPosition() - offset),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = viewHolder.getAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(true);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mContext, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = viewHolder.getAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(false);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                });
                    }
                });
                viewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                    mContext.startActivity(intent);
                });
            }

            if (iconUrl != null && !iconUrl.equals("")) {
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
        } else if (viewHolder instanceof FavoriteSubredditViewHolder) {
            String name = mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).getName();
            String iconUrl = mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).getIconUrl();
            if(mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).isFavorite()) {
                ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setOnClickListener(view -> {
                if(mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).isFavorite()) {
                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).setFavorite(false);
                    FavoriteThing.unfavoriteSubreddit(mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                            mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1),
                            new FavoriteThing.FavoriteThingListener() {
                                @Override
                                public void success() {
                                    int position = viewHolder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(false);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = viewHolder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(true);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }
                            });
                } else {
                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1).setFavorite(true);
                    FavoriteThing.favoriteSubreddit(mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                            mFavoriteSubscribedSubredditData.get(viewHolder.getAdapterPosition() - 1),
                            new FavoriteThing.FavoriteThingListener() {
                                @Override
                                public void success() {
                                    int position = viewHolder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(true);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mContext, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = viewHolder.getAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(false);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }
                            });
                }
            });
            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                mContext.startActivity(intent);
            });

            if (iconUrl != null && !iconUrl.equals("")) {
                glide.load(iconUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((FavoriteSubredditViewHolder) viewHolder).iconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteSubredditViewHolder) viewHolder).iconGifImageView);
            }
            ((FavoriteSubredditViewHolder) viewHolder).subredditNameTextView.setText(name);
        }
    }

    @Override
    public int getItemCount() {
        if (mSubscribedSubredditData != null) {
            if (itemClickListener != null) {
                if (hasClearSelectionRow) {
                    return mSubscribedSubredditData.size() + 2;
                } else {
                    return mSubscribedSubredditData.size() + 1;
                }
            }

            if(mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) {
                return mSubscribedSubredditData.size() > 0 ?
                        mFavoriteSubscribedSubredditData.size() + mSubscribedSubredditData.size() + 2 : 0;
            }

            return mSubscribedSubredditData.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).iconGifImageView);
        } else if (holder instanceof FavoriteSubredditViewHolder) {
            glide.clear(((FavoriteSubredditViewHolder) holder).iconGifImageView);
        }
    }

    public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
        mSubscribedSubredditData = subscribedSubreddits;
        notifyDataSetChanged();
    }

    public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubredditData) {
        mFavoriteSubscribedSubredditData = favoriteSubscribedSubredditData;
        notifyDataSetChanged();
    }

    public void addUser(String username, String userIconUrl) {
        this.username = username;
        this.userIconUrl = userIconUrl;
    }

    public interface ItemClickListener {
        void onClick(String name, String iconUrl, boolean subredditIsUser);
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thing_icon_gif_image_view_item_subscribed_thing)
        GifImageView iconGifImageView;
        @BindView(R.id.thing_name_text_view_item_subscribed_thing)
        TextView subredditNameTextView;
        @BindView(R.id.favorite_image_view_item_subscribed_thing)
        ImageView favoriteImageView;

        SubredditViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FavoriteSubredditViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thing_icon_gif_image_view_item_subscribed_thing)
        GifImageView iconGifImageView;
        @BindView(R.id.thing_name_text_view_item_subscribed_thing)
        TextView subredditNameTextView;
        @BindView(R.id.favorite_image_view_item_subscribed_thing)
        ImageView favoriteImageView;

        FavoriteSubredditViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FavoriteSubredditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        FavoriteSubredditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            dividerTextView.setText(R.string.favorites);
        }
    }

    class AllSubredditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        AllSubredditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            dividerTextView.setText(R.string.all);
        }
    }
}
