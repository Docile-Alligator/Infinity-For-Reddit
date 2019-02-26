package ml.docilealligator.infinityforreddit;


import android.content.SharedPreferences;
import android.os.Bundle;
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

import SubscribedSubredditDatabase.SubscribedSubredditRoomDatabase;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubredditListingFragment extends Fragment implements FragmentCommunicator {

    static final String QUERY_KEY = "QK";

    @BindView(R.id.coordinator_layout_subreddit_listing_fragment) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_subreddit_listing_fragment) RecyclerView mSubredditListingRecyclerView;
    @BindView(R.id.progress_bar_subreddit_listing_fragment) CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_subreddit_listing_info_linear_layout_subreddit_listing_fragment) LinearLayout mFetchSubredditListingInfoLinearLayout;
    @BindView(R.id.fetch_subreddit_listing_info_image_view_subreddit_listing_fragment) ImageView mFetchSubredditListingInfoImageView;
    @BindView(R.id.fetch_subreddit_listing_info_text_view_subreddit_listing_fragment) TextView mFetchSubredditListingInfoTextView;

    private LinearLayoutManager mLinearLayoutManager;

    private String mQuery;

    private SubredditListingRecyclerViewAdapter mAdapter;

    SubredditListingViewModel mSubredditListingViewModel;

    @Inject @Named("auth_info")
    SharedPreferences mAuthInfoSharedPreferences;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    public SubredditListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_subreddit_listing, container, false);

        ((Infinity) getActivity().getApplication()).getmNetworkComponent().inject(this);

        ButterKnife.bind(this, rootView);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mSubredditListingRecyclerView.setLayoutManager(mLinearLayoutManager);

        mQuery = getArguments().getString(QUERY_KEY);

        SubredditListingViewModel.Factory factory = new SubredditListingViewModel.Factory(mRetrofit, mQuery,
                new SubredditListingDataSource.OnSubredditListingDataFetchedCallback() {
            @Override
            public void hasSubreddit() {
                mFetchSubredditListingInfoLinearLayout.setVisibility(View.GONE);
            }

            @Override
            public void noSubreddit() {
                mFetchSubredditListingInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Do nothing
                    }
                });
                showErrorView(R.string.no_subreddits);
            }
        });

        mAdapter = new SubredditListingRecyclerViewAdapter(getActivity(), mOauthRetrofit, mRetrofit,
                mAuthInfoSharedPreferences,
                SubscribedSubredditRoomDatabase.getDatabase(getContext()).subscribedSubredditDao(),
                () -> mSubredditListingViewModel.retryLoadingMore());

        mSubredditListingRecyclerView.setAdapter(mAdapter);

        mSubredditListingViewModel = ViewModelProviders.of(this, factory).get(SubredditListingViewModel.class);
        mSubredditListingViewModel.getSubreddits().observe(this, subredditData -> mAdapter.submitList(subredditData));

        mSubredditListingViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchSubredditListingInfoLinearLayout.setOnClickListener(view -> mSubredditListingViewModel.retry());
                showErrorView(R.string.search_subreddits_error);
            } else {
                mFetchSubredditListingInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mSubredditListingViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(getActivity() != null && isAdded()) {
            mFetchSubredditListingInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchSubredditListingInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.load_post_error_indicator).into(mFetchSubredditListingInfoImageView);
        }
    }

    @Override
    public void refresh() {
        mSubredditListingViewModel.refresh();
    }
}
