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
import ml.docilealligator.infinityforreddit.thing.FavoriteThing;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFavoriteThingDividerBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemSubscribedThingBinding;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import retrofit2.Retrofit;

public class FollowedUsersRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupTextProvider {
    private static final int VIEW_TYPE_FAVORITE_USER_DIVIDER = 0;
    private static final int VIEW_TYPE_FAVORITE_USER = 1;
    private static final int VIEW_TYPE_USER_DIVIDER = 2;
    private static final int VIEW_TYPE_USER = 3;

    private List<SubscribedUserData> mSubscribedUserData;
    private List<SubscribedUserData> mFavoriteSubscribedUserData;
    private final BaseActivity mActivity;
    private final Executor mExecutor;
    private final Retrofit mOauthRetrofit;
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final String mAccessToken;
    private final String mAccountName;
    private final RequestManager glide;
    private final int mPrimaryTextColor;
    private final int mSecondaryTextColor;
    private final ItemOnClickListener itemOnClickListener;

    public FollowedUsersRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit,
                                            RedditDataRoomDatabase redditDataRoomDatabase,
                                            CustomThemeWrapper customThemeWrapper,
                                            @Nullable String accessToken, @NonNull String accountName,
                                            ItemOnClickListener itemOnClickListener) {
        mActivity = activity;
        mExecutor = executor;
        mOauthRetrofit = oauthRetrofit;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccessToken = accessToken;
        mAccountName = accountName;
        glide = Glide.with(activity);
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        this.itemOnClickListener = itemOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFavoriteSubscribedUserData != null && mFavoriteSubscribedUserData.size() > 0) {
            if (position == 0) {
                return VIEW_TYPE_FAVORITE_USER_DIVIDER;
            } else if (position == mFavoriteSubscribedUserData.size() + 1) {
                return VIEW_TYPE_USER_DIVIDER;
            } else if (position <= mFavoriteSubscribedUserData.size()) {
                return VIEW_TYPE_FAVORITE_USER;
            } else {
                return VIEW_TYPE_USER;
            }
        } else {
            return VIEW_TYPE_USER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case VIEW_TYPE_FAVORITE_USER_DIVIDER:
                return new FavoriteUsersDividerViewHolder(ItemFavoriteThingDividerBinding
                        .inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            case VIEW_TYPE_FAVORITE_USER:
                return new FavoriteUserViewHolder(ItemSubscribedThingBinding
                        .inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            case VIEW_TYPE_USER_DIVIDER:
                return new AllUsersDividerViewHolder(ItemFavoriteThingDividerBinding
                        .inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            default:
                return new UserViewHolder(ItemSubscribedThingBinding
                        .inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof UserViewHolder) {
            int offset = (mFavoriteSubscribedUserData != null && mFavoriteSubscribedUserData.size() > 0) ?
                    mFavoriteSubscribedUserData.size() + 2 : 0;

            if (!mSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - offset).getIconUrl().equals("")) {
                glide.load(mSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - offset).getIconUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((UserViewHolder) viewHolder).binding.thingIconGifImageViewItemSubscribedThing);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((UserViewHolder) viewHolder).binding.thingIconGifImageViewItemSubscribedThing);
            }
            ((UserViewHolder) viewHolder).binding.thingNameTextViewItemSubscribedThing.setText(mSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - offset).getName());

            if(mSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - offset).isFavorite()) {
                ((UserViewHolder) viewHolder).binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((UserViewHolder) viewHolder).binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
            }
        } else if (viewHolder instanceof FavoriteUserViewHolder) {
            if (!mFavoriteSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - 1).getIconUrl().equals("")) {
                glide.load(mFavoriteSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - 1).getIconUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((FavoriteUserViewHolder) viewHolder).binding.thingIconGifImageViewItemSubscribedThing);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((FavoriteUserViewHolder) viewHolder).binding.thingIconGifImageViewItemSubscribedThing);
            }
            ((FavoriteUserViewHolder) viewHolder).binding.thingNameTextViewItemSubscribedThing.setText(mFavoriteSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - 1).getName());

            if(mFavoriteSubscribedUserData.get(viewHolder.getBindingAdapterPosition() - 1).isFavorite()) {
                ((FavoriteUserViewHolder) viewHolder).binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
            } else {
                ((FavoriteUserViewHolder) viewHolder).binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mSubscribedUserData != null && mSubscribedUserData.size() > 0) {
            if(mFavoriteSubscribedUserData != null && mFavoriteSubscribedUserData.size() > 0) {
                return mSubscribedUserData.size() + mFavoriteSubscribedUserData.size() + 2;
            }
            return mSubscribedUserData.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof UserViewHolder) {
            glide.clear(((UserViewHolder) holder).binding.thingIconGifImageViewItemSubscribedThing);
        } else if (holder instanceof FavoriteUserViewHolder) {
            glide.clear(((FavoriteUserViewHolder) holder).binding.thingIconGifImageViewItemSubscribedThing);
        }
    }

    public void setSubscribedUsers(List<SubscribedUserData> subscribedUsers) {
        mSubscribedUserData = subscribedUsers;
        notifyDataSetChanged();
    }

    public void setFavoriteSubscribedUsers(List<SubscribedUserData> favoriteSubscribedUsers) {
        mFavoriteSubscribedUserData = favoriteSubscribedUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CharSequence getPopupText(@NonNull View view, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_USER:
                int offset = (mFavoriteSubscribedUserData != null && !mFavoriteSubscribedUserData.isEmpty()) ?
                        mFavoriteSubscribedUserData.size() + 2 : 0;
                return mSubscribedUserData.get(position - offset).getName().substring(0, 1).toUpperCase();
            case VIEW_TYPE_FAVORITE_USER:
                return mFavoriteSubscribedUserData.get(position - 1).getName().substring(0, 1).toUpperCase();
            default:
                return "";
        }
    }

    class FavoriteUserViewHolder extends RecyclerView.ViewHolder {
        ItemSubscribedThingBinding binding;

        FavoriteUserViewHolder(ItemSubscribedThingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.thingNameTextViewItemSubscribedThing.setTypeface(mActivity.typeface);
            }
            binding.thingNameTextViewItemSubscribedThing.setTextColor(mPrimaryTextColor);

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition() - 1;
                if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                    itemOnClickListener.onClick(mFavoriteSubscribedUserData.get(position));
                }
            });

            binding.favoriteImageViewItemSubscribedThing.setOnClickListener(view -> {
                int position = getBindingAdapterPosition() - 1;
                if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                    if(mFavoriteSubscribedUserData.get(position).isFavorite()) {
                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                        mFavoriteSubscribedUserData.get(position).setFavorite(false);
                        FavoriteThing.unfavoriteUser(mExecutor, new Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, mAccessToken, mAccountName,
                                mFavoriteSubscribedUserData.get(position),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                                            mFavoriteSubscribedUserData.get(position).setFavorite(false);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                                            mFavoriteSubscribedUserData.get(position).setFavorite(true);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                });
                    } else {
                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                        mFavoriteSubscribedUserData.get(position).setFavorite(true);
                        FavoriteThing.favoriteUser(mExecutor, new Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, mAccessToken, mAccountName,
                                mFavoriteSubscribedUserData.get(position),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                                            mFavoriteSubscribedUserData.get(position).setFavorite(true);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = getBindingAdapterPosition() - 1;
                                        if(position >= 0 && mFavoriteSubscribedUserData.size() > position) {
                                            mFavoriteSubscribedUserData.get(position).setFavorite(false);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                });
                    }
                }
            });
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemSubscribedThingBinding binding;

        UserViewHolder(@NonNull ItemSubscribedThingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.thingNameTextViewItemSubscribedThing.setTypeface(mActivity.typeface);
            }
            binding.thingNameTextViewItemSubscribedThing.setTextColor(mPrimaryTextColor);

            itemView.setOnClickListener(view -> {
                int offset = (mFavoriteSubscribedUserData != null && mFavoriteSubscribedUserData.size() > 0) ?
                        mFavoriteSubscribedUserData.size() + 2 : 0;
                int position = getBindingAdapterPosition() - offset;
                if(position >= 0 && mSubscribedUserData.size() > position) {
                    itemOnClickListener.onClick(mSubscribedUserData.get(position));
                }
            });

            binding.favoriteImageViewItemSubscribedThing.setOnClickListener(view -> {
                int offset = (mFavoriteSubscribedUserData != null && mFavoriteSubscribedUserData.size() > 0) ?
                        mFavoriteSubscribedUserData.size() + 2 : 0;
                int position = getBindingAdapterPosition() - offset;

                if(position >= 0 && mSubscribedUserData.size() > position) {
                    if(mSubscribedUserData.get(position).isFavorite()) {
                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                        mSubscribedUserData.get(position).setFavorite(false);
                        FavoriteThing.unfavoriteUser(mExecutor, new Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, mAccessToken, mAccountName,
                                mSubscribedUserData.get(position),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedUserData.size() > position) {
                                            mSubscribedUserData.get(position).setFavorite(false);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_unfavorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedUserData.size() > position) {
                                            mSubscribedUserData.get(position).setFavorite(true);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                                    }
                                });
                    } else {
                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                        mSubscribedUserData.get(position).setFavorite(true);
                        FavoriteThing.favoriteUser(mExecutor, new Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, mAccessToken, mAccountName,
                                mSubscribedUserData.get(position),
                                new FavoriteThing.FavoriteThingListener() {
                                    @Override
                                    public void success() {
                                        int position = getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedUserData.size() > position) {
                                            mSubscribedUserData.get(position).setFavorite(true);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_24dp);
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(mActivity, R.string.thing_favorite_failed, Toast.LENGTH_SHORT).show();
                                        int position = getBindingAdapterPosition() - offset;
                                        if(position >= 0 && mSubscribedUserData.size() > position) {
                                            mSubscribedUserData.get(position).setFavorite(false);
                                        }
                                        binding.favoriteImageViewItemSubscribedThing.setImageResource(R.drawable.ic_favorite_border_24dp);
                                    }
                                });
                    }
                }
            });
        }
    }

    class FavoriteUsersDividerViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteThingDividerBinding binding;

        FavoriteUsersDividerViewHolder(@NonNull ItemFavoriteThingDividerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.dividerTextViewItemFavoriteThingDivider.setTypeface(mActivity.typeface);
            }
            binding.dividerTextViewItemFavoriteThingDivider.setText(R.string.favorites);
            binding.dividerTextViewItemFavoriteThingDivider.setTextColor(mSecondaryTextColor);
        }
    }

    class AllUsersDividerViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteThingDividerBinding binding;

        AllUsersDividerViewHolder(@NonNull ItemFavoriteThingDividerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (mActivity.typeface != null) {
                binding.dividerTextViewItemFavoriteThingDivider.setTypeface(mActivity.typeface);
            }
            binding.dividerTextViewItemFavoriteThingDivider.setText(R.string.all);
            binding.dividerTextViewItemFavoriteThingDivider.setTextColor(mSecondaryTextColor);
        }
    }

    public interface ItemOnClickListener {
        void onClick(SubscribedUserData subscribedUserData);
    }
}
