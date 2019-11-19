package ml.docilealligator.infinityforreddit.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.UserListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.UserListingViewModel;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.coordinator_layout_user_listing_fragment)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_user_listing_fragment)
    RecyclerView mUserListingRecyclerView;
    @BindView(R.id.swipe_refresh_layout_user_listing_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fetch_user_listing_info_linear_layout_user_listing_fragment)
    LinearLayout mFetchUserListingInfoLinearLayout;
    @BindView(R.id.fetch_user_listing_info_image_view_user_listing_fragment)
    ImageView mFetchUserListingInfoImageView;
    @BindView(R.id.fetch_user_listing_info_text_view_user_listing_fragment)
    TextView mFetchUserListingInfoTextView;
    UserListingViewModel mUserListingViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private LinearLayoutManager mLinearLayoutManager;
    private String mQuery;
    private UserListingRecyclerViewAdapter mAdapter;
    private Activity mActivity;

    public UserListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_listing, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

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
        String sort = mSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_USER, SortType.Type.RELEVANCE.value);
        SortType sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));

        mAdapter = new UserListingRecyclerViewAdapter(getActivity(), mOauthRetrofit, mRetrofit,
                accessToken, accountName, mRedditDataRoomDatabase.subscribedUserDao(),
                () -> mUserListingViewModel.retryLoadingMore());

        mUserListingRecyclerView.setAdapter(mAdapter);

        UserListingViewModel.Factory factory = new UserListingViewModel.Factory(mRetrofit, mQuery,
                sortType);
        mUserListingViewModel = new ViewModelProvider(this, factory).get(UserListingViewModel.class);
        mUserListingViewModel.getUsers().observe(this, UserData -> mAdapter.submitList(UserData));

        mUserListingViewModel.hasUser().observe(this, hasUser -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasUser) {
                mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchUserListingInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_users);
            }
        });

        mUserListingViewModel.getInitialLoadingState().observe(this, networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mSwipeRefreshLayout.setRefreshing(false);
                mFetchUserListingInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.search_users_error);
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mUserListingViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> mUserListingViewModel.refresh());

        TypedValue typedValue = new TypedValue();
        mActivity.getTheme().resolveAttribute(R.attr.cardViewBackgroundColor, typedValue, true);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(typedValue.data);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    private void showErrorView(int stringResId) {
        if (getActivity() != null && isAdded()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mFetchUserListingInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchUserListingInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.error_image).into(mFetchUserListingInfoImageView);
        }
    }

    public void changeSortType(SortType sortType) {
        mUserListingViewModel.changeSortType(sortType);
    }

    @Override
    public void refresh() {
        mFetchUserListingInfoLinearLayout.setVisibility(View.GONE);
        mUserListingViewModel.refresh();
        mAdapter.setNetworkState(null);
    }
}
