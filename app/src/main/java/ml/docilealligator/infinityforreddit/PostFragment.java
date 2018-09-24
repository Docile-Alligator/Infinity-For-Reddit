package ml.docilealligator.infinityforreddit;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String SUBREDDIT_NAME_KEY = "SNK";
    static final String IS_BEST_POST_KEY = "IBPK";

    private static final String PostDataParcelableState = "BPDPS";
    private static final String lastItemState = "LIS";
    private static final String paginationSynchronizerState = "PSS";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private LinearLayout mFetchPostErrorLinearLayout;
    private ImageView mFetchPostErrorImageView;

    private ArrayList<PostData> mPostData;
    private String mLastItem;
    private PaginationSynchronizer mPaginationSynchronizer;

    private boolean mIsBestPost;
    private String mSubredditName;

    public PostFragment() {
        // Required empty public constructor
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
        if(mPostRecyclerView.getAdapter() != null) {
            ((PostRecyclerViewAdapter) mPostRecyclerView.getAdapter()).setCanStartActivity(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
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
                        fetchBestPost(1);
                    } else {
                        fetchPost();
                    }
                }
            });
        }

        if(savedInstanceState != null && savedInstanceState.containsKey(PostDataParcelableState)) {
            mPostData = savedInstanceState.getParcelableArrayList(PostDataParcelableState);
            mLastItem = savedInstanceState.getString(lastItemState);
            mPaginationSynchronizer = savedInstanceState.getParcelable(paginationSynchronizerState);
            PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getActivity(), mPostData, mPaginationSynchronizer);
            mPostRecyclerView.setAdapter(adapter);
            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                    getActivity(), mLinearLayoutManager, adapter, mLastItem, mPostData,
                    mPaginationSynchronizer, mSubredditName, mIsBestPost,
                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                    getResources().getConfiguration().locale));
            mProgressBar.setVisibility(View.GONE);
        } else {
            mPaginationSynchronizer = new PaginationSynchronizer(new LastItemSynchronizer() {
                @Override
                public void lastItemChanged(String lastItem) {
                    mLastItem = lastItem;
                }
            });
            if(mIsBestPost) {
                fetchBestPost(1);
            } else {
                fetchPost();
            }
        }

        return rootView;
    }

    private void fetchBestPost(final int refreshTime) {
        if(refreshTime < 0) {
            showErrorView();
            return;
        }

        mFetchPostErrorLinearLayout.setVisibility(View.GONE);
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
                                        if(isAdded() && getActivity() != null) {
                                            mPostData = postData;
                                            mLastItem = lastItem;
                                            PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer);

                                            mPostRecyclerView.setAdapter(adapter);
                                            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                                    getActivity(), mLinearLayoutManager, adapter, lastItem, postData,
                                                    mPaginationSynchronizer, mSubredditName, mIsBestPost,
                                                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                                                    getResources().getConfiguration().locale));
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onParsePostFail() {
                                        Log.i("Post fetch error", "Error parsing data");
                                        showErrorView();
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
                    if(response.isSuccessful()) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("response", response.body());
                        clipboard.setPrimaryClip(clip);

                        ParsePost.parsePost(response.body(), new ArrayList<PostData>(),
                                getResources().getConfiguration().locale, new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<PostData> postData, String lastItem) {
                                        if(isAdded() && getActivity() != null) {
                                            mPostData = postData;
                                            mLastItem = lastItem;
                                            PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getActivity(), postData, mPaginationSynchronizer);

                                            mPostRecyclerView.setAdapter(adapter);
                                            mPostRecyclerView.addOnScrollListener(new PostPaginationScrollListener(
                                                    getActivity(), mLinearLayoutManager, adapter, lastItem, postData,
                                                    mPaginationSynchronizer, mSubredditName, mIsBestPost,
                                                    mPaginationSynchronizer.isLoading(), mPaginationSynchronizer.isLoadSuccess(),
                                                    getResources().getConfiguration().locale));
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
                        fetchBestPost(1);
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
        mPaginationSynchronizer = new PaginationSynchronizer(new LastItemSynchronizer() {
            @Override
            public void lastItemChanged(String lastItem) {
                mLastItem = lastItem;
            }
        });
        mPostRecyclerView.clearOnScrollListeners();
        mPostRecyclerView.getRecycledViewPool().clear();
        if(mPostData != null) {
            mPostData.clear();
        }
        mPostData = null;
        if(mPostRecyclerView.getAdapter() != null) {
            (mPostRecyclerView.getAdapter()).notifyDataSetChanged();
        }
        if(mIsBestPost) {
            fetchBestPost(1);
        } else {
            fetchPost();
        }
    }
}