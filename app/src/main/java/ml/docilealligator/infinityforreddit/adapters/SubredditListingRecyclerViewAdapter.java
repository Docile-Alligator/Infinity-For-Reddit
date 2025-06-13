package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.os.Handler;
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
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsSubscribedToSubreddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterErrorBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemFooterLoadingBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemSubredditListingBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditSubscription;
import retrofit2.Retrofit;

public class SubredditListingRecyclerViewAdapter extends PagedListAdapter<SubredditData, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<SubredditData> DIFF_CALLBACK = new DiffUtil.ItemCallback<SubredditData>() {
        @Override
        public boolean areItemsTheSame(@NonNull SubredditData oldItem, @NonNull SubredditData newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SubredditData oldItem, @NonNull SubredditData newItem) {
            return true;
        }
    };
    private final RequestManager glide;
    private final BaseActivity activity;
    private final Executor executor;
    private final Retrofit retrofit;
    private final Retrofit oauthRetrofit;
    private final String accessToken;
    private final String accountName;
    private final RedditDataRoomDatabase redditDataRoomDatabase;
    private final boolean isMultiSelection;
    private final int colorPrimaryLightTheme;
    private final int primaryTextColor;
    private final int secondaryTextColor;
    private final int colorAccent;
    private final int buttonTextColor;
    private final int unsubscribed;

    private NetworkState networkState;
    private final Callback callback;

    public SubredditListingRecyclerViewAdapter(BaseActivity activity, Executor executor, Retrofit oauthRetrofit, Retrofit retrofit,
                                               CustomThemeWrapper customThemeWrapper,
                                               @Nullable String accessToken, @NonNull String accountName,
                                               RedditDataRoomDatabase redditDataRoomDatabase,
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
        glide = Glide.with(this.activity);
        colorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        colorAccent = customThemeWrapper.getColorAccent();
        buttonTextColor = customThemeWrapper.getButtonTextColor();
        unsubscribed = customThemeWrapper.getUnsubscribed();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(ItemSubredditListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(ItemFooterErrorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new LoadingViewHolder(ItemFooterLoadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            SubredditData subredditData = getItem(position);
            if (subredditData != null) {
                if (isMultiSelection) {
                    ((DataViewHolder) holder).binding.checkboxItemSubredditListing.setOnCheckedChangeListener((compoundButton, b) -> subredditData.setSelected(b));
                }
                ((DataViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (isMultiSelection) {
                        ((DataViewHolder) holder).binding.checkboxItemSubredditListing.performClick();
                    } else {
                        callback.subredditSelected(subredditData.getName(), subredditData.getIconUrl());
                    }
                });

                if (!subredditData.getIconUrl().equals("")) {
                    glide.load(subredditData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((DataViewHolder) holder).binding.subredditIconGifImageViewItemSubredditListing);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((DataViewHolder) holder).binding.subredditIconGifImageViewItemSubredditListing);
                }

                ((DataViewHolder) holder).binding.subredditNameTextViewItemSubredditListing.setText(subredditData.getName());
                ((DataViewHolder) holder).binding.subscriberCountTextViewItemSubredditListing.setText(activity.getString(R.string.subscribers_number_detail, subredditData.getNSubscribers()));

                if (!isMultiSelection) {
                    CheckIsSubscribedToSubreddit.checkIsSubscribedToSubreddit(executor, new Handler(),
                            redditDataRoomDatabase, subredditData.getName(), accountName,
                            new CheckIsSubscribedToSubreddit.CheckIsSubscribedToSubredditListener() {
                                @Override
                                public void isSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setVisibility(View.GONE);
                                }

                                @Override
                                public void isNotSubscribed() {
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setVisibility(View.VISIBLE);
                                    ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setOnClickListener(view -> {
                                        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                                            SubredditSubscription.subscribeToSubreddit(executor, new Handler(),
                                                    oauthRetrofit, retrofit, accessToken, subredditData.getName(),
                                                    accountName, redditDataRoomDatabase,
                                                    new SubredditSubscription.SubredditSubscriptionListener() {
                                                        @Override
                                                        public void onSubredditSubscriptionSuccess() {
                                                            ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setVisibility(View.GONE);
                                                            Toast.makeText(activity, R.string.subscribed, Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onSubredditSubscriptionFail() {
                                                            Toast.makeText(activity, R.string.subscribe_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            SubredditSubscription.anonymousSubscribeToSubreddit(executor, new Handler(),
                                                    retrofit, redditDataRoomDatabase,
                                                    subredditData.getName(),
                                                    new SubredditSubscription.SubredditSubscriptionListener() {
                                                        @Override
                                                        public void onSubredditSubscriptionSuccess() {
                                                            ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setVisibility(View.GONE);
                                                            Toast.makeText(activity, R.string.subscribed, Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onSubredditSubscriptionFail() {
                                                            Toast.makeText(activity, R.string.subscribe_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });
                } else {
                    ((DataViewHolder) holder).binding.checkboxItemSubredditListing.setChecked(subredditData.isSelected());
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
            glide.clear(((DataViewHolder) holder).binding.subredditIconGifImageViewItemSubredditListing);
            ((DataViewHolder) holder).binding.subscribeImageViewItemSubredditListing.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void subredditSelected(String subredditName, String iconUrl);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        ItemSubredditListingBinding binding;

        DataViewHolder(@NonNull ItemSubredditListingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.subredditNameTextViewItemSubredditListing.setTextColor(primaryTextColor);
            binding.subscriberCountTextViewItemSubredditListing.setTextColor(secondaryTextColor);
            binding.subscribeImageViewItemSubredditListing.setColorFilter(unsubscribed, android.graphics.PorterDuff.Mode.SRC_IN);
            if (isMultiSelection) {
                binding.checkboxItemSubredditListing.setVisibility(View.VISIBLE);
            }

            if (activity.typeface != null) {
                binding.subredditNameTextViewItemSubredditListing.setTypeface(activity.typeface);
                binding.subscriberCountTextViewItemSubredditListing.setTypeface(activity.typeface);
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
            binding.errorTextViewItemFooterError.setTextColor(secondaryTextColor);
            binding.retryButtonItemFooterError.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLightTheme));
            binding.retryButtonItemFooterError.setTextColor(buttonTextColor);

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
