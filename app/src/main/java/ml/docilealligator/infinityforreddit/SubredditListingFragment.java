package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.Intent;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import javax.inject.Inject;
import javax.inject.Named;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubredditListingFragment extends Fragment implements FragmentCommunicator {

  static final String EXTRA_QUERY = "EQ";
  static final String EXTRA_IS_POSTING = "EIP";
  static final String EXTRA_ACCESS_TOKEN = "EAT";
  static final String EXTRA_ACCOUNT_NAME = "EAN";

  @BindView(R.id.coordinator_layout_subreddit_listing_fragment)
  CoordinatorLayout mCoordinatorLayout;
  @BindView(R.id.recycler_view_subreddit_listing_fragment)
  RecyclerView mSubredditListingRecyclerView;
  @BindView(R.id.progress_bar_subreddit_listing_fragment)
  CircleProgressBar mProgressBar;
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
  RedditDataRoomDatabase redditDataRoomDatabase;
  private SubredditListingRecyclerViewAdapter mAdapter;

  public SubredditListingFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_subreddit_listing, container, false);

    Activity activity = getActivity();

    ((Infinity) activity.getApplication()).getAppComponent().inject(this);

    ButterKnife.bind(this, rootView);

    Resources resources = getResources();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
          || resources.getBoolean(R.bool.isTablet)) {
        int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (navBarResourceId > 0) {
          mSubredditListingRecyclerView
              .setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
        }
      }
    }

    LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
    mSubredditListingRecyclerView.setLayoutManager(mLinearLayoutManager);

    String query = getArguments().getString(EXTRA_QUERY);
    boolean isPosting = getArguments().getBoolean(EXTRA_IS_POSTING);
    String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
    String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME);

    mAdapter = new SubredditListingRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit,
        accessToken, accountName, redditDataRoomDatabase,
        new SubredditListingRecyclerViewAdapter.Callback() {
          @Override
          public void retryLoadingMore() {
            mSubredditListingViewModel.retryLoadingMore();
          }

          @Override
          public void subredditSelected(String subredditName, String iconUrl) {
            if (isPosting) {
              ((SearchSubredditsResultActivity) activity)
                  .getSelectedSubreddit(subredditName, iconUrl);
            } else {
              Intent intent = new Intent(activity, ViewSubredditDetailActivity.class);
              intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
              activity.startActivity(intent);
            }
          }
        });

    mSubredditListingRecyclerView.setAdapter(mAdapter);

    SubredditListingViewModel.Factory factory = new SubredditListingViewModel.Factory(mRetrofit,
        query,
        PostDataSource.SORT_TYPE_RELEVANCE);
    mSubredditListingViewModel = new ViewModelProvider(this, factory)
        .get(SubredditListingViewModel.class);
    mSubredditListingViewModel.getSubreddits()
        .observe(this, subredditData -> mAdapter.submitList(subredditData));

    mSubredditListingViewModel.hasSubredditLiveData().observe(this, hasSubreddit -> {
      mProgressBar.setVisibility(View.GONE);
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
        mProgressBar.setVisibility(View.GONE);
      } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
        mProgressBar.setVisibility(View.GONE);
        mFetchSubredditListingInfoLinearLayout.setOnClickListener(view -> refresh());
        showErrorView(R.string.search_subreddits_error);
      } else {
        mProgressBar.setVisibility(View.VISIBLE);
      }
    });

    mSubredditListingViewModel.getPaginationNetworkState()
        .observe(this, networkState -> mAdapter.setNetworkState(networkState));

    return rootView;
  }

  private void showErrorView(int stringResId) {
    if (getActivity() != null && isAdded()) {
      mProgressBar.setVisibility(View.GONE);
      mFetchSubredditListingInfoLinearLayout.setVisibility(View.VISIBLE);
      mFetchSubredditListingInfoTextView.setText(stringResId);
      Glide.with(this).load(R.drawable.error_image).into(mFetchSubredditListingInfoImageView);
    }
  }

  void changeSortType(String sortType) {
    mSubredditListingViewModel.changeSortType(sortType);
  }

  @Override
  public void refresh() {
    mFetchSubredditListingInfoLinearLayout.setVisibility(View.GONE);
    mSubredditListingViewModel.refresh();
    mAdapter.setNetworkState(null);
  }
}
