package ml.docilealligator.infinityforreddit;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String SUBREDDIT_NAME_KEY = "SNK";
    static final String IS_BEST_POST_KEY = "IBPK";

    private static final String POST_DATA_PARCELABLE_STATE = "PDPS";
    private static final String LAST_ITEM_STATE = "LIS";
    private static final String LOADING_STATE_STATE = "LSS";
    private static final String LOAD_SUCCESS_STATE = "LOSS";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private LinearLayout mFetchPostErrorLinearLayout;
    private ImageView mFetchPostErrorImageView;

    private ArrayList<Post> mPostData;
    private String mLastItem;
    private PaginationSynchronizer mPaginationSynchronizer;

    private boolean mIsBestPost;
    private String mSubredditName;

    private PostRecyclerViewAdapter mAdapter;

    private PostViewModel mPostViewModel;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject @Named("auth_info")
    SharedPreferences mSharedPreferences;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LAST_ITEM_STATE, mLastItem);
        outState.putBoolean(LOADING_STATE_STATE, mPaginationSynchronizer.isLoading());
        outState.putBoolean(LOAD_SUCCESS_STATE, mPaginationSynchronizer.isLoadingMorePostsSuccess());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPostRecyclerView.getAdapter() != null) {
            ((PostRecyclerViewAdapter) mPostRecyclerView.getAdapter()).setCanStartActivity(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        ((Infinity) getActivity().getApplication()).getmNetworkComponent().inject(this);

        mCoordinatorLayout = rootView.findViewById(R.id.coordinator_layout_post_fragment);
        mPostRecyclerView = rootView.findViewById(R.id.recycler_view_post_fragment);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar = rootView.findViewById(R.id.progress_bar_post_fragment);
        mFetchPostErrorLinearLayout = rootView.findViewById(R.id.fetch_post_error_linear_layout_post_fragment);
        mFetchPostErrorImageView = rootView.findViewById(R.id.fetch_post_error_image_view_post_fragment);
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
        } else {
            mFetchPostErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mIsBestPost) {
                        fetchBestPost();
                    } else {
                        fetchPost();
                    }
                }
            });
        }

        mPaginationSynchronizer = new PaginationSynchronizer();
        mPaginationSynchronizer.addLastItemSynchronizer(new LastItemSynchronizer() {
            @Override
            public void lastItemChanged(String lastItem) {
                mLastItem = lastItem;
            }
        });

        mPostViewModel = ViewModelProviders.of(this).get(PostViewModel.class);
        mPostViewModel.getPosts().observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Post> posts) {
                mAdapter.changeDataSet(posts);
                if(posts == null) {
                    Log.i("datachange", Integer.toString(0));
                } else {
                    Log.i("datachange", Integer.toString(posts.size()));
                }
            }
        });

        if(mIsBestPost) {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mOauthRetrofit,
                    mSharedPreferences, mPaginationSynchronizer, mIsBestPost);

            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                    getActivity(), mOauthRetrofit, mPostViewModel, mLinearLayoutManager,
                    mLastItem, mPaginationSynchronizer, mSubredditName, mIsBestPost,
                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadingMorePostsSuccess(),
                    getResources().getConfiguration().locale));
        } else {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mRetrofit,
                    mSharedPreferences, mPaginationSynchronizer, mIsBestPost);

            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                    getActivity(), mRetrofit, mPostViewModel, mLinearLayoutManager,
                    mLastItem, mPaginationSynchronizer, mSubredditName, mIsBestPost,
                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadingMorePostsSuccess(),
                    getResources().getConfiguration().locale));
        }
        mPostRecyclerView.setAdapter(mAdapter);

        if(savedInstanceState != null && savedInstanceState.containsKey(LAST_ITEM_STATE)) {
            mLastItem = savedInstanceState.getString(LAST_ITEM_STATE);

            mPaginationSynchronizer.notifyLastItemChanged(mLastItem);
            mPaginationSynchronizer.setLoadSuccess(savedInstanceState.getBoolean(LOAD_SUCCESS_STATE));
            mPaginationSynchronizer.setLoadingState(savedInstanceState.getBoolean(LOADING_STATE_STATE));

            mProgressBar.setVisibility(View.GONE);
        } else {
            if(mIsBestPost) {
                fetchBestPost();
            } else {
                fetchPost();
            }
        }

        return rootView;
    }

    private void fetchBestPost() {
        mFetchPostErrorLinearLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        RedditAPI api = mOauthRetrofit.create(RedditAPI.class);

        String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> bestPost = api.getBestPost(mLastItem, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(getActivity() != null) {
                    if(response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), getResources().getConfiguration().locale,
                                new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                        if(isAdded() && getActivity() != null) {
                                            mLastItem = lastItem;
                                            mPaginationSynchronizer.notifyLastItemChanged(lastItem);
                                            mPostViewModel.setPosts(newPosts);
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onParsePostFail() {
                                        Log.i("Post fetch error", "Error parsing data");
                                        showErrorView();
                                    }
                                });
                    } else {
                        Log.i("Post fetch error", response.message());
                        showErrorView();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showErrorView();
            }
        });
    }

    private void fetchPost() {
        mFetchPostErrorLinearLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        RedditAPI api = mRetrofit.create(RedditAPI.class);
        Call<String> getPost = api.getPost(mSubredditName, mLastItem);
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                if(getActivity() != null) {
                    if(response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), getResources().getConfiguration().locale,
                                new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                        if(isAdded() && getActivity() != null) {
                                            mLastItem = lastItem;
                                            mPaginationSynchronizer.notifyLastItemChanged(lastItem);
                                            mPostViewModel.setPosts(newPosts);
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onParsePostFail() {
                                        Log.i("Post fetch error", "Error parsing data");
                                        showErrorView();
                                    }
                                });
                    } else {
                        Log.i("Post fetch error", response.message());
                        showErrorView();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showErrorView();
            }
        });
    }

    private void showErrorView() {
        mProgressBar.setVisibility(View.GONE);
        if(mIsBestPost) {
            if(getActivity() != null && isAdded()) {
                mFetchPostErrorLinearLayout.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.load_post_error_indicator).into(mFetchPostErrorImageView);
            }
        } else {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Error getting post", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mIsBestPost) {
                        fetchBestPost();
                    } else {
                        fetchPost();
                    }
                }
            });
            snackbar.show();
        }
    }

    @Override
    public void refresh() {
        mLastItem = null;
        mPostRecyclerView.clearOnScrollListeners();
        mPostRecyclerView.getRecycledViewPool().clear();

        mPostViewModel.setPosts(null);

        if(mIsBestPost) {
            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                    getActivity(), mOauthRetrofit, mPostViewModel, mLinearLayoutManager,
                    mLastItem, mPaginationSynchronizer, mSubredditName, mIsBestPost,
                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadingMorePostsSuccess(),
                    getResources().getConfiguration().locale));

            fetchBestPost();
        } else {
            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                    getActivity(), mRetrofit, mPostViewModel, mLinearLayoutManager,
                    mLastItem, mPaginationSynchronizer, mSubredditName, mIsBestPost,
                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadingMorePostsSuccess(),
                    getResources().getConfiguration().locale));

            fetchPost();
        }
    }
}