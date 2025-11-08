package ml.docilealligator.infinityforreddit.fragments;

import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.INDEX_UNSET;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.TIME_UNSET;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.FetchPostFilterAndConcatenatedSubredditNames;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.Paging3LoadingStateAdapter;
import ml.docilealligator.infinityforreddit.adapters.PostRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentHistoryPostBinding;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.NeedForPostListFromPostFragmentEvent;
import ml.docilealligator.infinityforreddit.events.ProvidePostListToViewPostDetailActivityEvent;
import ml.docilealligator.infinityforreddit.post.HistoryPostPagingSource;
import ml.docilealligator.infinityforreddit.post.HistoryPostViewModel;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;
import retrofit2.Retrofit;

public class HistoryPostFragment extends PostFragmentBase implements FragmentCommunicator {

    public static final String EXTRA_HISTORY_TYPE = "EHT";
    public static final String EXTRA_FILTER = "EF";
    public static final int HISTORY_TYPE_READ_POSTS = 1;

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String RECYCLER_VIEW_POSITION_STATE = "RVPS";
    private static final String READ_POST_LIST_STATE = "RPLS";
    private static final String POST_FILTER_STATE = "PFS";
    private static final String POST_FRAGMENT_ID_STATE = "PFIS";

    HistoryPostViewModel mHistoryPostViewModel;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;
    @Inject
    Provider<StreamableAPI> mStreamableApiProvider;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("post_history")
    SharedPreferences mPostHistorySharedPreferences;
    @Inject
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences mPostFeedScrolledPositionSharedPreferences;
    @Inject
    ExoCreator mExoCreator;
    @Inject
    Executor mExecutor;
    private int postType;
    private PostRecyclerViewAdapter mAdapter;
    private int maxPosition = -1;
    private PostFilter postFilter;
    private int historyType;
    private FragmentHistoryPostBinding binding;

    public HistoryPostFragment() {
        // Required empty public constructor
    }

