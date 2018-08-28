package ml.docilealligator.infinityforreddit;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {

    static final String QUERY_POST_URL_KEY = "QPUK";
    static final String IS_BEST_POST_KEY = "IBPK";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private ArrayList<PostData> mPostData;
    private String mLastItem;
    private PaginationSynchronizer mPaginationSynchronizer;
    private PostRecyclerViewAdapter mAdapter;

    private boolean mIsBestPost;
    private String mQueryPostUrl;
    private String PostDataParcelableState = "BPDPS";
    private String lastItemState = "LIS";
    private String paginationSynchronizerState = "PSS";

    private RequestQueue mRequestQueue;
    private RequestQueue mPaginationRequestQueue;
    private RequestQueue mAcquireAccessTokenRequestQueue;
    private RequestQueue mVoteThingRequestQueue;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(PostDataParcelableState)) {
                mPostData = savedInstanceState.getParcelableArrayList(PostDataParcelableState);
                mLastItem = savedInstanceState.getString(lastItemState);
                mAdapter = new PostRecyclerViewAdapter(getActivity(), mPostData, mPaginationSynchronizer, mVoteThingRequestQueue);
                mPostRecyclerView.setAdapter(mAdapter);
                mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                        getActivity(), mLinearLayoutManager, mAdapter, mLastItem, mPostData,
                        mPaginationSynchronizer, mAcquireAccessTokenRequestQueue,
                        mQueryPostUrl, mIsBestPost,
                        mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                        getResources().getConfiguration().locale));
                mProgressBar.setVisibility(View.GONE);
            } else {
                if(mIsBestPost) {
                    queryBestPost(1);
                } else {
                    fetchPost(mQueryPostUrl, 1);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mRequestQueue != null) {
            mRequestQueue.cancelAll(this);
        }

        if(mAcquireAccessTokenRequestQueue != null) {
            mAcquireAccessTokenRequestQueue.cancelAll(RefreshAccessToken.class);
        }

        if(mVoteThingRequestQueue != null) {
            mVoteThingRequestQueue.cancelAll(VoteThing.class);
        }

        if(mPaginationRequestQueue != null) {
            mPaginationRequestQueue.cancelAll(PostPaginationScrollListener.class);
        }

        if(mPostData != null) {
            outState.putParcelableArrayList(PostDataParcelableState, mPostData);
            outState.putString(lastItemState, mLastItem);
            outState.putParcelable(paginationSynchronizerState, mPaginationSynchronizer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null) {
            mAdapter.setCanStartActivity(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        mCoordinatorLayout = rootView.findViewById(R.id.coordinator_layout_post_fragment);
        mPostRecyclerView = rootView.findViewById(R.id.recycler_view_post_fragment);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar = rootView.findViewById(R.id.progress_bar_post_fragment);
        /*FloatingActionButton fab = rootView.findViewById(R.id.fab_post_fragment);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        mRequestQueue = Volley.newRequestQueue(getActivity());
        mAcquireAccessTokenRequestQueue = Volley.newRequestQueue(getActivity());
        mVoteThingRequestQueue = Volley.newRequestQueue(getActivity());

        mIsBestPost = getArguments().getBoolean(IS_BEST_POST_KEY);
        mQueryPostUrl = getArguments().getString(QUERY_POST_URL_KEY);

        if(savedInstanceState != null && savedInstanceState.getParcelable(paginationSynchronizerState) != null) {
            mPaginationSynchronizer = savedInstanceState.getParcelable(paginationSynchronizerState);
        } else {
            mPaginationSynchronizer = new PaginationSynchronizer();
            if(mIsBestPost) {
                queryBestPost(1);
            } else {
                fetchPost(mQueryPostUrl, 1);
            }
        }

        LastItemSynchronizer lastItemSynchronizer = new LastItemSynchronizer() {
            @Override
            public void lastItemChanged(String lastItem) {
                mLastItem = lastItem;
            }
        };
        mPaginationSynchronizer.setLastItemSynchronizer(lastItemSynchronizer);

        PaginationRequestQueueSynchronizer paginationRequestQueueSynchronizer = new PaginationRequestQueueSynchronizer() {
            @Override
            public void passQueue(RequestQueue q) {
                mPaginationRequestQueue = q;
            }
        };
        mPaginationSynchronizer.setPaginationRequestQueueSynchronizer(paginationRequestQueueSynchronizer);

        return rootView;
    }

    private void queryBestPost(final int refreshTime) {
        if(refreshTime < 0) {
            showErrorSnackbar();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        StringRequest postRequest = new StringRequest(Request.Method.GET, mQueryPostUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(getActivity() != null) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", response);
                    clipboard.setPrimaryClip(clip);
                    //new ParsePostDataAsyncTask(response, accessToken).execute();
                    ParsePost.parsePost(response, new ArrayList<PostData>(),
                            getResources().getConfiguration().locale, new ParsePost.ParsePostListener() {
                                @Override
                                public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                                    mPostData = postData;
                                    mLastItem = lastItem;
                                    mAdapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer, mVoteThingRequestQueue);

                                    mPostRecyclerView.setAdapter(mAdapter);
                                    mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                            getActivity(), mLinearLayoutManager, mAdapter, lastItem, postData,
                                            mPaginationSynchronizer, mAcquireAccessTokenRequestQueue,
                                            mQueryPostUrl, mIsBestPost,
                                            mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                                            getResources().getConfiguration().locale));
                                    mProgressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onParsePostFail() {
                                    Toast.makeText(getActivity(), "Error parsing data", Toast.LENGTH_SHORT).show();
                                    Log.i("Post fetch error", "Error parsing data");
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    // Error indicating that there was an Authentication Failure while performing the request
                    // Access token expired
                    RefreshAccessToken.refreshAccessToken(getActivity(),
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    queryBestPost(refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {}
                            });
                } else {
                    Log.i("Post fetch error", error.toString());
                    showErrorSnackbar();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
                return RedditUtils.getOAuthHeader(accessToken);
            }
        };
        postRequest.setTag(PostFragment.class);
        mRequestQueue.add(postRequest);
    }

    private void fetchPost(final String queryPostUrl, final int refreshTime) {
        if(refreshTime < 0) {
            showErrorSnackbar();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        Uri uri = Uri.parse(RedditUtils.OAUTH_API_BASE_URI + RedditUtils.BEST_POST_SUFFIX)
                .buildUpon().appendQueryParameter(RedditUtils.RAW_JSON_KEY, RedditUtils.RAW_JSON_VALUE)
                .build();

        StringRequest postRequest = new StringRequest(Request.Method.GET, queryPostUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(getActivity() != null) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", response);
                    clipboard.setPrimaryClip(clip);
                    //new ParsePostDataAsyncTask(response, accessToken).execute();
                    ParsePost.parsePost(response, new ArrayList<PostData>(),
                            getResources().getConfiguration().locale, new ParsePost.ParsePostListener() {
                                @Override
                                public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                                    mPostData = postData;
                                    mLastItem = lastItem;
                                    mAdapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer, mVoteThingRequestQueue);

                                    mPostRecyclerView.setAdapter(mAdapter);
                                    mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                            getActivity(), mLinearLayoutManager, mAdapter, lastItem, postData,
                                            mPaginationSynchronizer, mAcquireAccessTokenRequestQueue,
                                            mQueryPostUrl, mIsBestPost,
                                            mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                                            getResources().getConfiguration().locale));
                                    mProgressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onParsePostFail() {
                                    Toast.makeText(getActivity(), "Error parsing data", Toast.LENGTH_SHORT).show();
                                    Log.i("Post fetch error", "Error parsing data");
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    // Error indicating that there was an Authentication Failure while performing the request
                    // Access token expired
                    RefreshAccessToken.refreshAccessToken(getActivity(),
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    fetchPost(queryPostUrl, refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {}
                            });
                } else {
                    Log.i("Post fetch error", error.toString());
                    showErrorSnackbar();
                }
            }
        });
        postRequest.setTag(PostFragment.class);
        mRequestQueue.add(postRequest);
    }

    private void showErrorSnackbar() {
        mProgressBar.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Error getting post", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsBestPost) {
                    queryBestPost(1);
                } else {
                    fetchPost(mQueryPostUrl, 1);
                }
            }
        });
        snackbar.show();
    }
}