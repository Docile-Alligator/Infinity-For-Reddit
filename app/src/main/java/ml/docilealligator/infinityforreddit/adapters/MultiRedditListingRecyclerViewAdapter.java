package ml.docilealligator.infinityforreddit.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.concurrent.Executor;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.zhanghai.android.fastscroll.PopupTextProvider;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFavoriteThingDividerBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemMultiRedditBinding;
import ml.docilealligator.infinityforreddit.multireddit.FavoriteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import retrofit2.Retrofit;

public class MultiRedditListingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements PopupTextProvider {

    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_MULTI_REDDIT = 1;
    private static final int VIEW_TYPE_MULTI_REDDIT_DIVIDER = 2;
    private static final int VIEW_TYPE_MULTI_REDDIT = 3;

    private final BaseActivity mActivity;
    private final Executor mExecutor;
    private final Retrofit mOauthRetrofit;
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final RequestManager mGlide;

    private final String mAccessToken;
    private final String mAccountName;
    private List<MultiReddit> mMultiReddits;
    private List<MultiReddit> mFavoriteMultiReddits;
    private final int mPrimaryTextColor;
    private final int mSecondaryTextColor;
    private final OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClick(MultiReddit multiReddit);
        void onLongClick(MultiReddit multiReddit);
    }

    public MultiRedditListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit,
                                                 RedditDataRoomDatabase redditDataRoomDatabase,
                                                 CustomThemeWrapper customThemeWrapper,
                                                 @Nullable String accessToken, @NonNull String accountName,
                                                 OnItemClickListener onItemClickListener) {
        mActivity = activity;
        mExecutor = executor;
        mGlide = Glide.with(activity);
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccessToken = accessToken;
        mAccountName = accountName;
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
                return new FavoriteMultiRedditsDividerViewHolder(ItemFavoriteThingDividerBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_FAVORITE_MULTI_REDDIT:
                return new FavoriteMultiRedditViewHolder(ItemMultiRedditBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_MULTI_REDDIT_DIVIDER:
                return new AllMultiRedditsDividerViewHolder(ItemFavoriteThingDividerBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default:
                return new MultiRedditViewHolder(ItemMultiRedditBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
                ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setOnClickListener(view -> {
                if(multiReddit.isFavorite()) {
                    ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
                    multiReddit.setFavorite(false);
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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
                                        ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                }
                        );
                    }
                } else {
                    ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                    multiReddit.setFavorite(true);
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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
                                        ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mMultiReddits.size() > position) {
                                            mMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((MultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
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
                        .into(((MultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((MultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
            }
            ((MultiRedditViewHolder) holder).binding.multiRedditNameTextViewItemMultiReddit.setText(name);
        } else if (holder instanceof FavoriteMultiRedditViewHolder) {
            MultiReddit multiReddit = mFavoriteMultiReddits.get(holder.getBindingAdapterPosition() - 1);
            String name = multiReddit.getDisplayName();
            String iconUrl = multiReddit.getIconUrl();
            if(multiReddit.isFavorite()) {
                ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
            }

            ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setOnClickListener(view -> {
                if(multiReddit.isFavorite()) {
                    ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
                    multiReddit.setFavorite(false);
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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
                                        ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(true);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                }
                        );
                    }
                } else {
                    ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                    multiReddit.setFavorite(true);
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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
                                        ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = holder.getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteMultiReddits.size() > position) {
                                            mFavoriteMultiReddits.get(position).setFavorite(false);
                                        }
                                        ((FavoriteMultiRedditViewHolder) holder).binding.favoriteImageViewItemMultiReddit.setImageResource(R.drawable.ic_favorite_border_24dp);
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
                        .into(((FavoriteMultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteMultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
            }
            ((FavoriteMultiRedditViewHolder) holder).binding.multiRedditNameTextViewItemMultiReddit.setText(name);
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
            mGlide.clear(((MultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
        } else if (holder instanceof FavoriteMultiRedditViewHolder) {
            mGlide.clear(((FavoriteMultiRedditViewHolder) holder).binding.multiRedditIconGifImageViewItemMultiReddit);
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
    public String getPopupText(@NonNull View view, int position) {
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
        ItemMultiRedditBinding binding;

        MultiRedditViewHolder(@NonNull ItemMultiRedditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.multiRedditNameTextViewItemMultiReddit.setTypeface(mActivity.typeface);
            }
            binding.multiRedditNameTextViewItemMultiReddit.setTextColor(mPrimaryTextColor);
        }
    }

    class FavoriteMultiRedditViewHolder extends RecyclerView.ViewHolder {
        ItemMultiRedditBinding binding;

        FavoriteMultiRedditViewHolder(@NonNull ItemMultiRedditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.multiRedditNameTextViewItemMultiReddit.setTypeface(mActivity.typeface);
            }
            binding.multiRedditNameTextViewItemMultiReddit.setTextColor(mPrimaryTextColor);
        }
    }

    class FavoriteMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteThingDividerBinding binding;

        FavoriteMultiRedditsDividerViewHolder(@NonNull ItemFavoriteThingDividerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.dividerTextViewItemFavoriteThingDivider.setTypeface(mActivity.typeface);
            }
            binding.dividerTextViewItemFavoriteThingDivider.setText(R.string.favorites);
            binding.dividerTextViewItemFavoriteThingDivider.setTextColor(mSecondaryTextColor);
        }
    }

    class AllMultiRedditsDividerViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteThingDividerBinding binding;

        AllMultiRedditsDividerViewHolder(@NonNull ItemFavoriteThingDividerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.dividerTextViewItemFavoriteThingDivider.setTypeface(mActivity.typeface);
            }
            binding.dividerTextViewItemFavoriteThingDivider.setText(R.string.all);
            binding.dividerTextViewItemFavoriteThingDivider.setTextColor(mSecondaryTextColor);
        }
    }
}
