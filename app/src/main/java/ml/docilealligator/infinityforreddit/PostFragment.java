package ml.docilealligator.infinityforreddit;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String SUBREDDIT_NAME_KEY = "SNK";
    static final String IS_BEST_POST_KEY = "IBPK";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private CircleProgressBar mProgressBar;
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
            mFetchPostErrorLinearLayout.setOnClickListener(view -> mPostViewModel.retry());
        }

        if(mIsBestPost) {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mOauthRetrofit,
                    mSharedPreferences, mIsBestPost, () -> mPostViewModel.retryLoadingMore());
        } else {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mRetrofit,
                    mSharedPreferences, mIsBestPost, () -> mPostViewModel.retryLoadingMore());
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

        mPostViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                showErrorView();
            } else {
                mFetchPostErrorLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mPostViewModel.getPaginationNetworkState().observe(this, networkState -> {
            Log.i("networkstate", networkState.getStatus().toString());
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
    }

    @Override
    public void refresh() {

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
            snackbar.setAction(R.string.retry, view -> mPostViewModel.retry());
            snackbar.show();
        }
    }
}