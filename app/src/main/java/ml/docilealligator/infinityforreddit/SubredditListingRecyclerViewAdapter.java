package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class SubredditListingRecyclerViewAdapter extends PagedListAdapter<SubredditData, RecyclerView.ViewHolder> {
    interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    private RequestManager glide;

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private Context context;
    private Retrofit oauthRetrofit;
    private Retrofit retrofit;
    private SharedPreferences authInfoSharedPreferences;
    private SubscribedSubredditDao subscribedSubredditDao;

    private NetworkState networkState;
    private RetryLoadingMoreCallback retryLoadingMoreCallback;

    SubredditListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, Retrofit retrofit,
                                        SharedPreferences authInfoSharedPreferences,
                                        SubscribedSubredditDao subscribedSubredditDao,
                                        RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.oauthRetrofit = oauthRetrofit;
        this.retrofit = retrofit;
        this.authInfoSharedPreferences = authInfoSharedPreferences;
        this.subscribedSubredditDao = subscribedSubredditDao;
        this.retryLoadingMoreCallback = retryLoadingMoreCallback;
        glide = Glide.with(context.getApplicationContext());
    }

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_DATA) {
            ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false);
            return new DataViewHolder(constraintLayout);
        } else if(viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DataViewHolder) {
            SubredditData subredditData = getItem(position);
            ((DataViewHolder) holder).constraintLayout.setOnClickListener(view -> {
                Intent intent = new Intent(context, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                context.startActivity(intent);
            });

            if(!subredditData.getIconUrl().equals("")) {
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

            new CheckIsSubscribedToSubredditAsyncTask(subscribedSubredditDao, subredditData.getName(),
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
                                        authInfoSharedPreferences, subredditData.getName(), subscribedSubredditDao,
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
        if(hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    void setNetworkState(NetworkState newNetworkState) {
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

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.constraint_layout_item_subreddit_listing) ConstraintLayout constraintLayout;
        @BindView(R.id.subreddit_icon_gif_image_view_item_subreddit_listing) GifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_subreddit_listing) TextView subredditNameTextView;
        @BindView(R.id.subscribe_image_view_item_subreddit_listing) ImageView subscribeButton;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error) TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error) Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            retryButton.setOnClickListener(view -> retryLoadingMoreCallback.retryLoadingMore());
            errorTextView.setText(R.string.post_load_comments_failed);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof DataViewHolder) {
            glide.clear(((DataViewHolder) holder).iconGifImageView);
            ((DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
        }
    }
}
