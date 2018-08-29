package ml.docilealligator.infinityforreddit;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {

    static final String SUBREDDIT_NAME_KEY = "SNK";
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
    private String mSubredditName;
    private String PostDataParcelableState = "BPDPS";
    private String lastItemState = "LIS";
    private String paginationSynchronizerState = "PSS";

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
                mAdapter = new PostRecyclerViewAdapter(getActivity(), mPostData, mPaginationSynchronizer);
                mPostRecyclerView.setAdapter(mAdapter);
                mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                        getActivity(), mLinearLayoutManager, mAdapter, mLastItem, mPostData,
                        mPaginationSynchronizer, mSubredditName, mIsBestPost,
                        mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                        getResources().getConfiguration().locale));
                mProgressBar.setVisibility(View.GONE);
            } else {
                if(mIsBestPost) {
                    fetchBestPost(1);
                } else {
                    fetchPost(mSubredditName, 1);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

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

        mIsBestPost = getArguments().getBoolean(IS_BEST_POST_KEY);
        if(!mIsBestPost) {
            mSubredditName = getArguments().getString(SUBREDDIT_NAME_KEY);
        }

        if(savedInstanceState != null && savedInstanceState.getParcelable(paginationSynchronizerState) != null) {
            mPaginationSynchronizer = savedInstanceState.getParcelable(paginationSynchronizerState);
        } else {
            mPaginationSynchronizer = new PaginationSynchronizer();
            if(mIsBestPost) {
                fetchBestPost(1);
            } else {
                fetchPost(mSubredditName, 1);
            }
        }

        LastItemSynchronizer lastItemSynchronizer = new LastItemSynchronizer() {
            @Override
            public void lastItemChanged(String lastItem) {
                mLastItem = lastItem;
            }
        };
        mPaginationSynchronizer.setLastItemSynchronizer(lastItemSynchronizer);

        return rootView;
    }

    private void fetchBestPost(final int refreshTime) {
        if(refreshTime < 0) {
            showErrorSnackbar();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);

        String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> bestPost = api.getBestPost(mLastItem, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(getActivity() != null) {

                    if(response.isSuccessful()) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("response", response.body());
                        clipboard.setPrimaryClip(clip);

                        ParsePost.parsePost(response.body(), new ArrayList<PostData>(),
                                getResources().getConfiguration().locale, new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                                        mPostData = postData;
                                        mLastItem = lastItem;
                                        mAdapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer);

                                        mPostRecyclerView.setAdapter(mAdapter);
                                        mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                                getActivity(), mLinearLayoutManager, mAdapter, lastItem, postData,
                                                mPaginationSynchronizer, mSubredditName, mIsBestPost,
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
                    } else if(response.code() == 401) {
                        // Error indicating that there was an Authentication Failure while performing the request
                        // Access token expired
                        RefreshAccessToken.refreshAccessToken(getActivity(),
                                new RefreshAccessToken.RefreshAccessTokenListener() {
                                    @Override
                                    public void onRefreshAccessTokenSuccess() {
                                        fetchBestPost(refreshTime - 1);
                                    }

                                    @Override
                                    public void onRefreshAccessTokenFail() {}
                                });
                    } else {
                        Log.i("Post fetch error", response.message());
                        showErrorSnackbar();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showErrorSnackbar();
            }
        });
    }

    private void fetchPost(final String queryPostUrl, final int refreshTime) {
        if(refreshTime < 0) {
            showErrorSnackbar();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost = api.getPost(mSubredditName, mLastItem);
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(getActivity() != null) {
                    Log.i("response_code", Integer.toString(response.code()));
                    if(response.isSuccessful()) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("response", response.body());
                        clipboard.setPrimaryClip(clip);

                        ParsePost.parsePost(response.body(), new ArrayList<PostData>(),
                                getResources().getConfiguration().locale, new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                                        mPostData = postData;
                                        mLastItem = lastItem;
                                        mAdapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer);

                                        mPostRecyclerView.setAdapter(mAdapter);
                                        mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                                getActivity(), mLinearLayoutManager, mAdapter, lastItem, postData,
                                                mPaginationSynchronizer, mSubredditName, mIsBestPost,
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
                    } else {
                        Log.i("Post fetch error", response.message());
                        showErrorSnackbar();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showErrorSnackbar();
            }
        });
    }

    private void showErrorSnackbar() {
        mProgressBar.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Error getting post", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsBestPost) {
                    fetchBestPost(1);
                } else {
                    fetchPost(mSubredditName, 1);
                }
            }
        });
        snackbar.show();
    }
}