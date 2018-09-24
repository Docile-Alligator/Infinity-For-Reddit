package ml.docilealligator.infinityforreddit;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by alex on 3/12/18.
 */

class PostPaginationScrollListener extends RecyclerView.OnScrollListener {
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private PostRecyclerViewAdapter mAdapter;
    private ArrayList<PostData> mPostData;
    private PaginationSynchronizer mPaginationSynchronizer;
    private LastItemSynchronizer mLastItemSynchronizer;

    private String mSubredditName;
    private boolean isBestPost;
    private boolean isLoading;
    private boolean loadSuccess;
    private Locale locale;
    private String mLastItem;

    PostPaginationScrollListener(Context context, LinearLayoutManager layoutManager, PostRecyclerViewAdapter adapter,
                                 String lastItem, ArrayList<PostData> postData, PaginationSynchronizer paginationSynchronizer,
                                 final String subredditName, final boolean isBestPost, boolean isLoading,
                                 boolean loadSuccess, Locale locale) {
        if(context != null) {
            this.mContext = context;
            this.mLayoutManager = layoutManager;
            this.mAdapter = adapter;
            this.mLastItem = lastItem;
            this.mPostData = postData;
            this.mPaginationSynchronizer = paginationSynchronizer;
            this.mSubredditName = subredditName;
            this.isBestPost = isBestPost;
            this.isLoading = isLoading;
            this.loadSuccess = loadSuccess;
            this.locale = locale;

            PaginationRetryNotifier paginationRetryNotifier = new PaginationRetryNotifier() {
                @Override
                public void retry() {
                    if (isBestPost) {
                        fetchBestPost(1);
                    } else {
                        fetchPost(subredditName, 1);
                    }
                }
            };
            mPaginationSynchronizer.setPaginationRetryNotifier(paginationRetryNotifier);
            mLastItemSynchronizer = mPaginationSynchronizer.getLastItemSynchronizer();
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
                    fetchBestPost(1);
                } else {
                    fetchPost(mSubredditName, 1);
                }
            }
        }
    }


    private void fetchBestPost(final int refreshTime) {
        if(refreshTime < 0) {
            loadFailed();
            return;
        }

        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoading(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);

        String accessToken = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> bestPost = api.getBestPost(mLastItem, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", response.body());
                    clipboard.setPrimaryClip(clip);
                    ParsePost.parsePost(response.body(), mPostData, locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                            if(mAdapter != null) {
                                mAdapter.notifyItemRangeInserted(mPostData.size(), postData.size());
                                mLastItem = lastItem;
                                mLastItemSynchronizer.lastItemChanged(lastItem);

                                isLoading = false;
                                loadSuccess = true;
                                mPaginationSynchronizer.setLoading(false);
                                mPaginationSynchronizer.setLoadingState(true);
                            }
                        }

                        @Override
                        public void onParsePostFail() {
                            Toast.makeText(mContext, "Error parsing data", Toast.LENGTH_SHORT).show();
                            Log.i("Best post", "Error parsing data");
                            loadFailed();
                        }
                    });
                } else if(response.code() == 401) {
                    //Access token expired
                    RefreshAccessToken.refreshAccessToken(mContext,
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    fetchBestPost(refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {
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

    private void fetchPost(final String subredditName, final int refreshTime) {
        if(refreshTime < 0) {
            loadFailed();
            return;
        }

        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoading(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost = api.getPost(subredditName, mLastItem);
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", response.body());
                    clipboard.setPrimaryClip(clip);
                    ParsePost.parsePost(response.body(), mPostData, locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                            if(mAdapter != null) {
                                mAdapter.notifyItemRangeInserted(mPostData.size(), postData.size());
                                mLastItem = lastItem;
                                mLastItemSynchronizer.lastItemChanged(lastItem);

                                isLoading = false;
                                loadSuccess = true;
                                mPaginationSynchronizer.setLoading(false);
                                mPaginationSynchronizer.setLoadingState(true);
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
        mPaginationSynchronizer.setLoading(false);
        mPaginationSynchronizer.setLoadingState(false);
    }
}