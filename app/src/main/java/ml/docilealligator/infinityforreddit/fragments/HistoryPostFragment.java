package ml.docilealligator.infinityforreddit.fragments;

import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.INDEX_UNSET;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.TIME_UNSET;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ItemSnapshotList;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.FetchPostFilterReadPostsAndConcatenatedSubredditNames;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.HistoryPostRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.Paging3LoadingStateAdapter;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentHistoryPostBinding;
import ml.docilealligator.infinityforreddit.events.ChangeAutoplayNsfwVideosEvent;
import ml.docilealligator.infinityforreddit.events.ChangeCompactLayoutToolbarHiddenByDefaultEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDataSavingModeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultLinkPostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDisableImagePreviewEvent;
import ml.docilealligator.infinityforreddit.events.ChangeEasierToWatchInFullScreenEvent;
import ml.docilealligator.infinityforreddit.events.ChangeEnableSwipeActionSwitchEvent;
import ml.docilealligator.infinityforreddit.events.ChangeFixedHeightPreviewInCardEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHidePostFlairEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHidePostTypeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideSubredditAndUserPrefixEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTextPostContent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTheNumberOfCommentsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideTheNumberOfVotesEvent;
import ml.docilealligator.infinityforreddit.events.ChangeLongPressToHideToolbarInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeMuteAutoplayingVideosEvent;
import ml.docilealligator.infinityforreddit.events.ChangeMuteNSFWVideoEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ChangeOnlyDisablePreviewInVideoAndGifPostsEvent;
import ml.docilealligator.infinityforreddit.events.ChangePostFeedMaxResolutionEvent;
import ml.docilealligator.infinityforreddit.events.ChangePostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangePullToRefreshEvent;
import ml.docilealligator.infinityforreddit.events.ChangeRememberMutingOptionInPostFeedEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowAbsoluteNumberOfVotesEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowElapsedTimeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeStartAutoplayVisibleAreaOffsetEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionThresholdEvent;
import ml.docilealligator.infinityforreddit.events.ChangeTimeFormatEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVibrateWhenActionTriggeredEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVideoAutoplayEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVoteButtonsPositionEvent;
import ml.docilealligator.infinityforreddit.events.NeedForPostListFromPostFragmentEvent;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.events.ProvidePostListToViewPostDetailActivityEvent;
import ml.docilealligator.infinityforreddit.events.ShowDividerInCompactLayoutPreferenceEvent;
import ml.docilealligator.infinityforreddit.events.ShowThumbnailOnTheRightInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.post.HistoryPostPagingSource;
import ml.docilealligator.infinityforreddit.post.HistoryPostViewModel;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;
import retrofit2.Retrofit;

