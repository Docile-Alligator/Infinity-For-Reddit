package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.os.Handler;
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
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.zhanghai.android.fastscroll.PopupTextProvider;
import ml.docilealligator.infinityforreddit.FavoriteThing;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class SubscribedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupTextProvider {
    private static final int VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_SUBREDDIT = 1;
    private static final int VIEW_TYPE_SUBREDDIT_DIVIDER = 2;
    private static final int VIEW_TYPE_SUBREDDIT = 3;

    private BaseActivity mActivity;
    private Executor mExecutor;
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

    private int primaryTextColor;
    private int secondaryTextColor;

    public SubscribedSubredditsRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit,
                                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                                   CustomThemeWrapper customThemeWrapper,
                                                   String accessToken) {
        mActivity = activity;
        mExecutor = executor;
        glide = Glide.with(activity);
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        this.accessToken = accessToken;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
    }

    public SubscribedSubredditsRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit,
                                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                                   CustomThemeWrapper customThemeWrapper,
                                                   String accessToken, boolean hasClearSelectionRow,
                                                   ItemClickListener itemClickListener) {
        this(activity, executor, oauthRetrofit, redditDataRoomDatabase, customThemeWrapper, accessToken);
        this.hasClearSelectionRow = hasClearSelectionRow;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) {
            if (itemClickListener != null && !hasClearSelectionRow) {
                if (position == 0) {
                    return VIEW_TYPE_SUBREDDIT;
                } else if (position == 1) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER;
                } else if (position == mFavoriteSubscribedSubredditData.size() + 2) {
                    return VIEW_TYPE_SUBREDDIT_DIVIDER;
                } else if (position <= mFavoriteSubscribedSubredditData.size() + 1) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT;
                } else {
                    return VIEW_TYPE_SUBREDDIT;
                }
            } else if (hasClearSelectionRow) {
                if (position == 0) {
                    return VIEW_TYPE_SUBREDDIT;
                } else if (position == 1) {
                    return VIEW_TYPE_SUBREDDIT;
                } else if (position == 2) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER;
                } else if (position == mFavoriteSubscribedSubredditData.size() + 3) {
                    return VIEW_TYPE_SUBREDDIT_DIVIDER;
                } else if (position <= mFavoriteSubscribedSubredditData.size() + 2) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT;
                } else {
                    return VIEW_TYPE_SUBREDDIT;
                }
            } else {
                if (position == 0) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT_DIVIDER;
                } else if (position == mFavoriteSubscribedSubredditData.size() + 1) {
                    return VIEW_TYPE_SUBREDDIT_DIVIDER;
                } else if (position <= mFavoriteSubscribedSubredditData.size()) {
                    return VIEW_TYPE_FAVORITE_SUBREDDIT;
                } else {
                    return VIEW_TYPE_SUBREDDIT;
                }
            }
        } else {
            return VIEW_TYPE_SUBREDDIT;
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof SubredditViewHolder) {
            String name;
            String iconUrl;

            if (hasClearSelectionRow && viewHolder.getBindingAdapterPosition() == 0) {
                ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(R.string.all_subreddits);
                ((SubredditViewHolder) viewHolder).favoriteImageView.setVisibility(View.GONE);
                viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(null, null, false));
                return;
            } else if (itemClickListener != null && !hasClearSelectionRow && viewHolder.getBindingAdapterPosition() == 0) {
                ((SubredditViewHolder) viewHolder).favoriteImageView.setVisibility(View.GONE);
                name = username;
                iconUrl = userIconUrl;
                viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, true));
            } else if (hasClearSelectionRow && viewHolder.getBindingAdapterPosition() == 1) {
                ((SubredditViewHolder) viewHolder).favoriteImageView.setVisibility(View.GONE);
                name = username;
                iconUrl = userIconUrl;
                if (itemClickListener != null) {
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, true));
                }
            } else {
                int offset;
                if (itemClickListener != null) {
                    if (hasClearSelectionRow) {
                        offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                                mFavoriteSubscribedSubredditData.size() + 4 : 2;
                    } else {
                        offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                                mFavoriteSubscribedSubredditData.size() + 3 : 1;
                    }
                } else {
                    offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                            mFavoriteSubscribedSubredditData.size() + 2 : 0;
                }

                name = mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).getName();
                iconUrl = mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).getIconUrl();
                if(mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).isFavorite()) {
                    ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                } else {
                    ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                }

                ((SubredditViewHolder) viewHolder).favoriteImageView.setOnClickListener(view -> {
                    if(mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).isFavorite()) {
                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                        mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).setFavorite(false);
                        FavoriteThing.unfavoriteSubreddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                                mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = viewHolder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(false);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = viewHolder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(true);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                });
                    } else {
                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                        mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).setFavorite(true);
                        FavoriteThing.favoriteSubreddit(mExecutor, new Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, accessToken,
                                mSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = viewHolder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(true);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = viewHolder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedSubredditData.size() > position) {
                                            mSubscribedSubredditData.get(position).setFavorite(false);
                                        }
                                        ((SubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                });
                    }
                });

                if (itemClickListener != null) {
                    viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, false));
                }
            }

            if (itemClickListener == null) {
                viewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                    mActivity.startActivity(intent);
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
            int offset;
            if (itemClickListener != null) {
                if (hasClearSelectionRow) {
                    offset = 3;
                } else {
                    offset = 2;
                }
            } else {
                offset = 1;
            }
            String name = mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).getName();
            String iconUrl = mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).getIconUrl();
            if(mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).isFavorite()) {
                ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setOnClickListener(view -> {
                if(mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).isFavorite()) {
                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).setFavorite(false);
                    FavoriteThing.unfavoriteSubreddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                            mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset),
                            new FavoriteThing.FavoriteThingListener() {
                                @Override
                                public void success() {
                                    int position = viewHolder.getBindingAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(false);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = viewHolder.getBindingAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(true);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }
                            });
                } else {
                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset).setFavorite(true);
                    FavoriteThing.favoriteSubreddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                            mFavoriteSubscribedSubredditData.get(viewHolder.getBindingAdapterPosition() - offset),
                            new FavoriteThing.FavoriteThingListener() {
                                @Override
                                public void success() {
                                    int position = viewHolder.getBindingAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(true);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                }

                                @Override
                                public void failed() {
                                    Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                    int position = viewHolder.getBindingAdapterPosition() - 1;
                                    if(position >= 0 && mFavoriteSubscribedSubredditData.size() > position) {
                                        mFavoriteSubscribedSubredditData.get(position).setFavorite(false);
                                    }
                                    ((FavoriteSubredditViewHolder) viewHolder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                }
                            });
                }
            });

            if (itemClickListener != null) {
                viewHolder.itemView.setOnClickListener(view -> itemClickListener.onClick(name, iconUrl, false));
            } else {
                viewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, name);
                    mActivity.startActivity(intent);
                });
            }

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
            if(mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) {
                if (itemClickListener != null) {
                    if (hasClearSelectionRow) {
                        return mSubscribedSubredditData.size() > 0 ?
                                mFavoriteSubscribedSubredditData.size() + mSubscribedSubredditData.size() + 4 : 0;
                    } else {
                        return mSubscribedSubredditData.size() > 0 ?
                                mFavoriteSubscribedSubredditData.size() + mSubscribedSubredditData.size() + 3 : 0;
                    }
                }
                return mSubscribedSubredditData.size() > 0 ?
                        mFavoriteSubscribedSubredditData.size() + mSubscribedSubredditData.size() + 2 : 0;
            }

            if (itemClickListener != null) {
                if (hasClearSelectionRow) {
                    return mSubscribedSubredditData.size() > 0 ? mSubscribedSubredditData.size() + 2 : 0;
                } else {
                    return mSubscribedSubredditData.size() > 0 ? mSubscribedSubredditData.size() + 1 : 0;
                }
            }

            return mSubscribedSubredditData.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).iconGifImageView);
            ((SubredditViewHolder) holder).favoriteImageView.setVisibility(View.VISIBLE);
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

    @NonNull
    @Override
    public String getPopupText(int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_SUBREDDIT:
                if (hasClearSelectionRow && position == 0) {
                    return "";
                } else if (itemClickListener != null && !hasClearSelectionRow && position == 0) {
                    return "";
                } else if (hasClearSelectionRow && position == 1) {
                    return "";
                } else {
                    int offset;
                    if (itemClickListener != null) {
                        if (hasClearSelectionRow) {
                            offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                                    mFavoriteSubscribedSubredditData.size() + 4 : 0;
                        } else {
                            offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                                    mFavoriteSubscribedSubredditData.size() + 3 : 0;
                        }
                    } else {
                        offset = (mFavoriteSubscribedSubredditData != null && mFavoriteSubscribedSubredditData.size() > 0) ?
                                mFavoriteSubscribedSubredditData.size() + 2 : 0;
                    }

                    return mSubscribedSubredditData.get(position - offset).getName().substring(0, 1).toUpperCase();
                }
            case VIEW_TYPE_FAVORITE_SUBREDDIT:
                int offset;
                if (itemClickListener != null) {
                    if (hasClearSelectionRow) {
                        offset = 3;
                    } else {
                        offset = 2;
                    }
                } else {
                    offset = 1;
                }
                return mFavoriteSubscribedSubredditData.get(position - offset).getName().substring(0, 1).toUpperCase();
            default:
                return "";
        }
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
            if (mActivity.typeface != null) {
                subredditNameTextView.setTypeface(mActivity.typeface);
            }
            subredditNameTextView.setTextColor(primaryTextColor);
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
            if (mActivity.typeface != null) {
                subredditNameTextView.setTypeface(mActivity.typeface);
            }
            subredditNameTextView.setTextColor(primaryTextColor);
        }
    }

    class FavoriteSubredditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        FavoriteSubredditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                dividerTextView.setTypeface(mActivity.typeface);
            }
            dividerTextView.setText(R.string.favorites);
            dividerTextView.setTextColor(secondaryTextColor);
        }
    }

    class AllSubredditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        AllSubredditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                dividerTextView.setTypeface(mActivity.typeface);
            }
            dividerTextView.setText(R.string.all);
            dividerTextView.setTextColor(secondaryTextColor);
        }
    }
}
