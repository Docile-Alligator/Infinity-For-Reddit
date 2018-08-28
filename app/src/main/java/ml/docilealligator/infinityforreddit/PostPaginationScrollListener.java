package ml.docilealligator.infinityforreddit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Created by alex on 3/12/18.
 */

class PostPaginationScrollListener extends RecyclerView.OnScrollListener {
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private PostRecyclerViewAdapter mAdapter;
    private ArrayList<PostData> mPostData;
    private PaginationSynchronizer mPaginationSynchronizer;
    private PaginationRetryNotifier mPaginationRetryNotifier;
    private LastItemSynchronizer mLastItemSynchronizer;
    private PaginationRequestQueueSynchronizer mPaginationRequestQueueSynchronizer;

    private String mQueryPostUrl;
    private boolean isBestPost;
    private boolean isLoading;
    private boolean loadSuccess;
    private Locale locale;
    private String mLastItem;
    private RequestQueue mRequestQueue;
    private RequestQueue mAcquireAccessTokenRequestQueue;

    PostPaginationScrollListener(Context context, LinearLayoutManager layoutManager, PostRecyclerViewAdapter adapter,
                                 String lastItem, ArrayList<PostData> postData, PaginationSynchronizer paginationSynchronizer,
                                 RequestQueue acquireAccessTokenRequestQueue, final String queryPostUrl,
                                 final boolean isBestPost, boolean isLoading, boolean loadSuccess, Locale locale) {
        if(context != null) {
            this.mContext = context;
            this.mLayoutManager = layoutManager;
            this.mAdapter = adapter;
            this.mLastItem = lastItem;
            this.mPostData = postData;
            this.mPaginationSynchronizer = paginationSynchronizer;
            this.mAcquireAccessTokenRequestQueue = acquireAccessTokenRequestQueue;
            this.mQueryPostUrl = queryPostUrl;
            this.isBestPost = isBestPost;
            this.isLoading = isLoading;
            this.loadSuccess = loadSuccess;
            this.locale = locale;

            mRequestQueue = Volley.newRequestQueue(mContext);
            mAcquireAccessTokenRequestQueue = Volley.newRequestQueue(mContext);
            mPaginationRetryNotifier = new PaginationRetryNotifier() {
                @Override
                public void retry() {
                    if(isBestPost) {
                        fetchBestPost(queryPostUrl, 1);
                    } else {
                        fetchPost(queryPostUrl, 1);
                    }
                }
            };
            mPaginationSynchronizer.setPaginationRetryNotifier(mPaginationRetryNotifier);
            mLastItemSynchronizer = mPaginationSynchronizer.getLastItemSynchronizer();
            mPaginationRequestQueueSynchronizer = mPaginationSynchronizer.getPaginationRequestQueueSynchronizer();
            mPaginationRequestQueueSynchronizer.passQueue(mRequestQueue);
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
                    fetchBestPost(mQueryPostUrl, 1);
                } else {
                    fetchPost(mQueryPostUrl, 1);
                }
            }
        }
    }


    private void fetchBestPost(final String queryPostUrl, final int refreshTime) {
        if(refreshTime < 0) {
            loadFailed();
            return;
        }

        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoading(true);

        Uri uri = Uri.parse(queryPostUrl)
                .buildUpon().appendQueryParameter(RedditUtils.AFTER_KEY, mLastItem).build();

        StringRequest bestPostRequest = new StringRequest(Request.Method.GET,  uri.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("response", response);
                clipboard.setPrimaryClip(clip);
                ParsePost.parsePost(response, mPostData, locale, new ParsePost.ParsePostListener() {
                    @Override
                    public void onParsePostSuccess(ArrayList<PostData> bestPostData, String lastItem) {
                        mAdapter.notifyDataSetChanged();
                        mLastItem = lastItem;
                        mLastItemSynchronizer.lastItemChanged(mLastItem);

                        isLoading = false;
                        loadSuccess = true;
                        mPaginationSynchronizer.setLoading(false);
                        mPaginationSynchronizer.setLoadingState(true);
                    }

                    @Override
                    public void onParsePostFail() {
                        Toast.makeText(mContext, "Error parsing data", Toast.LENGTH_SHORT).show();
                        Log.i("Best post", "Error parsing data");
                        loadFailed();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    //Access token expired
                    RefreshAccessToken.refreshAccessToken(mContext,
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    fetchBestPost(queryPostUrl, refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {
                                }
                            });
                } else {
                    Toast.makeText(mContext, "Error getting best post", Toast.LENGTH_SHORT).show();
                    Log.i("best post", error.toString());
                    loadFailed();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                String accessToken = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
                return RedditUtils.getOAuthHeader(accessToken);
            }
        };
        bestPostRequest.setTag(PostPaginationScrollListener.class);
        mRequestQueue.add(bestPostRequest);
    }

    private void fetchPost(final String queryPostUrl, final int refreshTime) {
        if(refreshTime < 0) {
            loadFailed();
            return;
        }

        isLoading = true;
        loadSuccess = false;
        mPaginationSynchronizer.setLoading(true);

        Uri uri = Uri.parse(queryPostUrl)
                .buildUpon().appendQueryParameter(RedditUtils.AFTER_KEY, mLastItem).build();

        StringRequest bestPostRequest = new StringRequest(Request.Method.GET,  uri.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("response", response);
                clipboard.setPrimaryClip(clip);
                ParsePost.parsePost(response, mPostData, locale, new ParsePost.ParsePostListener() {
                    @Override
                    public void onParsePostSuccess(ArrayList<PostData> bestPostData, String lastItem) {
                        mAdapter.notifyDataSetChanged();
                        mLastItem = lastItem;
                        mLastItemSynchronizer.lastItemChanged(mLastItem);

                        isLoading = false;
                        loadSuccess = true;
                        mPaginationSynchronizer.setLoading(false);
                        mPaginationSynchronizer.setLoadingState(true);
                    }

                    @Override
                    public void onParsePostFail() {
                        Toast.makeText(mContext, "Error parsing data", Toast.LENGTH_SHORT).show();
                        Log.i("Best post", "Error parsing data");
                        loadFailed();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    //Access token expired
                    RefreshAccessToken.refreshAccessToken(mContext,
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    fetchPost(queryPostUrl, refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {
                                }
                            });
                } else {
                    Toast.makeText(mContext, "Error getting best post", Toast.LENGTH_SHORT).show();
                    Log.i("best post", error.toString());
                    loadFailed();
                }
            }
        });
        bestPostRequest.setTag(PostPaginationScrollListener.class);
        mRequestQueue.add(bestPostRequest);
    }

    private void loadFailed() {
        isLoading = false;
        loadSuccess = false;
        mPaginationSynchronizer.setLoading(false);
        mPaginationSynchronizer.setLoadingState(false);
    }
}