public class HistoryPostFragment extends Fragment implements FragmentCommunicator {

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
    @Named("default")
    SharedPreferences mSharedPreferences;
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
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    ExoCreator mExoCreator;
    @Inject
    Executor mExecutor;
    private RequestManager mGlide;
    private BaseActivity activity;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private MenuItem lazyModeItem;
    private long historyPostFragmentId;
    private int postType;
    private boolean isInLazyMode = false;
    private boolean isLazyModePaused = false;
    private boolean hasPost = false;
    private boolean rememberMutingOptionInPostFeed;
    private boolean swipeActionEnabled;
    private Boolean masterMutingOption;
    private HistoryPostRecyclerViewAdapter mAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private Window window;
    private Handler lazyModeHandler;
    private LazyModeRunnable lazyModeRunnable;
    private CountDownTimer resumeLazyModeCountDownTimer;
    private float lazyModeInterval;
    private int maxPosition = -1;
    private int postLayout;
    private PostFilter postFilter;
    private ColorDrawable backgroundSwipeRight;
    private ColorDrawable backgroundSwipeLeft;
    private Drawable drawableSwipeRight;
    private Drawable drawableSwipeLeft;
    private int swipeLeftAction;
    private int swipeRightAction;
    private boolean vibrateWhenActionTriggered;
    private float swipeActionThreshold;
    private ItemTouchHelper touchHelper;
    private ArrayList<String> readPosts;
    private final Map<String, String> subredditOrUserIcons = new HashMap<>();
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

        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);

        applyTheme();

        binding.recyclerViewHistoryPostFragment.addOnWindowFocusChangedListener(this::onWindowFocusChanged);

        lazyModeHandler = new Handler();

        lazyModeInterval = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.LAZY_MODE_INTERVAL_KEY, "2.5"));

        smoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        window = activity.getWindow();

        Resources resources = getResources();

        if ((activity != null && activity.isImmersiveInterface())) {
            binding.recyclerViewHistoryPostFragment.setPadding(0, 0, 0, ((BaseActivity) activity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewHistoryPostFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mGlide = Glide.with(activity);

        lazyModeRunnable = new LazyModeRunnable() {

            @Override
            public void run() {
                if (isInLazyMode && !isLazyModePaused && mAdapter != null) {
                    int nPosts = mAdapter.getItemCount();
                    if (getCurrentPosition() == -1) {
                        if (mLinearLayoutManager != null) {
                            setCurrentPosition(mLinearLayoutManager.findFirstVisibleItemPosition());
                        } else {
                            int[] into = new int[2];
                            setCurrentPosition(mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[1]);
                        }
                    }

                    if (getCurrentPosition() != RecyclerView.NO_POSITION && nPosts > getCurrentPosition()) {
                        incrementCurrentPosition();
                        smoothScroller.setTargetPosition(getCurrentPosition());
                        if (mLinearLayoutManager != null) {
                            mLinearLayoutManager.startSmoothScroll(smoothScroller);
                        } else {
                            mStaggeredGridLayoutManager.startSmoothScroll(smoothScroller);
                        }
                    }
                }
                lazyModeHandler.postDelayed(this, (long) (lazyModeInterval * 1000));
            }
        };

        resumeLazyModeCountDownTimer = new CountDownTimer((long) (lazyModeInterval * 1000), (long) (lazyModeInterval * 1000)) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                resumeLazyMode(true);
            }
        };

        binding.swipeRefreshLayoutHistoryPostFragment.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayoutHistoryPostFragment.setOnRefreshListener(this::refresh);

        int recyclerViewPosition = 0;
        if (savedInstanceState != null) {
            recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_STATE);

            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            readPosts = savedInstanceState.getStringArrayList(READ_POST_LIST_STATE);
            postFilter = savedInstanceState.getParcelable(POST_FILTER_STATE);
            historyPostFragmentId = savedInstanceState.getLong(POST_FRAGMENT_ID_STATE);
        } else {
            postFilter = getArguments().getParcelable(EXTRA_FILTER);
            historyPostFragmentId = System.currentTimeMillis() + new Random().nextInt(1000);
        }

        binding.recyclerViewHistoryPostFragment.setOnTouchListener((view, motionEvent) -> {
            if (isInLazyMode) {
                pauseLazyMode(true);
            }
            return false;
        });

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
        rememberMutingOptionInPostFeed = mSharedPreferences.getBoolean(SharedPreferencesUtils.REMEMBER_MUTING_OPTION_IN_POST_FEED, false);
        Locale locale = getResources().getConfiguration().locale;

        if (historyType == HISTORY_TYPE_READ_POSTS) {
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.HISTORY_POST_LAYOUT_READ_POST, defaultPostLayout);

            mAdapter = new HistoryPostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences,
                    mExoCreator, new HistoryPostRecyclerViewAdapter.Callback() {
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
            FetchPostFilterReadPostsAndConcatenatedSubredditNames.fetchPostFilterAndReadPosts(mRedditDataRoomDatabase, mExecutor,
                    new Handler(), activity.accountName, PostFilterUsage.HISTORY_TYPE, PostFilterUsage.HISTORY_TYPE_USAGE_READ_POSTS, (postFilter, readPostList) -> {
                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                            this.postFilter = postFilter;
                            postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(activity.accountName + SharedPreferencesUtils.NSFW_BASE, false);
                            initializeAndBindPostViewModel();
                        }
                    });
        } else {
            initializeAndBindPostViewModel();
        }

        vibrateWhenActionTriggered = mSharedPreferences.getBoolean(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED, true);
        swipeActionThreshold = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD, "0.3"));
        swipeRightAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_RIGHT_ACTION, "1"));
        swipeLeftAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_LEFT_ACTION, "0"));
        initializeSwipeActionDrawable();

        touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            boolean exceedThreshold = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof HistoryPostRecyclerViewAdapter.PostBaseViewHolder) &&
                        !(viewHolder instanceof HistoryPostRecyclerViewAdapter.PostCompactBaseViewHolder)) {
                    return makeMovementFlags(0, 0);
                } else if (viewHolder instanceof HistoryPostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) {
                    if (((HistoryPostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) viewHolder).isSwipeLocked()) {
                        return makeMovementFlags(0, 0);
                    }
                }
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (touchHelper != null) {
                    exceedThreshold = false;
                    touchHelper.attachToRecyclerView(null);
                    touchHelper.attachToRecyclerView(binding.recyclerViewHistoryPostFragment);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (isCurrentlyActive) {
                    View itemView = viewHolder.itemView;
                    int horizontalOffset = (int) Utils.convertDpToPixel(16, activity);
                    if (dX > 0) {
                        if (dX > (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold) {
                            if (!exceedThreshold) {
                                exceedThreshold = true;
                                if (vibrateWhenActionTriggered) {
                                    viewHolder.itemView.setHapticFeedbackEnabled(true);
                                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                }
                            }
                            backgroundSwipeRight.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        } else {
                            exceedThreshold = false;
                            backgroundSwipeRight.setBounds(0, 0, 0, 0);
                        }

                        drawableSwipeRight.setBounds(itemView.getLeft() + ((int) dX) - horizontalOffset - drawableSwipeRight.getIntrinsicWidth(),
                                (itemView.getBottom() + itemView.getTop() - drawableSwipeRight.getIntrinsicHeight()) / 2,
                                itemView.getLeft() + ((int) dX) - horizontalOffset,
                                (itemView.getBottom() + itemView.getTop() + drawableSwipeRight.getIntrinsicHeight()) / 2);
                        backgroundSwipeRight.draw(c);
                        drawableSwipeRight.draw(c);
                    } else if (dX < 0) {
                        if (-dX > (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold) {
                            if (!exceedThreshold) {
                                exceedThreshold = true;
                                if (vibrateWhenActionTriggered) {
                                    viewHolder.itemView.setHapticFeedbackEnabled(true);
                                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                }
                            }
                            backgroundSwipeLeft.setBounds(0, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        } else {
                            exceedThreshold = false;
                            backgroundSwipeLeft.setBounds(0, 0, 0, 0);
                        }
                        drawableSwipeLeft.setBounds(itemView.getRight() + ((int) dX) + horizontalOffset,
                                (itemView.getBottom() + itemView.getTop() - drawableSwipeLeft.getIntrinsicHeight()) / 2,
                                itemView.getRight() + ((int) dX) + horizontalOffset + drawableSwipeLeft.getIntrinsicWidth(),
                                (itemView.getBottom() + itemView.getTop() + drawableSwipeLeft.getIntrinsicHeight()) / 2);
                        backgroundSwipeLeft.draw(c);
                        drawableSwipeLeft.draw(c);
                    }
                } else {
                    if (exceedThreshold) {
                        mAdapter.onItemSwipe(viewHolder, dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START, swipeLeftAction, swipeRightAction);
                        exceedThreshold = false;
                    }
                }
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1;
            }
        });

        if (nColumns == 1 && mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false)) {
            swipeActionEnabled = true;
            touchHelper.attachToRecyclerView(binding.recyclerViewHistoryPostFragment);
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

    private boolean scrollPostsByCount(int count) {
        if (mLinearLayoutManager != null) {
            int pos = mLinearLayoutManager.findFirstVisibleItemPosition();
            int targetPosition = pos + count;
            mLinearLayoutManager.scrollToPositionWithOffset(targetPosition, 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        boolean volumeKeysNavigatePosts = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOLUME_KEYS_NAVIGATE_POSTS, false);
        if (volumeKeysNavigatePosts) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return scrollPostsByCount(-1);
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return scrollPostsByCount(1);
            }
        }
        return false;
    }

    private int getNColumns(Resources resources) {
        final boolean foldEnabled = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_FOLD_SUPPORT, false);
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (postLayout) {
                case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT_CARD_LAYOUT_2, "1"));
                case SharedPreferencesUtils.POST_LAYOUT_COMPACT:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT_COMPACT_LAYOUT, "1"));
                case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT_GALLERY_LAYOUT, "2"));
                default:
                    if (getResources().getBoolean(R.bool.isTablet)) {
                        if (foldEnabled) {
                            return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT_UNFOLDED, "2"));
                        } else {
                            return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT, "2"));
                        }
                    }
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_PORTRAIT, "1"));
            }
        } else {
            switch (postLayout) {
                case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_LANDSCAPE_CARD_LAYOUT_2, "2"));
                case SharedPreferencesUtils.POST_LAYOUT_COMPACT:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_LANDSCAPE_COMPACT_LAYOUT, "2"));
                case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_LANDSCAPE_GALLERY_LAYOUT, "2"));
                default:
                    if (getResources().getBoolean(R.bool.isTablet) && foldEnabled) {
                        return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_LANDSCAPE_UNFOLDED, "2"));
                    }
                    return Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.NUMBER_OF_COLUMNS_IN_POST_FEED_LANDSCAPE, "2"));
            }
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

    private void noPostFound() {
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }

        binding.fetchPostInfoLinearLayoutHistoryPostFragment.setOnClickListener(null);
        showErrorView(R.string.no_posts);
    }

    private void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        }
    }

    public long getHistoryPostFragmentId() {
        return historyPostFragmentId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putStringArrayList(READ_POST_LIST_STATE, readPosts);
        if (mLinearLayoutManager != null) {
            outState.putInt(RECYCLER_VIEW_POSITION_STATE, mLinearLayoutManager.findFirstVisibleItemPosition());
        } else if (mStaggeredGridLayoutManager != null) {
            int[] into = new int[mStaggeredGridLayoutManager.getSpanCount()];
            outState.putInt(RECYCLER_VIEW_POSITION_STATE,
                    mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0]);
        }
        outState.putParcelable(POST_FILTER_STATE, postFilter);
        outState.putLong(POST_FRAGMENT_ID_STATE, historyPostFragmentId);
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

    private void showErrorView(int stringResId) {
        if (activity != null && isAdded()) {
            binding.swipeRefreshLayoutHistoryPostFragment.setRefreshing(false);
            binding.fetchPostInfoLinearLayoutHistoryPostFragment.setVisibility(View.VISIBLE);
            binding.fetchPostInfoTextViewHistoryPostFragment.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(binding.fetchPostInfoImageViewHistoryPostFragment);
        }
    }

    @Override
    public boolean startLazyMode() {
        if (!hasPost) {
            Toast.makeText(activity, R.string.no_posts_no_lazy_mode, Toast.LENGTH_SHORT).show();
            return false;
        }

        Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_stop_lazy_mode));

        if (mAdapter != null && mAdapter.isAutoplay()) {
            mAdapter.setAutoplay(false);
            refreshAdapter();
        }

        isInLazyMode = true;
        isLazyModePaused = false;

        lazyModeInterval = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.LAZY_MODE_INTERVAL_KEY, "2.5"));
        lazyModeHandler.postDelayed(lazyModeRunnable, (long) (lazyModeInterval * 1000));
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_start, lazyModeInterval),
                Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void stopLazyMode() {
        Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_start_lazy_mode));
        if (mAdapter != null) {
            String autoplayString = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON) ||
                    (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI) && Utils.isConnectedToWifi(activity))) {
                mAdapter.setAutoplay(true);
                refreshAdapter();
            }
        }
        isInLazyMode = false;
        isLazyModePaused = false;
        lazyModeRunnable.resetOldPosition();
        lazyModeHandler.removeCallbacks(lazyModeRunnable);
        resumeLazyModeCountDownTimer.cancel();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_stop), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeLazyMode(boolean resumeNow) {
        if (isInLazyMode) {
            if (mAdapter != null && mAdapter.isAutoplay()) {
                mAdapter.setAutoplay(false);
                refreshAdapter();
            }
            isLazyModePaused = false;
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            lazyModeRunnable.resetOldPosition();

            if (resumeNow) {
                lazyModeHandler.post(lazyModeRunnable);
            } else {
                lazyModeHandler.postDelayed(lazyModeRunnable, (long) (lazyModeInterval * 1000));
            }
        }
    }

    @Override
    public void pauseLazyMode(boolean startTimer) {
        resumeLazyModeCountDownTimer.cancel();
        isInLazyMode = true;
        isLazyModePaused = true;
        lazyModeHandler.removeCallbacks(lazyModeRunnable);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (startTimer) {
            resumeLazyModeCountDownTimer.start();
        }
    }

    @Override
    public boolean isInLazyMode() {
        return isInLazyMode;
    }

    @Override
    public void changePostLayout(int postLayout) {
        changePostLayout(postLayout, false);
    }

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

    @Nullable
    public Boolean getMasterMutingOption() {
        return masterMutingOption;
    }

    public void videoAutoplayChangeMutingOption(boolean isMute) {
        if (rememberMutingOptionInPostFeed) {
            masterMutingOption = isMute;
        }
    }

    public void loadIcon(String subredditOrUserName, boolean isSubreddit, PostFragment.LoadIconListener loadIconListener) {
        if (subredditOrUserIcons.containsKey(subredditOrUserName)) {
            loadIconListener.loadIconSuccess(subredditOrUserName, subredditOrUserIcons.get(subredditOrUserName));
        } else {
            if (isSubreddit) {
                LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        subredditOrUserName, activity.accessToken, activity.accountName, mOauthRetrofit, mRetrofit,
                        iconImageUrl -> {
                            subredditOrUserIcons.put(subredditOrUserName, iconImageUrl);
                            loadIconListener.loadIconSuccess(subredditOrUserName, iconImageUrl);
                        });
            } else {
                LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditOrUserName,
                        mRetrofit, iconImageUrl -> {
                            subredditOrUserIcons.put(subredditOrUserName, iconImageUrl);
                            loadIconListener.loadIconSuccess(subredditOrUserName, iconImageUrl);
                        });
            }
        }
    }

    private void refreshAdapter() {
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
        EventBus.getDefault().unregister(this);
        binding.recyclerViewHistoryPostFragment.addOnWindowFocusChangedListener(null);
        super.onDestroy();
    }

    private void onWindowFocusChanged(boolean hasWindowsFocus) {
        if (mAdapter != null) {
            mAdapter.setCanPlayVideo(hasWindowsFocus);
        }
    }

    public boolean isRecyclerViewItemSwipeable(RecyclerView.ViewHolder viewHolder) {
        if (swipeActionEnabled) {
            if (viewHolder instanceof HistoryPostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) {
                return !((HistoryPostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) viewHolder).isSwipeLocked();
            }

            return true;
        }

        return false;
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostList event) {
        ItemSnapshotList<Post> posts = mAdapter.snapshot();
        if (event.positionInList >= 0 && event.positionInList < posts.size()) {
            Post post = posts.get(event.positionInList);
            if (post != null && post.getFullName().equals(event.post.getFullName())) {
                post.setTitle(event.post.getTitle());
                post.setVoteType(event.post.getVoteType());
                post.setScore(event.post.getScore());
                post.setNComments(event.post.getNComments());
                post.setNSFW(event.post.isNSFW());
                post.setHidden(event.post.isHidden());
                post.setSpoiler(event.post.isSpoiler());
                post.setFlair(event.post.getFlair());
                post.setSaved(event.post.isSaved());
                if (event.post.isRead()) {
                    post.markAsRead();
                }
                mAdapter.notifyItemChanged(event.positionInList);
            }
        }
    }

    @Subscribe
    public void onChangeShowElapsedTimeEvent(ChangeShowElapsedTimeEvent event) {
        if (mAdapter != null) {
            mAdapter.setShowElapsedTime(event.showElapsedTime);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeTimeFormatEvent(ChangeTimeFormatEvent changeTimeFormatEvent) {
        if (mAdapter != null) {
            mAdapter.setTimeFormat(changeTimeFormatEvent.timeFormat);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVoteButtonsPositionEvent(ChangeVoteButtonsPositionEvent event) {
        if (mAdapter != null) {
            mAdapter.setVoteButtonsPosition(event.voteButtonsOnTheRight);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeNSFWBlurEvent(ChangeNSFWBlurEvent event) {
        if (mAdapter != null) {
            mAdapter.setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(event.needBlurNSFW);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeSpoilerBlurEvent(ChangeSpoilerBlurEvent event) {
        if (mAdapter != null) {
            mAdapter.setBlurSpoiler(event.needBlurSpoiler);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangePostLayoutEvent(ChangePostLayoutEvent event) {
        changePostLayout(event.postLayout);
    }

    @Subscribe
    public void onShowDividerInCompactLayoutPreferenceEvent(ShowDividerInCompactLayoutPreferenceEvent event) {
        if (mAdapter != null) {
            mAdapter.setShowDividerInCompactLayout(event.showDividerInCompactLayout);
            refreshAdapter();
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
    public void onChangeDefaultLinkPostLayoutEvent(ChangeDefaultLinkPostLayoutEvent event) {
        if (mAdapter != null) {
            mAdapter.setDefaultLinkPostLayout(event.defaultLinkPostLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeShowAbsoluteNumberOfVotesEvent(ChangeShowAbsoluteNumberOfVotesEvent changeShowAbsoluteNumberOfVotesEvent) {
        if (mAdapter != null) {
            mAdapter.setShowAbsoluteNumberOfVotes(changeShowAbsoluteNumberOfVotesEvent.showAbsoluteNumberOfVotes);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVideoAutoplayEvent(ChangeVideoAutoplayEvent changeVideoAutoplayEvent) {
        if (mAdapter != null) {
            boolean autoplay = false;
            if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
                autoplay = true;
            } else if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                autoplay = Utils.isConnectedToWifi(activity);
            }
            mAdapter.setAutoplay(autoplay);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeAutoplayNsfwVideosEvent(ChangeAutoplayNsfwVideosEvent changeAutoplayNsfwVideosEvent) {
        if (mAdapter != null) {
            mAdapter.setAutoplayNsfwVideos(changeAutoplayNsfwVideosEvent.autoplayNsfwVideos);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteAutoplayingVideosEvent(ChangeMuteAutoplayingVideosEvent changeMuteAutoplayingVideosEvent) {
        if (mAdapter != null) {
            mAdapter.setMuteAutoplayingVideos(changeMuteAutoplayingVideosEvent.muteAutoplayingVideos);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        if (mAdapter != null) {
            String autoplay = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            boolean stateChanged = false;
            if (autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                mAdapter.setAutoplay(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_WIFI);
                stateChanged = true;
            }
            if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                mAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
                stateChanged = true;
            }

            if (stateChanged) {
                refreshAdapter();
            }
        }
    }

    @Subscribe
    public void onShowThumbnailOnTheRightInCompactLayoutEvent(ShowThumbnailOnTheRightInCompactLayoutEvent showThumbnailOnTheRightInCompactLayoutEvent) {
        if (mAdapter != null) {
            mAdapter.setShowThumbnailOnTheRightInCompactLayout(showThumbnailOnTheRightInCompactLayoutEvent.showThumbnailOnTheRightInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeStartAutoplayVisibleAreaOffsetEvent(ChangeStartAutoplayVisibleAreaOffsetEvent changeStartAutoplayVisibleAreaOffsetEvent) {
        if (mAdapter != null) {
            mAdapter.setStartAutoplayVisibleAreaOffset(changeStartAutoplayVisibleAreaOffsetEvent.startAutoplayVisibleAreaOffset);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteNSFWVideoEvent(ChangeMuteNSFWVideoEvent changeMuteNSFWVideoEvent) {
        if (mAdapter != null) {
            mAdapter.setMuteNSFWVideo(changeMuteNSFWVideoEvent.muteNSFWVideo);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVibrateWhenActionTriggeredEvent(ChangeVibrateWhenActionTriggeredEvent changeVibrateWhenActionTriggeredEvent) {
        vibrateWhenActionTriggered = changeVibrateWhenActionTriggeredEvent.vibrateWhenActionTriggered;
    }

    @Subscribe
    public void onChangeEnableSwipeActionSwitchEvent(ChangeEnableSwipeActionSwitchEvent changeEnableSwipeActionSwitchEvent) {
        if (getNColumns(getResources()) == 1 && touchHelper != null) {
            swipeActionEnabled = changeEnableSwipeActionSwitchEvent.enableSwipeAction;
            if (changeEnableSwipeActionSwitchEvent.enableSwipeAction) {
                touchHelper.attachToRecyclerView(binding.recyclerViewHistoryPostFragment);
            } else {
                touchHelper.attachToRecyclerView(null);
            }
        }
    }

    @Subscribe
    public void onChangePullToRefreshEvent(ChangePullToRefreshEvent changePullToRefreshEvent) {
        binding.swipeRefreshLayoutHistoryPostFragment.setEnabled(changePullToRefreshEvent.pullToRefresh);
    }

    @Subscribe
    public void onChangeLongPressToHideToolbarInCompactLayoutEvent(ChangeLongPressToHideToolbarInCompactLayoutEvent changeLongPressToHideToolbarInCompactLayoutEvent) {
        if (mAdapter != null) {
            mAdapter.setLongPressToHideToolbarInCompactLayout(changeLongPressToHideToolbarInCompactLayoutEvent.longPressToHideToolbarInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeCompactLayoutToolbarHiddenByDefaultEvent(ChangeCompactLayoutToolbarHiddenByDefaultEvent changeCompactLayoutToolbarHiddenByDefaultEvent) {
        if (mAdapter != null) {
            mAdapter.setCompactLayoutToolbarHiddenByDefault(changeCompactLayoutToolbarHiddenByDefaultEvent.compactLayoutToolbarHiddenByDefault);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeSwipeActionThresholdEvent(ChangeSwipeActionThresholdEvent changeSwipeActionThresholdEvent) {
        swipeActionThreshold = changeSwipeActionThresholdEvent.swipeActionThreshold;
    }

    @Subscribe
    public void onChangeDataSavingModeEvent(ChangeDataSavingModeEvent changeDataSavingModeEvent) {
        if (mAdapter != null) {
            boolean dataSavingMode = false;
            if (changeDataSavingModeEvent.dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                dataSavingMode = Utils.isConnectedToCellularData(activity);
            } else if (changeDataSavingModeEvent.dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
                dataSavingMode = true;
            }
            mAdapter.setDataSavingMode(dataSavingMode);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeDisableImagePreviewEvent(ChangeDisableImagePreviewEvent changeDisableImagePreviewEvent) {
        if (mAdapter != null) {
            mAdapter.setDisableImagePreview(changeDisableImagePreviewEvent.disableImagePreview);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeOnlyDisablePreviewInVideoAndGifPostsEvent(ChangeOnlyDisablePreviewInVideoAndGifPostsEvent changeOnlyDisablePreviewInVideoAndGifPostsEvent) {
        if (mAdapter != null) {
            mAdapter.setOnlyDisablePreviewInVideoPosts(changeOnlyDisablePreviewInVideoAndGifPostsEvent.onlyDisablePreviewInVideoAndGifPosts);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeSwipeActionEvent(ChangeSwipeActionEvent changeSwipeActionEvent) {
        swipeRightAction = changeSwipeActionEvent.swipeRightAction == -1 ? swipeRightAction : changeSwipeActionEvent.swipeRightAction;
        swipeLeftAction = changeSwipeActionEvent.swipeLeftAction == -1 ? swipeLeftAction : changeSwipeActionEvent.swipeLeftAction;
        initializeSwipeActionDrawable();
    }

    @Subscribe
    public void onNeedForPostListFromPostRecyclerViewAdapterEvent(NeedForPostListFromPostFragmentEvent event) {
        if (historyPostFragmentId == event.postFragmentTimeId && mAdapter != null) {
            EventBus.getDefault().post(new ProvidePostListToViewPostDetailActivityEvent(historyPostFragmentId,
                    new ArrayList<>(mAdapter.snapshot()), HistoryPostPagingSource.TYPE_READ_POSTS,
                    null, null, null, null,
                    null, null, null, postFilter, null, null));
        }
    }

    @Subscribe
    public void onChangeHidePostTypeEvent(ChangeHidePostTypeEvent event) {
        if (mAdapter != null) {
            mAdapter.setHidePostType(event.hidePostType);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHidePostFlairEvent(ChangeHidePostFlairEvent event) {
        if (mAdapter != null) {
            mAdapter.setHidePostFlair(event.hidePostFlair);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideSubredditAndUserEvent(ChangeHideSubredditAndUserPrefixEvent event) {
        if (mAdapter != null) {
            mAdapter.setHideSubredditAndUserPrefix(event.hideSubredditAndUserPrefix);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTheNumberOfVotesEvent(ChangeHideTheNumberOfVotesEvent event) {
        if (mAdapter != null) {
            mAdapter.setHideTheNumberOfVotes(event.hideTheNumberOfVotes);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTheNumberOfCommentsEvent(ChangeHideTheNumberOfCommentsEvent event) {
        if (mAdapter != null) {
            mAdapter.setHideTheNumberOfComments(event.hideTheNumberOfComments);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeRememberMutingOptionInPostFeedEvent(ChangeRememberMutingOptionInPostFeedEvent event) {
        rememberMutingOptionInPostFeed = event.rememberMutingOptionInPostFeedEvent;
        if (!event.rememberMutingOptionInPostFeedEvent) {
            masterMutingOption = null;
        }
    }

    @Subscribe
    public void onChangeFixedHeightPreviewCardEvent(ChangeFixedHeightPreviewInCardEvent event) {
        if (mAdapter != null) {
            mAdapter.setFixedHeightPreviewInCard(event.fixedHeightPreviewInCard);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTextPostContentEvent(ChangeHideTextPostContent event) {
        if (mAdapter != null) {
            mAdapter.setHideTextPostContent(event.hideTextPostContent);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangePostFeedMaxResolutionEvent(ChangePostFeedMaxResolutionEvent event) {
        if (mAdapter != null) {
            mAdapter.setPostFeedMaxResolution(event.postFeedMaxResolution);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeEasierToWatchInFullScreenEvent(ChangeEasierToWatchInFullScreenEvent event) {
        if (mAdapter != null) {
            mAdapter.setEasierToWatchInFullScreen(event.easierToWatchInFullScreen);
        }
    }

    private static abstract class LazyModeRunnable implements Runnable {
        private int currentPosition = -1;

        int getCurrentPosition() {
            return currentPosition;
        }

        void setCurrentPosition(int currentPosition) {
            this.currentPosition = currentPosition;
        }

        void incrementCurrentPosition() {
            currentPosition++;
        }

        void resetOldPosition() {
            currentPosition = -1;
        }
    }

    private static class StaggeredGridLayoutManagerItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private final int mItemOffset;
        private final int mNColumns;

        StaggeredGridLayoutManagerItemOffsetDecoration(int itemOffset, int nColumns) {
            mItemOffset = itemOffset;
            mNColumns = nColumns;
        }

        StaggeredGridLayoutManagerItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId, int nColumns) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId), nColumns);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            int spanIndex = layoutParams.getSpanIndex();

            int halfOffset = mItemOffset / 2;

            if (mNColumns == 2) {
                if (spanIndex == 0) {
                    outRect.set(halfOffset, 0, halfOffset / 2, 0);
                } else {
                    outRect.set(halfOffset / 2, 0, halfOffset, 0);
                }
            } else if (mNColumns == 3) {
                if (spanIndex == 0) {
                    outRect.set(halfOffset, 0, halfOffset / 2, 0);
                } else if (spanIndex == 1) {
                    outRect.set(halfOffset / 2, 0, halfOffset / 2, 0);
                } else {
                    outRect.set(halfOffset / 2, 0, halfOffset, 0);
                }
            }
        }
    }

    public interface LoadIconListener {
        void loadIconSuccess(String subredditOrUserName, String iconUrl);
    }
}