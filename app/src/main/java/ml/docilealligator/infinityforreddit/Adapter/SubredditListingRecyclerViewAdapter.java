package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.AsyncTask.CheckIsSubscribedToSubredditAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubredditSubscription;
import pl.droidsonroids.gif.GifImageView;
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
    private RequestManager glide;
    private Context context;
    private Retrofit oauthRetrofit;
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private int colorPrimaryLightTheme;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int colorAccent;
    private int buttonTextColor;
    private int unsubscribed;

    private NetworkState networkState;
    private Callback callback;

    public SubredditListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, Retrofit retrofit,
                                               CustomThemeWrapper customThemeWrapper,
                                               String accessToken, String accountName,
                                               RedditDataRoomDatabase redditDataRoomDatabase,
                                               Callback callback) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.oauthRetrofit = oauthRetrofit;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.callback = callback;
        glide = Glide.with(context.getApplicationContext());
        int themeType = customThemeWrapper.getThemeType();
        colorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme(themeType);
        primaryTextColor = customThemeWrapper.getPrimaryTextColor(themeType);
        secondaryTextColor = customThemeWrapper.getSecondaryTextColor(themeType);
        colorAccent = customThemeWrapper.getColorAccent(themeType);
        buttonTextColor = customThemeWrapper.getButtonTextColor(themeType);
        unsubscribed = customThemeWrapper.getUnsubscribed(themeType);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false);
            return new DataViewHolder(constraintLayout);
        } else if (viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            SubredditData subredditData = getItem(position);
            ((DataViewHolder) holder).constraintLayout.setOnClickListener(view ->
                    callback.subredditSelected(subredditData.getName(), subredditData.getIconUrl()));

            if (!subredditData.getIconUrl().equals("")) {
                glide.load(subredditData.getIconUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((DataViewHolder) holder).iconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((DataViewHolder) holder).iconGifImageView);
            }

            ((DataViewHolder) holder).subredditNameTextView.setText(subredditData.getName());

            new CheckIsSubscribedToSubredditAsyncTask(redditDataRoomDatabase, subredditData.getName(), accountName,
                    new CheckIsSubscribedToSubredditAsyncTask.CheckIsSubscribedToSubredditListener() {
                        @Override
                        public void isSubscribed() {
                            ((DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
                        }

                        @Override
                        public void isNotSubscribed() {
                            ((DataViewHolder) holder).subscribeButton.setVisibility(View.VISIBLE);
                            ((DataViewHolder) holder).subscribeButton.setOnClickListener(view -> {
                                SubredditSubscription.subscribeToSubreddit(oauthRetrofit, retrofit,
                                        accessToken, subredditData.getName(), accountName, redditDataRoomDatabase,
                                        new SubredditSubscription.SubredditSubscriptionListener() {
                                            @Override
                                            public void onSubredditSubscriptionSuccess() {
                                                ((DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
                                                Toast.makeText(context, R.string.subscribed, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onSubredditSubscriptionFail() {
                                                Toast.makeText(context, R.string.subscribe_failed, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                        }
                    }).execute();
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
            glide.clear(((DataViewHolder) holder).iconGifImageView);
            ((DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void subredditSelected(String subredditName, String iconUrl);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.constraint_layout_item_subreddit_listing)
        ConstraintLayout constraintLayout;
        @BindView(R.id.subreddit_icon_gif_image_view_item_subreddit_listing)
        GifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_subreddit_listing)
        TextView subredditNameTextView;
        @BindView(R.id.subscribe_image_view_item_subreddit_listing)
        ImageView subscribeButton;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            subredditNameTextView.setTextColor(primaryTextColor);
            subscribeButton.setColorFilter(unsubscribed, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error)
        Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            retryButton.setOnClickListener(view -> callback.retryLoadingMore());
            errorTextView.setText(R.string.load_comments_failed);
            errorTextView.setTextColor(secondaryTextColor);
            retryButton.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLightTheme));
            retryButton.setTextColor(buttonTextColor);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_footer_loading)
        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(colorAccent));
        }
    }
}
