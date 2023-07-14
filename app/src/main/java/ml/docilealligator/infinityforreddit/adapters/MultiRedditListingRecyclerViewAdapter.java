package ml.docilealligator.infinityforreddit.adapters;

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
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.multireddit.FavoriteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class MultiRedditListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements PopupTextProvider {

    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT = 1;
    private static final int VIEW_TYPE_MULTI_REDDIT_DIVIDER = 2;
    private static final int VIEW_TYPE_MULTI_REDDIT = 3;

    private BaseActivity mActivity;
    private Executor mExecutor;
    private Retrofit mOauthRetrofit;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private RequestManager mGlide;

    private String mAccessToken;
    private List<MultiReddit> mMultiReddits;
    private List<MultiReddit> mFavoriteMultiReddits;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClick(MultiReddit multiReddit);
        void onLongClick(MultiReddit multiReddit);
    }

    public MultiRedditListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit,
                                                 RedditDataRoomDatabase redditDataRoomDatabase,
                                                 CustomThemeWrapper customThemeWrapper,
                                                 String accessToken, OnItemClickListener onItemClickListener) {
        mActivity = activity;
        mExecutor = executor;
        mGlide = Glide.with(activity);
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccessToken = accessToken;
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mOnItemClickListener = onItemClickListener;
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

            MultiReddit multiReddit = mMultiReddits.get(holder.getBindingAdapterPosition() - offset);
            name = multiReddit.getDisplayName();
            iconUrl = multiReddit.getIconUrl();
            if(multiReddit.isFavorite()) {
                ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((MultiRedditViewHolder) holder).favoriteImageView.setOnClickListener(view -> {
                if(multiReddit.isFavorite()) {
                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    multiReddit.setFavorite(false);
                    if (mAccessToken == null) {
                        InsertMultireddit.insertMultireddit(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddit,
                                () -> {
                                    //Do nothing
                                });
                    } else {
                        FavoriteMultiReddit.favoriteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                mAccessToken, false, multiReddit,
                                new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                }
                        );
                    }
                } else {
                    ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    multiReddit.setFavorite(true);
                    if (mAccessToken == null) {
                        InsertMultireddit.insertMultireddit(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddit,
                                () -> {
                                    //Do nothing
                                });
                    } else {
                        FavoriteMultiReddit.favoriteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                mAccessToken, true, multiReddit,
                                new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((MultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                }
                        );
                    }
                }
            });
            holder.itemView.setOnClickListener(view -> {
                mOnItemClickListener.onClick(multiReddit);
            });

            holder.itemView.setOnLongClickListener(view -> {
                mOnItemClickListener.onLongClick(multiReddit);
                return true;
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
            MultiReddit multiReddit = mFavoriteMultiReddits.get(holder.getBindingAdapterPosition() - 1);
            String name = multiReddit.getDisplayName();
            String iconUrl = multiReddit.getIconUrl();
            if(multiReddit.isFavorite()) {
                ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setOnClickListener(view -> {
                if(multiReddit.isFavorite()) {
                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                    multiReddit.setFavorite(false);
                    if (mAccessToken == null) {
                        InsertMultireddit.insertMultireddit(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddit,
                                () -> {
                                    //Do nothing
                                });
                    } else {
                        FavoriteMultiReddit.favoriteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                mAccessToken, false, multiReddit,
                                new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                }
                        );
                    }
                } else {
                    ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                    multiReddit.setFavorite(true);
                    if (mAccessToken == null) {
                        InsertMultireddit.insertMultireddit(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddit,
                                () -> {
                                    //Do nothing
                                });
                    } else {
                        FavoriteMultiReddit.favoriteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                mAccessToken, true, multiReddit,
                                new FavoriteMultiReddit.FavoriteMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).favoriteImageView.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                }
                        );
                    }
                }
            });
            holder.itemView.setOnClickListener(view -> {
                mOnItemClickListener.onClick(multiReddit);
            });

            holder.itemView.setOnLongClickListener(view -> {
                mOnItemClickListener.onLongClick(multiReddit);
                return true;
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

    @NonNull
    @Override
    public String getPopupText(int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_MULTI_REDDIT:
                int offset = (mFavoriteMultiReddits != null && mFavoriteMultiReddits.size() > 0) ?
                        mFavoriteMultiReddits.size() + 2 : 0;
                return mMultiReddits.get(position - offset).getDisplayName().substring(0, 1).toUpperCase();
            case VIEW_TYPE_FAVORITE_MULTI_REDDIT:
                return mFavoriteMultiReddits.get(position - 1).getDisplayName().substring(0, 1).toUpperCase();
            default:
                return "";
        }
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
            if (mActivity.typeface != null) {
                multiRedditNameTextView.setTypeface(mActivity.typeface);
            }
            multiRedditNameTextView.setTextColor(mPrimaryTextColor);
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
            if (mActivity.typeface != null) {
                multiRedditNameTextView.setTypeface(mActivity.typeface);
            }
            multiRedditNameTextView.setTextColor(mPrimaryTextColor);
        }
    }

    class FavoriteMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        FavoriteMultiRedditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                dividerTextView.setTypeface(mActivity.typeface);
            }
            dividerTextView.setText(R.string.favorites);
            dividerTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class AllMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.divider_text_view_item_favorite_thing_divider) TextView dividerTextView;

        AllMultiRedditsDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                dividerTextView.setTypeface(mActivity.typeface);
            }
            dividerTextView.setText(R.string.all);
            dividerTextView.setTextColor(mSecondaryTextColor);
        }
    }
}
