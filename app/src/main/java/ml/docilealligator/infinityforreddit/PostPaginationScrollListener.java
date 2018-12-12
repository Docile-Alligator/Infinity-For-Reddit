package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by alex on 3/12/18.
 */

class PostPaginationScrollListener extends RecyclerView.OnScrollListener {
    private Context mContext;
    private Retrofit mRetrofit;
    private PostViewModel mPostViewModel;
    private LinearLayoutManager mLayoutManager;
    private PaginationSynchronizer mPaginationSynchronizer;

    private String mSubredditName;
    private boolean isBestPost;
    private boolean isLoading;
    private boolean loadSuccess;
    private Locale locale;
    private String mLastItem;

    PostPaginationScrollListener(Context context, Retrofit retrofit, PostViewModel postViewModel,
                                 LinearLayoutManager layoutManager, String lastItem,
                                 PaginationSynchronizer paginationSynchronizer, final String subredditName,
                                 final boolean isBestPost, boolean isLoading, boolean loadSuccess, Locale locale) {
        if(context != null) {
            mContext = context;
            mRetrofit = retrofit;
            mPostViewModel = postViewModel;
            mLayoutManager = layoutManager;
            mLastItem = lastItem;
            mPaginationSynchronizer = paginationSynchronizer;
            mSubredditName = subredditName;
            this.isBestPost = isBestPost;
            this.isLoading = isLoading;
            this.loadSuccess = loadSuccess;
            this.locale = locale;

            PaginationRetryNotifier paginationRetryNotifier = new PaginationRetryNotifier() {
                @Override
                public void retry() {
                    if (isBestPost) {
                        fetchBestPost();
                    } else {
                        fetchPost(subredditName);
                    }
                }
            };
            mPaginationSynchronizer.setPaginationRetryNotifier(paginationRetryNotifier);
            mPaginationSynchronizer.addLastItemSynchronizer(new LastItemSynchronizer() {
                @Override
                public void lastItemChanged(String lastItem) {
                    mLastItem = lastItem;
                }
            });
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if(!isLoading && loadSuccess) {
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                if(isBestPost) {
                    fetchBestPost();
                } else {
                    fetchPost(mSubredditName);
                }
            }
        }
    }


    private void fetchBestPost() {
        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoadingState(true);

        RedditAPI api = mRetrofit.create(RedditAPI.class);

        String accessToken = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> bestPost = api.getBestPost(mLastItem, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(mPostViewModel != null) {
                                mPostViewModel.addPosts(newPosts);

                                mLastItem = lastItem;
                                mPaginationSynchronizer.notifyLastItemChanged(lastItem);

                                isLoading = false;
                                loadSuccess = true;
                                mPaginationSynchronizer.setLoadingState(false);
                                mPaginationSynchronizer.loadSuccess(true);
                            }
                        }

                        @Override
                        public void onParsePostFail() {
                            Toast.makeText(mContext, "Error parsing data", Toast.LENGTH_SHORT).show();
                            Log.i("Best post", "Error parsing data");
                            loadFailed();
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Error getting best post", Toast.LENGTH_SHORT).show();
                    Log.i("best post", response.message());
                    loadFailed();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadFailed();
            }
        });
    }

    private void fetchPost(final String subredditName) {
        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoadingState(true);

        RedditAPI api = mRetrofit.create(RedditAPI.class);
        Call<String> getPost = api.getPost(subredditName, mLastItem);
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(mPostViewModel != null) {
                                mPostViewModel.addPosts(newPosts);

                                mLastItem = lastItem;
                                mPaginationSynchronizer.notifyLastItemChanged(lastItem);

                                isLoading = false;
                                loadSuccess = true;
                                mPaginationSynchronizer.setLoadingState(false);
                                mPaginationSynchronizer.loadSuccess(true);
                            }
                        }

                        @Override
                        public void onParsePostFail() {
                            Toast.makeText(mContext, "Error parsing data", Toast.LENGTH_SHORT).show();
                            Log.i("Best post", "Error parsing data");
                            loadFailed();
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Error getting best post", Toast.LENGTH_SHORT).show();
                    Log.i("best post", response.message());
                    loadFailed();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(mContext, "Error getting best post", Toast.LENGTH_SHORT).show();
                loadFailed();
            }
        });
    }

    private void loadFailed() {
        isLoading = false;
        loadSuccess = false;
        mPaginationSynchronizer.setLoadingState(false);
        mPaginationSynchronizer.loadSuccess(false);
    }
}