    public static HistoryPostFragment newInstance() {
        HistoryPostFragment fragment = new HistoryPostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryPostBinding.inflate(inflater, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        applyTheme();

        if (activity.isImmersiveInterface()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    getPostRecyclerView().setPadding(
                            0, 0, 0, allInsets.bottom
                    );
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        binding.recyclerViewHistoryPostFragment.addOnWindowFocusChangedListener(this::onWindowFocusChanged);

        Resources resources = getResources();

        /*if (activity.isImmersiveInterface()) {
            binding.recyclerViewHistoryPostFragment.setPadding(0, 0, 0, activity.getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewHistoryPostFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }*/

        binding.swipeRefreshLayoutHistoryPostFragment.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayoutHistoryPostFragment.setOnRefreshListener(this::refresh);

        int recyclerViewPosition = 0;
        if (savedInstanceState != null) {
            recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_STATE);

            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            postFilter = savedInstanceState.getParcelable(POST_FILTER_STATE);
            postFragmentId = savedInstanceState.getLong(POST_FRAGMENT_ID_STATE);
        } else {
            postFilter = getArguments().getParcelable(EXTRA_FILTER);
            postFragmentId = System.currentTimeMillis() + new Random().nextInt(1000);
        }

        if (activity instanceof RecyclerViewContentScrollingInterface) {
            binding.recyclerViewHistoryPostFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((RecyclerViewContentScrollingInterface) activity).contentScrollDown();
                    } else if (dy < 0) {
                        ((RecyclerViewContentScrollingInterface) activity).contentScrollUp();
                    }
                }
            });
        }

        historyType = getArguments().getInt(EXTRA_HISTORY_TYPE, HISTORY_TYPE_READ_POSTS);
        int defaultPostLayout = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY, "0"));
        Locale locale = getResources().getConfiguration().locale;

        if (historyType == HISTORY_TYPE_READ_POSTS) {
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.HISTORY_POST_LAYOUT_READ_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences,
                    null, mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    /*Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_FILTER, filter);
                    startActivity(intent);*/
                }

                @Override
                public void flairChipClicked(String flair) {
                    /*Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);*/
                }

                @Override
                public void nsfwChipClicked() {
                    /*Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);*/
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewHistoryPostFragment, new AutoTransition());
                }
            });
        }

        int nColumns = getNColumns(resources);
        if (nColumns == 1) {
            mLinearLayoutManager = new LinearLayoutManagerBugFixed(activity);
            binding.recyclerViewHistoryPostFragment.setLayoutManager(mLinearLayoutManager);
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(nColumns, StaggeredGridLayoutManager.VERTICAL);
            binding.recyclerViewHistoryPostFragment.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset, nColumns);
            binding.recyclerViewHistoryPostFragment.addItemDecoration(itemDecoration);
        }

        if (recyclerViewPosition > 0) {
            binding.recyclerViewHistoryPostFragment.scrollToPosition(recyclerViewPosition);
        }

        if (postFilter == null) {
            FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilter(mRedditDataRoomDatabase, mExecutor,
                    new Handler(), PostFilterUsage.HISTORY_TYPE, PostFilterUsage.HISTORY_TYPE_USAGE_READ_POSTS, (postFilter) -> {
                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                            this.postFilter = postFilter;
                            postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(activity.accountName + SharedPreferencesUtils.NSFW_BASE, false);
                            initializeAndBindPostViewModel();
                        }
                    });
        } else {
            initializeAndBindPostViewModel();
        }

        if (nColumns == 1 && mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false)) {
            swipeActionEnabled = true;
            touchHelper.attachToRecyclerView(binding.recyclerViewHistoryPostFragment, 1);
        }
        binding.recyclerViewHistoryPostFragment.setAdapter(mAdapter);
        binding.recyclerViewHistoryPostFragment.setCacheManager(mAdapter);
        binding.recyclerViewHistoryPostFragment.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(true, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.setCanStartActivity(true);
        }
        if (isInLazyMode) {
            resumeLazyMode(false);
        }
        if (mAdapter != null && binding.recyclerViewHistoryPostFragment != null) {
            binding.recyclerViewHistoryPostFragment.onWindowVisibilityChanged(View.VISIBLE);
        }
    }

    @Override
    protected boolean scrollPostsByCount(int count) {
        if (mLinearLayoutManager != null) {
            int pos = mLinearLayoutManager.findFirstVisibleItemPosition();
            int targetPosition = pos + count;
            mLinearLayoutManager.scrollToPositionWithOffset(targetPosition, 0);
            return true;
        } else {
            return false;
        }
    }

    private void initializeAndBindPostViewModel() {
        if (postType == HistoryPostPagingSource.TYPE_READ_POSTS) {
            mHistoryPostViewModel = new ViewModelProvider(HistoryPostFragment.this, new HistoryPostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, mRedditDataRoomDatabase, activity.accessToken,
                    activity.accountName, mSharedPreferences, HistoryPostPagingSource.TYPE_READ_POSTS, postFilter)).get(HistoryPostViewModel.class);
        } else {
            mHistoryPostViewModel = new ViewModelProvider(HistoryPostFragment.this, new HistoryPostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, mRedditDataRoomDatabase, activity.accessToken,
                    activity.accountName, mSharedPreferences, HistoryPostPagingSource.TYPE_READ_POSTS, postFilter)).get(HistoryPostViewModel.class);
        }

        bindPostViewModel();
    }

    private void bindPostViewModel() {
        mHistoryPostViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> mAdapter.submitData(getViewLifecycleOwner().getLifecycle(), posts));

        mAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();

            binding.swipeRefreshLayoutHistoryPostFragment.setRefreshing(refreshLoadState instanceof LoadState.Loading);
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && mAdapter.getItemCount() < 1) {
                    noPostFound();
                } else {
                    hasPost = true;
                }
            } else if (refreshLoadState instanceof LoadState.Error) {
                binding.fetchPostInfoLinearLayoutHistoryPostFragment.setOnClickListener(view -> refresh());
                showErrorView(R.string.load_posts_error);
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && mAdapter.getItemCount() < 1) {
                    noPostFound();
                }
            }
            return null;
        });

        binding.recyclerViewHistoryPostFragment.setAdapter(mAdapter.withLoadStateFooter(new Paging3LoadingStateAdapter(activity, mCustomThemeWrapper, R.string.load_more_posts_error,
                view -> mAdapter.retry())));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.history_post_fragment, menu);
        for (int i = 0; i < menu.size(); i++) {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, menu.getItem(i), null);
        }
        lazyModeItem = menu.findItem(R.id.action_lazy_mode_history_post_fragment);

        if (isInLazyMode) {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_stop_lazy_mode));
        } else {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_start_lazy_mode));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_lazy_mode_history_post_fragment) {
            if (isInLazyMode) {
                stopLazyMode();
            } else {
                startLazyMode();
            }
            return true;
        }
        return false;
    }

    private void noPostFound() {
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }

        binding.fetchPostInfoLinearLayoutHistoryPostFragment.setOnClickListener(null);
        showErrorView(R.string.no_posts);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        if (mLinearLayoutManager != null) {
            outState.putInt(RECYCLER_VIEW_POSITION_STATE, mLinearLayoutManager.findFirstVisibleItemPosition());
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[mStaggeredGridLayoutManager.getSpanCount()];
            outState.putInt(RECYCLER_VIEW_POSITION_STATE,
                    mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0]);
        }
        outState.putParcelable(POST_FILTER_STATE, postFilter);
        outState.putLong(POST_FRAGMENT_ID_STATE, postFragmentId);
    }

    @Override
    public void refresh() {
        binding.fetchPostInfoLinearLayoutHistoryPostFragment.setVisibility(View.GONE);
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }
        mAdapter.refresh();
        goBackToTop();
    }

    @Override
    protected void showErrorView(int stringResId) {
        if (activity != null && isAdded()) {
            binding.swipeRefreshLayoutHistoryPostFragment.setRefreshing(false);
            binding.fetchPostInfoLinearLayoutHistoryPostFragment.setVisibility(View.VISIBLE);
            binding.fetchPostInfoTextViewHistoryPostFragment.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(binding.fetchPostInfoImageViewHistoryPostFragment);
        }
    }

    @NonNull
    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return binding.swipeRefreshLayoutHistoryPostFragment;
    }

    @NonNull
    @Override
    protected RecyclerView getPostRecyclerView() {
        return binding.recyclerViewHistoryPostFragment;
    }

    @Nullable
    @Override
    protected PostRecyclerViewAdapter getPostAdapter() {
        return mAdapter;
    }

    @Override
    public void changePostLayout(int postLayout, boolean temporary) {
        this.postLayout = postLayout;
        if (!temporary) {
            switch (postType) {
                case HistoryPostPagingSource.TYPE_READ_POSTS:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.HISTORY_POST_LAYOUT_READ_POST, postLayout).apply();
            }
        }

        int previousPosition = -1;
        if (mLinearLayoutManager != null) {
            previousPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[mStaggeredGridLayoutManager.getSpanCount()];
            previousPosition = mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0];
        }
        int nColumns = getNColumns(getResources());
        if (nColumns == 1) {
            mLinearLayoutManager = new LinearLayoutManagerBugFixed(activity);
            if (binding.recyclerViewHistoryPostFragment.getItemDecorationCount() > 0) {
                binding.recyclerViewHistoryPostFragment.removeItemDecorationAt(0);
            }
            binding.recyclerViewHistoryPostFragment.setLayoutManager(mLinearLayoutManager);
            mStaggeredGridLayoutManager = null;
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(nColumns, StaggeredGridLayoutManager.VERTICAL);
            if (binding.recyclerViewHistoryPostFragment.getItemDecorationCount() > 0) {
                binding.recyclerViewHistoryPostFragment.removeItemDecorationAt(0);
            }
            binding.recyclerViewHistoryPostFragment.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset, nColumns);
            binding.recyclerViewHistoryPostFragment.addItemDecoration(itemDecoration);
            mLinearLayoutManager = null;
        }

        if (previousPosition > 0) {
            binding.recyclerViewHistoryPostFragment.scrollToPosition(previousPosition);
        }

        if (mAdapter != null) {
            mAdapter.setPostLayout(postLayout);
            refreshAdapter();
        }
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayoutHistoryPostFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutHistoryPostFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchPostInfoTextViewHistoryPostFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (activity.typeface != null) {
            binding.fetchPostInfoTextViewHistoryPostFragment.setTypeface(activity.typeface);
        }
    }

    @Override
    protected void refreshAdapter() {
        int previousPosition = -1;
        if (mLinearLayoutManager != null) {
            previousPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[mStaggeredGridLayoutManager.getSpanCount()];
            previousPosition = mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0];
        }

        RecyclerView.LayoutManager layoutManager = binding.recyclerViewHistoryPostFragment.getLayoutManager();
        binding.recyclerViewHistoryPostFragment.setAdapter(null);
        binding.recyclerViewHistoryPostFragment.setLayoutManager(null);
        binding.recyclerViewHistoryPostFragment.setAdapter(mAdapter);
        binding.recyclerViewHistoryPostFragment.setLayoutManager(layoutManager);
        if (previousPosition > 0) {
            binding.recyclerViewHistoryPostFragment.scrollToPosition(previousPosition);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
            if (isInLazyMode) {
                lazyModeRunnable.resetOldPosition();
            }
        } else if (mStaggeredGridLayoutManager != null) {
            mStaggeredGridLayoutManager.scrollToPositionWithOffset(0, 0);
            if (isInLazyMode) {
                lazyModeRunnable.resetOldPosition();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isInLazyMode) {
            pauseLazyMode(false);
        }
        if (mAdapter != null) {
            binding.recyclerViewHistoryPostFragment.onWindowVisibilityChanged(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        binding.recyclerViewHistoryPostFragment.addOnWindowFocusChangedListener(null);
        super.onDestroy();
    }

    private void onWindowFocusChanged(boolean hasWindowsFocus) {
        if (mAdapter != null) {
            mAdapter.setCanPlayVideo(hasWindowsFocus);
        }
    }

    @Subscribe
    public void onChangeDefaultPostLayoutEvent(ChangeDefaultPostLayoutEvent changeDefaultPostLayoutEvent) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            switch (postType) {
                case HistoryPostPagingSource.TYPE_READ_POSTS:
                    if (mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.HISTORY_POST_LAYOUT_READ_POST)) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
            }
        }
    }

    @Subscribe
    public void onNeedForPostListFromPostRecyclerViewAdapterEvent(NeedForPostListFromPostFragmentEvent event) {
        if (postFragmentId == event.postFragmentTimeId && mAdapter != null) {
            EventBus.getDefault().post(new ProvidePostListToViewPostDetailActivityEvent(postFragmentId,
                    new ArrayList<>(mAdapter.snapshot()), HistoryPostPagingSource.TYPE_READ_POSTS,
                    null, null, null, null,
                    null, null, null, postFilter, null, null));
        }
    }
}