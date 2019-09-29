package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
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

import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.AsyncTask.CheckIsFollowingUserAsyncTask;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.User.UserData;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.UserFollowing;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class UserListingRecyclerViewAdapter extends PagedListAdapter<UserData, RecyclerView.ViewHolder> {
    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    private RequestManager glide;

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private Context context;
    private Retrofit oauthRetrofit;
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private SubscribedUserDao subscribedUserDao;

    private NetworkState networkState;
    private UserListingRecyclerViewAdapter.RetryLoadingMoreCallback retryLoadingMoreCallback;

    public UserListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, Retrofit retrofit,
                                        String accessToken, String accountName, SubscribedUserDao subscribedUserDao,
                                        UserListingRecyclerViewAdapter.RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.oauthRetrofit = oauthRetrofit;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.subscribedUserDao = subscribedUserDao;
        this.retryLoadingMoreCallback = retryLoadingMoreCallback;
        glide = Glide.with(context.getApplicationContext());
    }

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_DATA) {
            ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_listing, parent, false);
            return new UserListingRecyclerViewAdapter.DataViewHolder(constraintLayout);
        } else if(viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new UserListingRecyclerViewAdapter.ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new UserListingRecyclerViewAdapter.LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof UserListingRecyclerViewAdapter.DataViewHolder) {
            UserData userData = getItem(position);
            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).constraintLayout.setOnClickListener(view -> {
                Intent intent = new Intent(context, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, userData.getName());
                context.startActivity(intent);
            });

            if(!userData.getIconUrl().equals("")) {
                glide.load(userData.getIconUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((UserListingRecyclerViewAdapter.DataViewHolder) holder).iconGifImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((UserListingRecyclerViewAdapter.DataViewHolder) holder).iconGifImageView);
            }

            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).UserNameTextView.setText(userData.getName());

            new CheckIsFollowingUserAsyncTask(subscribedUserDao, userData.getName(), accountName,
                    new CheckIsFollowingUserAsyncTask.CheckIsFollowingUserListener() {
                        @Override
                        public void isSubscribed() {
                            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
                        }

                        @Override
                        public void isNotSubscribed() {
                            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).subscribeButton.setVisibility(View.VISIBLE);
                            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).subscribeButton.setOnClickListener(view -> {
                                UserFollowing.followUser(oauthRetrofit, retrofit,
                                        accessToken, userData.getName(), accountName, subscribedUserDao,
                                        new UserFollowing.UserFollowingListener() {
                                            @Override
                                            public void onUserFollowingSuccess() {
                                                ((UserListingRecyclerViewAdapter.DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
                                                Toast.makeText(context, R.string.followed, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onUserFollowingFail() {
                                                Toast.makeText(context, R.string.follow_failed, Toast.LENGTH_SHORT).show();
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

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.constraint_layout_item_user_listing) ConstraintLayout constraintLayout;
        @BindView(R.id.user_icon_gif_image_view_item_user_listing) GifImageView iconGifImageView;
        @BindView(R.id.user_name_text_view_item_user_listing) TextView UserNameTextView;
        @BindView(R.id.subscribe_image_view_item_user_listing) ImageView subscribeButton;

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
            errorTextView.setText(R.string.load_comments_failed);
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
        if(holder instanceof UserListingRecyclerViewAdapter.DataViewHolder) {
            glide.clear(((UserListingRecyclerViewAdapter.DataViewHolder) holder).iconGifImageView);
            ((UserListingRecyclerViewAdapter.DataViewHolder) holder).subscribeButton.setVisibility(View.GONE);
        }
    }
}
