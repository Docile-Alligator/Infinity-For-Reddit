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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String NAME_KEY = "NK";
    static final String POST_TYPE_KEY = "PTK";

    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPostRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private CircleProgressBar mProgressBar;
    private LinearLayout mFetchPostInfoLinearLayout;
    private ImageView mFetchPostInfoImageView;
    private TextView mFetchPostInfoTextView;

    private String mName;
    private int mPostType;

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
        mFetchPostInfoLinearLayout = rootView.findViewById(R.id.fetch_post_info_linear_layout_post_fragment);
        mFetchPostInfoImageView = rootView.findViewById(R.id.fetch_post_info_image_view_post_fragment);
        mFetchPostInfoTextView = rootView.findViewById(R.id.fetch_post_info_text_view_post_fragment);
        /*FloatingActionButton fab = rootView.findViewById(R.id.fab_post_fragment);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        mPostType = getArguments().getInt(POST_TYPE_KEY);

        if(mPostType != PostDataSource.TYPE_FRONT_PAGE) {
            mName = getArguments().getString(NAME_KEY);
        }

        if(mPostType == PostDataSource.TYPE_FRONT_PAGE) {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mOauthRetrofit,
                    mSharedPreferences, mPostType, () -> mPostViewModel.retryLoadingMore());
        } else {
            mAdapter = new PostRecyclerViewAdapter(getActivity(), mRetrofit,
                    mSharedPreferences, mPostType, () -> mPostViewModel.retryLoadingMore());
        }
        mPostRecyclerView.setAdapter(mAdapter);

        String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        PostViewModel.Factory factory;
        if(mPostType == PostDataSource.TYPE_FRONT_PAGE) {
            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, mPostType, new PostDataSource.OnPostFetchedCallback() {
                @Override
                public void hasPost() {
                    mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void noPost() {
                    mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                        //Do nothing
                    });
                    showErrorView(R.string.no_posts);
                }
            });
        } else {
            factory = new PostViewModel.Factory(mRetrofit,
                    getResources().getConfiguration().locale, mName, mPostType, new PostDataSource.OnPostFetchedCallback() {
                @Override
                public void hasPost() {
                    mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void noPost() {
                    mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                        //Do nothing
                    });
                    showErrorView(R.string.no_posts);
                }
            });
        }

        mPostViewModel = ViewModelProviders.of(this, factory).get(PostViewModel.class);
        mPostViewModel.getPosts().observe(this, posts -> mAdapter.submitList(posts));

        mPostViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchPostInfoLinearLayout.setOnClickListener(view -> mPostViewModel.retry());
                showErrorView(R.string.load_posts_error);
            } else {
                mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mPostViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
    }

    @Override
    public void refresh() {
        mPostViewModel.refresh();
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(getActivity() != null && isAdded()) {
            mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchPostInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.load_post_error_indicator).into(mFetchPostInfoImageView);
        }
    }
}