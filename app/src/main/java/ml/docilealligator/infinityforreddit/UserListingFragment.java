package ml.docilealligator.infinityforreddit;


import android.content.Context;
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

import SubscribedUserDatabase.SubscribedUserRoomDatabase;
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
public class UserListingFragment extends Fragment {
    static final String QUERY_KEY = "QK";

    @BindView(R.id.coordinator_layout_user_listing_fragment)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_user_listing_fragment)
    RecyclerView mUserListingRecyclerView;
    @BindView(R.id.progress_bar_user_listing_fragment)
    CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_user_listing_info_linear_layout_user_listing_fragment)
    LinearLayout mFetchUserListingInfoLinearLayout;
    @BindView(R.id.fetch_user_listing_info_image_view_user_listing_fragment)
    ImageView mFetchUserListingInfoImageView;
    @BindView(R.id.fetch_user_listing_info_text_view_user_listing_fragment)
    TextView mFetchUserListingInfoTextView;

    private LinearLayoutManager mLinearLayoutManager;

    private String mQuery;

    private UserListingRecyclerViewAdapter mAdapter;

    UserListingViewModel mUserListingViewModel;

    @Inject
    @Named("auth_info")
    SharedPreferences mAuthInfoSharedPreferences;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    public UserListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_listing, container, false);

        ((Infinity) getActivity().getApplication()).getmNetworkComponent().inject(this);

        ButterKnife.bind(this, rootView);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mUserListingRecyclerView.setLayoutManager(mLinearLayoutManager);

        mQuery = getArguments().getString(QUERY_KEY);

        String accessToken = getActivity().getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        UserListingViewModel.Factory factory = new UserListingViewModel.Factory(mRetrofit, mQuery,
                new UserListingDataSource.OnUserListingDataFetchedCallback() {
                    @Override
                    public void hasUser() {
                        mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void noUser() {
                        mFetchUserListingInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Do nothing
                            }
                        });
                        showErrorView(R.string.no_users);
                    }
                });

        mAdapter = new UserListingRecyclerViewAdapter(getActivity(), mOauthRetrofit, mRetrofit,
                mAuthInfoSharedPreferences,
                SubscribedUserRoomDatabase.getDatabase(getContext()).subscribedUserDao(),
                () -> mUserListingViewModel.retryLoadingMore());

        mUserListingRecyclerView.setAdapter(mAdapter);

        mUserListingViewModel = ViewModelProviders.of(this, factory).get(UserListingViewModel.class);
        mUserListingViewModel.getUsers().observe(this, UserData -> mAdapter.submitList(UserData));

        mUserListingViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchUserListingInfoLinearLayout.setOnClickListener(view -> mUserListingViewModel.retry());
                showErrorView(R.string.load_posts_error);
            } else {
                mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
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
            Glide.with(this).load(R.drawable.load_post_error_indicator).into(mFetchUserListingInfoImageView);
        }
    }
}
