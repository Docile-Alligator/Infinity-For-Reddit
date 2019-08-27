package ml.docilealligator.infinityforreddit;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserListingFragment extends Fragment implements FragmentCommunicator {

    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_ACCESS_TOKEN = "EAT";
    static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.coordinator_layout_user_listing_fragment) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_user_listing_fragment) RecyclerView mUserListingRecyclerView;
    @BindView(R.id.progress_bar_user_listing_fragment) CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_user_listing_info_linear_layout_user_listing_fragment) LinearLayout mFetchUserListingInfoLinearLayout;
    @BindView(R.id.fetch_user_listing_info_image_view_user_listing_fragment) ImageView mFetchUserListingInfoImageView;
    @BindView(R.id.fetch_user_listing_info_text_view_user_listing_fragment) TextView mFetchUserListingInfoTextView;

    private LinearLayoutManager mLinearLayoutManager;

    private String mQuery;

    private UserListingRecyclerViewAdapter mAdapter;

    UserListingViewModel mUserListingViewModel;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;

    public UserListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_listing, container, false);

        ((Infinity) getActivity().getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    mUserListingRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
                }
            }
        }

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mUserListingRecyclerView.setLayoutManager(mLinearLayoutManager);

        mQuery = getArguments().getString(EXTRA_QUERY);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        mAdapter = new UserListingRecyclerViewAdapter(getActivity(), mOauthRetrofit, mRetrofit,
                accessToken, accountName, redditDataRoomDatabase.subscribedUserDao(),
                () -> mUserListingViewModel.retryLoadingMore());

        mUserListingRecyclerView.setAdapter(mAdapter);

        UserListingViewModel.Factory factory = new UserListingViewModel.Factory(mRetrofit, mQuery,
                PostDataSource.SORT_TYPE_RELEVANCE);
        mUserListingViewModel = new ViewModelProvider(this, factory).get(UserListingViewModel.class);
        mUserListingViewModel.getUsers().observe(this, UserData -> mAdapter.submitList(UserData));

        mUserListingViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchUserListingInfoLinearLayout.setOnClickListener(view -> mUserListingViewModel.retry());
                showErrorView(R.string.search_users_error);
            } else {
                mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mUserListingViewModel.hasUser().observe(this, hasUser -> {
            if(hasUser) {
                mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchUserListingInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_users);
            }
        });

        mUserListingViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(getActivity() != null && isAdded()) {
            mFetchUserListingInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchUserListingInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.error_image).into(mFetchUserListingInfoImageView);
        }
    }

    void changeSortType(String sortType) {
        mUserListingViewModel.changeSortType(sortType);
    }

    @Override
    public void refresh() {
        mUserListingViewModel.refresh();
    }
}
