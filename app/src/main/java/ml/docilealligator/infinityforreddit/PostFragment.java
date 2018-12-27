package ml.docilealligator.infinityforreddit;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String SUBREDDIT_NAME_KEY = "SNK";
    static final String IS_BEST_POST_KEY = "IBPK";

    private static final String LAST_ITEM_STATE = "LIS";
    private static final String LOADING_STATE_STATE = "LSS";
    private static final String LOAD_SUCCESS_STATE = "LOSS";
    private static final String IS_REFRESH_STATE = "IRS";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private LinearLayout mFetchPostErrorLinearLayout;
    private ImageView mFetchPostErrorImageView;

    private boolean mIsBestPost;
    private String mSubredditName;

    private PostRecyclerViewAdapter mAdapter;

    PostViewModel mPostViewModel;

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
            /*mFetchPostErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mIsBestPost) {
                        fetchBestPost();
                    } else {
                        fetchPost();
                    }
                }
            });*/
        }

        if(mIsBestPost) {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mOauthRetrofit,
                    mSharedPreferences, mIsBestPost);
        } else {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mRetrofit,
                    mSharedPreferences, mIsBestPost);
        }
        mPostRecyclerView.setAdapter(mAdapter);

        String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        PostViewModel.Factory factory;
        if(mIsBestPost) {
            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, mIsBestPost);
        } else {
            factory = new PostViewModel.Factory(mRetrofit,
                    getResources().getConfiguration().locale, mIsBestPost, mSubredditName);
        }
        mPostViewModel = ViewModelProviders.of(this, factory).get(PostViewModel.class);
        mPostViewModel.getPosts().observe(this, posts -> mAdapter.submitList(posts));

        return rootView;
    }

    @Override
    public void refresh() {

    }

    /*
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
    }*/
}