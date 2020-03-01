package ml.docilealligator.infinityforreddit.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import ml.docilealligator.infinityforreddit.Activity.BaseActivity;
import ml.docilealligator.infinityforreddit.Activity.SearchSubredditsResultActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Adapter.SubredditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SubredditListingViewModel;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubredditListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_IS_POSTING = "EIP";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";

    @BindView(R.id.coordinator_layout_subreddit_listing_fragment)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_subreddit_listing_fragment)
    RecyclerView mSubredditListingRecyclerView;
    @BindView(R.id.swipe_refresh_layout_subreddit_listing_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fetch_subreddit_listing_info_linear_layout_subreddit_listing_fragment)
    LinearLayout mFetchSubredditListingInfoLinearLayout;
    @BindView(R.id.fetch_subreddit_listing_info_image_view_subreddit_listing_fragment)
    ImageView mFetchSubredditListingInfoImageView;
    @BindView(R.id.fetch_subreddit_listing_info_text_view_subreddit_listing_fragment)
    TextView mFetchSubredditListingInfoTextView;
    SubredditListingViewModel mSubredditListingViewModel;
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
    private SubredditListingRecyclerViewAdapter mAdapter;
    private Activity mActivity;

    public SubredditListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_subreddit_listing, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        Resources resources = getResources();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            mSubredditListingRecyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mSubredditListingRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mSubredditListingRecyclerView.setLayoutManager(mLinearLayoutManager);

        String query = getArguments().getString(EXTRA_QUERY);
        boolean isPosting = getArguments().getBoolean(EXTRA_IS_POSTING);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

        String sort = mSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, SortType.Type.RELEVANCE.value);
        SortType sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));

        mAdapter = new SubredditListingRecyclerViewAdapter(mActivity, mOauthRetrofit, mRetrofit,
                accessToken, accountName, mRedditDataRoomDatabase,
                new SubredditListingRecyclerViewAdapter.Callback() {
                    @Override
                    public void retryLoadingMore() {
                        mSubredditListingViewModel.retryLoadingMore();
                    }

                    @Override
                    public void subredditSelected(String subredditName, String iconUrl) {
                        if (isPosting) {
                            ((SearchSubredditsResultActivity) mActivity).getSelectedSubreddit(subredditName, iconUrl);
                        } else {
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                            mActivity.startActivity(intent);
                        }
                    }
                });

        mSubredditListingRecyclerView.setAdapter(mAdapter);

        SubredditListingViewModel.Factory factory = new SubredditListingViewModel.Factory(mRetrofit, query, sortType);
        mSubredditListingViewModel = new ViewModelProvider(this, factory).get(SubredditListingViewModel.class);
        mSubredditListingViewModel.getSubreddits().observe(this, subredditData -> mAdapter.submitList(subredditData));

        mSubredditListingViewModel.hasSubredditLiveData().observe(this, hasSubreddit -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasSubreddit) {
                mFetchSubredditListingInfoLinearLayout.setVisibility(View.GONE);
            } else {
                mFetchSubredditListingInfoLinearLayout.setOnClickListener(view -> {
                    //Do nothing
                });
                showErrorView(R.string.no_subreddits);
            }
        });

        mSubredditListingViewModel.getInitialLoadingState().observe(this, networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mSwipeRefreshLayout.setRefreshing(false);
                mFetchSubredditListingInfoLinearLayout.setOnClickListener(view -> refresh());
                showErrorView(R.string.search_subreddits_error);
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mSubredditListingViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> mSubredditListingViewModel.refresh());
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Utils.getAttributeColor(mActivity, R.attr.cardViewBackgroundColor));
        mSwipeRefreshLayout.setColorSchemeColors(Utils.getAttributeColor(mActivity, R.attr.colorAccent));

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
            mFetchSubredditListingInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchSubredditListingInfoTextView.setText(stringResId);
            Glide.with(this).load(R.drawable.error_image).into(mFetchSubredditListingInfoImageView);
        }
    }

    public void changeSortType(SortType sortType) {
        mSubredditListingViewModel.changeSortType(sortType);
    }

    @Override
    public void refresh() {
        mFetchSubredditListingInfoLinearLayout.setVisibility(View.GONE);
        mSubredditListingViewModel.refresh();
        mAdapter.setNetworkState(null);
    }
}
