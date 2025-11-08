package ml.docilealligator.infinityforreddit.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.SubredditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentSubredditListingBinding;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditListingViewModel;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubredditListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_IS_GETTING_SUBREDDIT_INFO = "EIGSI";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";

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
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private SubredditListingRecyclerViewAdapter mAdapter;
    private BaseActivity mActivity;
    private SortType sortType;
    private FragmentSubredditListingBinding binding;

    public SubredditListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSubredditListingBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        if (mActivity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    binding.recyclerViewSubredditListingFragment.setPadding(
                            0, 0, 0, allInsets.bottom
                    );
                    return WindowInsetsCompat.CONSUMED;
                }
            });
            //binding.recyclerViewSubredditListingFragment.setPadding(0, 0, 0, mActivity.getNavBarHeight());
        }/* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewSubredditListingFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }*/

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(getActivity());
        binding.recyclerViewSubredditListingFragment.setLayoutManager(mLinearLayoutManager);

        String query = getArguments().getString(EXTRA_QUERY);
        boolean isGettingSubredditInfo = getArguments().getBoolean(EXTRA_IS_GETTING_SUBREDDIT_INFO);

        String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, SortType.Type.RELEVANCE.value);
        sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
        boolean nsfw = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mActivity.accountName) + SharedPreferencesUtils.NSFW_BASE, false);

        mAdapter = new SubredditListingRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRetrofit,
                mCustomThemeWrapper, mActivity.accessToken, mActivity.accountName,
                mRedditDataRoomDatabase, getArguments().getBoolean(EXTRA_IS_MULTI_SELECTION, false),
                new SubredditListingRecyclerViewAdapter.Callback() {
                    @Override
                    public void retryLoadingMore() {
                        mSubredditListingViewModel.retryLoadingMore();
                    }

                    @Override
                    public void subredditSelected(String subredditName, String iconUrl) {
                        if (isGettingSubredditInfo) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, subredditName);
                            returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, iconUrl);
                            mActivity.setResult(Activity.RESULT_OK, returnIntent);
                            mActivity.finish();
                        } else {
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                            mActivity.startActivity(intent);
                        }
                    }
                });

        binding.recyclerViewSubredditListingFragment.setAdapter(mAdapter);

        if (mActivity instanceof RecyclerViewContentScrollingInterface) {
            binding.recyclerViewSubredditListingFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollDown();
                    } else if (dy < 0) {
                        ((RecyclerViewContentScrollingInterface) mActivity).contentScrollUp();
                    }
                }
            });
        }

        SubredditListingViewModel.Factory factory = new SubredditListingViewModel.Factory(mExecutor,
                new Handler(), mOauthRetrofit, query, sortType, mActivity.accessToken, mActivity.accountName, nsfw);
        mSubredditListingViewModel = new ViewModelProvider(this, factory).get(SubredditListingViewModel.class);
        mSubredditListingViewModel.getSubreddits().observe(getViewLifecycleOwner(), subredditData -> mAdapter.submitList(subredditData));

        mSubredditListingViewModel.hasSubredditLiveData().observe(getViewLifecycleOwner(), hasSubreddit -> {
            binding.swipeRefreshLayoutSubredditListingFragment.setRefreshing(false);
            if (hasSubreddit) {
                binding.fetchSubredditListingInfoLinearLayoutSubredditListingFragment.setVisibility(View.GONE);
            } else {
                binding.fetchSubredditListingInfoLinearLayoutSubredditListingFragment.setOnClickListener(null);
                showErrorView(R.string.no_subreddits);
            }
        });

        mSubredditListingViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                binding.swipeRefreshLayoutSubredditListingFragment.setRefreshing(false);
            } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                binding.swipeRefreshLayoutSubredditListingFragment.setRefreshing(false);
                binding.fetchSubredditListingInfoLinearLayoutSubredditListingFragment.setOnClickListener(view -> refresh());
                showErrorView(R.string.search_subreddits_error);
            } else {
                binding.swipeRefreshLayoutSubredditListingFragment.setRefreshing(true);
            }
        });

        mSubredditListingViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        binding.swipeRefreshLayoutSubredditListingFragment.setOnRefreshListener(() -> mSubredditListingViewModel.refresh());

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    private void showErrorView(int stringResId) {
        if (getActivity() != null && isAdded()) {
            binding.swipeRefreshLayoutSubredditListingFragment.setRefreshing(false);
            binding.fetchSubredditListingInfoLinearLayoutSubredditListingFragment.setVisibility(View.VISIBLE);
            binding.fetchSubredditListingInfoTextViewSubredditListingFragment.setText(stringResId);
            Glide.with(this).load(R.drawable.error_image).into(binding.fetchSubredditListingInfoImageViewSubredditListingFragment);
        }
    }

    public void changeSortType(SortType sortType) {
        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, sortType.getType().name()).apply();
        mSubredditListingViewModel.changeSortType(sortType);
        this.sortType = sortType;
    }

    @Override
    public void refresh() {
        binding.fetchSubredditListingInfoLinearLayoutSubredditListingFragment.setVisibility(View.GONE);
        mSubredditListingViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayoutSubredditListingFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutSubredditListingFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchSubredditListingInfoTextViewSubredditListingFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.fetchSubredditListingInfoTextViewSubredditListingFragment.setTypeface(mActivity.contentTypeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public SortType getSortType() {
        return sortType;
    }

    public ArrayList<String> getSelectedSubredditNames() {
        if (mSubredditListingViewModel != null) {
            List<SubredditData> allSubreddits = mSubredditListingViewModel.getSubreddits().getValue();
            if (allSubreddits == null) {
                return null;
            }

            ArrayList<String> selectedSubreddits = new ArrayList<>();
            for (SubredditData s : allSubreddits) {
                if (s.isSelected()) {
                    selectedSubreddits.add(s.getName());
                }
            }
            return selectedSubreddits;
        }

        return null;
    }
}
