package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.Executor;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsFollowingUser;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemUserListingBinding;
import ml.docilealligator.infinityforreddit.user.UserData;
import ml.docilealligator.infinityforreddit.user.UserFollowing;
import retrofit2.Retrofit;

public class UserListingRecyclerViewAdapter extends PagedListAdapter<UserData, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<UserData> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserData>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserData oldItem, @NonNull UserData newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserData oldItem, @NonNull UserData newItem) {
            return true;
        }
    };
    private final RequestManager glide;
    private final BaseActivity activity;
    private final Executor executor;
    private final Retrofit oauthRetrofit;
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final RedditDataRoomDatabase redditDataRoomDatabase;
    private final boolean isMultiSelection;

    private final int primaryTextColor;
    private final int buttonTextColor;
    private final int colorPrimaryLightTheme;
    private final int colorAccent;
    private final int unsubscribedColor;

    private NetworkState networkState;
    private final Callback callback;

    public UserListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit, Retrofit retrofit,
                                          CustomThemeWrapper customThemeWrapper, @Nullable String accessToken,
                                          @NonNull String accountName, RedditDataRoomDatabase redditDataRoomDatabase,
                                          boolean isMultiSelection, Callback callback) {
        super(DIFF_CALLBACK);
        this.activity = activity;
        this.executor = executor;
        this.oauthRetrofit = oauthRetrofit;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.isMultiSelection = isMultiSelection;
        this.callback = callback;
        glide = Glide.with(activity);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        buttonTextColor = customThemeWrapper.getButtonTextColor();
        colorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        colorAccent = customThemeWrapper.getColorAccent();
        unsubscribedColor = customThemeWrapper.getUnsubscribed();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(ItemUserListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new UserListingRecyclerViewAdapter.ErrorViewHolder(ItemFooterErrorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new UserListingRecyclerViewAdapter.LoadingViewHolder(ItemFooterLoadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            UserData userData = getItem(position);
            if (userData != null) {
                ((DataViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (isMultiSelection) {
                        ((DataViewHolder) holder).binding.checkboxItemUserListing.performClick();
                    } else {
                        callback.userSelected(userData.getName(), userData.getIconUrl());
                    }
                });

                if (!userData.getIconUrl().equals("")) {
                    glide.load(userData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((DataViewHolder) holder).binding.userIconGifImageViewItemUserListing);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((DataViewHolder) holder).binding.userIconGifImageViewItemUserListing);
                }

                ((DataViewHolder) holder).binding.userNameTextViewItemUserListing.setText(userData.getName());

                if (!isMultiSelection) {
                    CheckIsFollowingUser.checkIsFollowingUser(executor, activity.mHandler, redditDataRoomDatabase,
                            userData.getName(), accountName, new CheckIsFollowingUser.CheckIsFollowingUserListener() {
                                @Override
                                public void isSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemUserListing.setVisibility(View.GONE);
                                }

                                @Override
                                public void isNotSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemUserListing.setVisibility(View.VISIBLE);
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemUserListing.setOnClickListener(view -> {
                                        UserFollowing.followUser(executor, activity.mHandler, oauthRetrofit, retrofit,
                                                accessToken, userData.getName(), accountName, redditDataRoomDatabase,
                                                new UserFollowing.UserFollowingListener() {
                                                    @Override
                                                    public void onUserFollowingSuccess() {
                                                        ((DataViewHolder) holder).binding.subscribeImageViewItemUserListing.setVisibility(View.GONE);
                                                        Toast.makeText(activity, R.string.followed, Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onUserFollowingFail() {
                                                        Toast.makeText(activity, R.string.follow_failed, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                                }
                            });
                } else {
                    ((DataViewHolder) holder).binding.checkboxItemUserListing.setOnCheckedChangeListener((compoundButton, b) -> userData.setSelected(b));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof DataViewHolder) {
            glide.clear(((DataViewHolder) holder).binding.userIconGifImageViewItemUserListing);
            ((DataViewHolder) holder).binding.subscribeImageViewItemUserListing.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void userSelected(String username, String iconUrl);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        ItemUserListingBinding binding;

        DataViewHolder(@NonNull ItemUserListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.userNameTextViewItemUserListing.setTextColor(primaryTextColor);
            binding.subscribeImageViewItemUserListing.setColorFilter(unsubscribedColor, android.graphics.PorterDuff.Mode.SRC_IN);

            if (activity.typeface != null) {
                binding.userNameTextViewItemUserListing.setTypeface(activity.typeface);
            }

            if (isMultiSelection) {
                binding.checkboxItemUserListing.setVisibility(View.VISIBLE);
            }
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ItemFooterErrorBinding binding;

        ErrorViewHolder(@NonNull ItemFooterErrorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.retryButtonItemFooterError.setOnClickListener(view -> callback.retryLoadingMore());
            binding.errorTextViewItemFooterError.setText(R.string.load_comments_failed);
            binding.errorTextViewItemFooterError.setTextColor(primaryTextColor);
            binding.retryButtonItemFooterError.setTextColor(buttonTextColor);
            binding.retryButtonItemFooterError.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLightTheme));

            if (activity.typeface != null) {
                binding.retryButtonItemFooterError.setTypeface(activity.typeface);
                binding.errorTextViewItemFooterError.setTypeface(activity.typeface);
            }
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        ItemFooterLoadingBinding binding;

        LoadingViewHolder(@NonNull ItemFooterLoadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.progressBarItemFooterLoading.setIndicatorColor(colorAccent);
        }
    }
}
