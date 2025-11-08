package ml.docilealligator.infinityforreddit.fragments;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.paging.ItemSnapshotList;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.PostRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AdjustableTouchSlopItemTouchHelper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.ChangeAutoplayNsfwVideosEvent;
import ml.docilealligator.infinityforreddit.events.ChangeCompactLayoutToolbarHiddenByDefaultEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDataSavingModeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultLinkPostLayoutEvent;
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
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.events.ShowDividerInCompactLayoutPreferenceEvent;
import ml.docilealligator.infinityforreddit.events.ShowThumbnailOnTheLeftInCompactLayoutEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesLiveDataKt;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public abstract class PostFragmentBase extends Fragment {

    @Inject
    @Named("no_oauth")
    protected Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    protected Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    protected SharedPreferences mSharedPreferences;
    @Inject
    protected RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    protected Executor mExecutor;
    protected BaseActivity activity;
    protected RequestManager mGlide;
    protected Window window;
    protected MenuItem lazyModeItem;
    protected LinearLayoutManagerBugFixed mLinearLayoutManager;
    protected StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    protected boolean hasPost;
    protected long postFragmentId;
    protected boolean rememberMutingOptionInPostFeed;
    protected Boolean masterMutingOption;
    protected Handler lazyModeHandler;
    protected CountDownTimer resumeLazyModeCountDownTimer;
    protected RecyclerView.SmoothScroller smoothScroller;
    protected LazyModeRunnable lazyModeRunnable;
    protected float lazyModeInterval;
    protected boolean isInLazyMode = false;
    protected boolean isLazyModePaused = false;
    protected int postLayout;
    protected boolean swipeActionEnabled;
    protected ColorDrawable backgroundSwipeRight;
    protected ColorDrawable backgroundSwipeLeft;
    protected Drawable drawableSwipeRight;
    protected Drawable drawableSwipeLeft;
    protected boolean vibrateWhenActionTriggered;
    protected float swipeActionThreshold;
    protected int swipeLeftAction;
    protected int swipeRightAction;
    protected AdjustableTouchSlopItemTouchHelper touchHelper;
    private boolean shouldSwipeBack;
    protected final Map<String, String> subredditOrUserIcons = new HashMap<>();

    public PostFragmentBase() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);

        window = activity.getWindow();

        rememberMutingOptionInPostFeed = mSharedPreferences.getBoolean(SharedPreferencesUtils.REMEMBER_MUTING_OPTION_IN_POST_FEED, false);

        smoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        lazyModeHandler = new Handler();
        lazyModeRunnable = new LazyModeRunnable() {
            @Override
            public void run() {
                if (isInLazyMode && !isLazyModePaused && getPostAdapter() != null) {
                    int nPosts = getPostAdapter().getItemCount();
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
        lazyModeInterval = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.LAZY_MODE_INTERVAL_KEY, "2.5"));
        resumeLazyModeCountDownTimer = new CountDownTimer((long) (lazyModeInterval * 1000), (long) (lazyModeInterval * 1000)) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                resumeLazyMode(true);
            }
        };

        mGlide = Glide.with(activity);

        vibrateWhenActionTriggered = mSharedPreferences.getBoolean(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED, true);
        swipeActionThreshold = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD, "0.3"));
        swipeRightAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_RIGHT_ACTION, "1"));
        swipeLeftAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_LEFT_ACTION, "0"));
        initializeSwipeActionDrawable();

        touchHelper = new AdjustableTouchSlopItemTouchHelper(new AdjustableTouchSlopItemTouchHelper.Callback() {
            boolean exceedThreshold = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ACTION_STATE_IDLE, calculateMovementFlags(recyclerView, viewHolder));
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                if (shouldSwipeBack) {
                    shouldSwipeBack = false;
                    return 0;
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int horizontalOffset = (int) Utils.convertDpToPixel(16, activity);
                if (dX > 0) {
                    if (dX > (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold) {
                        dX = (itemView.getRight() - itemView.getLeft()) * swipeActionThreshold;
                        if (!exceedThreshold && isCurrentlyActive) {
                            exceedThreshold = true;
                            if (vibrateWhenActionTriggered) {
                                itemView.setHapticFeedbackEnabled(true);
                                itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
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
                        dX = -(itemView.getRight() - itemView.getLeft()) * swipeActionThreshold;
                        if (!exceedThreshold && isCurrentlyActive) {
                            exceedThreshold = true;
                            if (vibrateWhenActionTriggered) {
                                itemView.setHapticFeedbackEnabled(true);
                                itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
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

                if (!isCurrentlyActive && exceedThreshold && getPostAdapter() != null) {
                    getPostAdapter().onItemSwipe(viewHolder, dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START, swipeLeftAction, swipeRightAction);
                    exceedThreshold = false;
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1;
            }
        });

        getPostRecyclerView().setOnTouchListener((view, motionEvent) -> {
            shouldSwipeBack = motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP;
            if (isInLazyMode) {
                pauseLazyMode(true);
            }
            return false;
        });

        SharedPreferencesLiveDataKt.stringLiveData(mSharedPreferences, SharedPreferencesUtils.LONG_PRESS_POST_NON_MEDIA_AREA, SharedPreferencesUtils.LONG_PRESS_POST_VALUE_SHOW_POST_OPTIONS).observe(getViewLifecycleOwner(), s -> {
            if (getPostAdapter() != null) {
                getPostAdapter().setLongPressPostNonMediaAreaAction(s);
            }
        });

        SharedPreferencesLiveDataKt.stringLiveData(mSharedPreferences, SharedPreferencesUtils.LONG_PRESS_POST_MEDIA, SharedPreferencesUtils.LONG_PRESS_POST_VALUE_SHOW_POST_OPTIONS).observe(getViewLifecycleOwner(), s -> {
            if (getPostAdapter() != null) {
                getPostAdapter().setLongPressPostMediaAction(s);
            }
        });

        SharedPreferencesLiveDataKt.stringLiveData(mSharedPreferences, SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION, "360").observe(getViewLifecycleOwner(), s -> {
            if (getPostAdapter() != null) {
                getPostAdapter().setDataSavingModeDefaultResolution(Integer.parseInt(s));
            }
        });

        SharedPreferencesLiveDataKt.stringLiveData(mSharedPreferences, SharedPreferencesUtils.REDDIT_VIDEO_DEFAULT_RESOLUTION_NO_DATA_SAVING, "0").observe(getViewLifecycleOwner(), s -> {
            if (getPostAdapter() != null) {
                getPostAdapter().setNonDataSavingModeDefaultResolution(Integer.parseInt(s));
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.requestApplyInsets(view);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }

    public final boolean handleKeyDown(int keyCode) {
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

    public final long getPostFragmentId() {
        return postFragmentId;
    }

    public boolean startLazyMode() {
        if (!hasPost) {
            Toast.makeText(activity, R.string.no_posts_no_lazy_mode, Toast.LENGTH_SHORT).show();
            return false;
        }

        Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_stop_lazy_mode));

        if (getPostAdapter() != null && getPostAdapter().isAutoplay()) {
            getPostAdapter().setAutoplay(false);
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

    public void stopLazyMode() {
        Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_start_lazy_mode));
        if (getPostAdapter() != null) {
            String autoplayString = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON) ||
                    (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI) && Utils.isConnectedToWifi(activity))) {
                getPostAdapter().setAutoplay(true);
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

    public void resumeLazyMode(boolean resumeNow) {
        if (isInLazyMode) {
            if (getPostAdapter() != null && getPostAdapter().isAutoplay()) {
                getPostAdapter().setAutoplay(false);
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

    public final boolean isInLazyMode() {
        return isInLazyMode;
    }

    protected abstract void refreshAdapter();

    protected final int getNColumns(Resources resources) {
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

    public final void changePostLayout(int postLayout) {
        changePostLayout(postLayout, false);
    }

    public abstract void changePostLayout(int postLayout, boolean temporary);

    public final Boolean getMasterMutingOption() {
        return masterMutingOption;
    }

    public final void videoAutoplayChangeMutingOption(boolean isMute) {
        if (rememberMutingOptionInPostFeed) {
            masterMutingOption = isMute;
        }
    }

    public boolean getIsNsfwSubreddit() {
        return false;
    }

    public boolean isRecyclerViewItemSwipeable(RecyclerView.ViewHolder viewHolder) {
        if (swipeActionEnabled) {
            if (viewHolder instanceof PostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) {
                return !((PostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) viewHolder).isSwipeLocked();
            }

            return true;
        }

        return false;
    }

    public final void loadIcon(String subredditOrUserName, boolean isSubreddit, LoadIconListener loadIconListener) {
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
                LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase, activity.accessToken,
                        subredditOrUserName, mOauthRetrofit, mRetrofit, iconImageUrl -> {
                            subredditOrUserIcons.put(subredditOrUserName, iconImageUrl);
                            loadIconListener.loadIconSuccess(subredditOrUserName, iconImageUrl);
                        });
            }
        }
    }

    protected abstract boolean scrollPostsByCount(int count);

    protected final void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        }
    }

    protected int calculateMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof PostRecyclerViewAdapter.PostBaseViewHolder) &&
                !(viewHolder instanceof PostRecyclerViewAdapter.PostCompactBaseViewHolder)) {
            return 0;
        } else if (viewHolder instanceof PostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) {
            if (((PostRecyclerViewAdapter.PostBaseGalleryTypeViewHolder) viewHolder).isSwipeLocked()) {
                return 0;
            }
        }
        return ItemTouchHelper.START | ItemTouchHelper.END;
    }

    protected abstract void showErrorView(int stringResId);

    @NonNull
    protected abstract SwipeRefreshLayout getSwipeRefreshLayout();

    @NonNull
    protected abstract RecyclerView getPostRecyclerView();

    @Nullable
    protected abstract PostRecyclerViewAdapter getPostAdapter();

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostList event) {
        if (getPostAdapter() == null) {
            return;
        }

        ItemSnapshotList<Post> posts = getPostAdapter().snapshot();
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
                post.setIsStickied(event.post.isStickied());
                post.setApproved(event.post.isApproved());
                post.setApprovedAtUTC(event.post.getApprovedAtUTC());
                post.setApprovedBy(event.post.getApprovedBy());
                post.setRemoved(event.post.isRemoved(), event.post.isSpam());
                post.setIsLocked(event.post.isLocked());
                post.setIsModerator(event.post.isModerator());
                if (event.post.isRead()) {
                    post.markAsRead();
                }
                getPostAdapter().notifyItemChanged(event.positionInList);
            }
        }
    }

    @Subscribe
    public void onChangeShowElapsedTimeEvent(ChangeShowElapsedTimeEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setShowElapsedTime(event.showElapsedTime);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeTimeFormatEvent(ChangeTimeFormatEvent changeTimeFormatEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setTimeFormat(changeTimeFormatEvent.timeFormat);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVoteButtonsPositionEvent(ChangeVoteButtonsPositionEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setVoteButtonsPosition(event.voteButtonsOnTheRight);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeNSFWBlurEvent(ChangeNSFWBlurEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(event.needBlurNSFW, event.doNotBlurNsfwInNsfwSubreddits);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeSpoilerBlurEvent(ChangeSpoilerBlurEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setBlurSpoiler(event.needBlurSpoiler);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangePostLayoutEvent(ChangePostLayoutEvent event) {
        changePostLayout(event.postLayout);
    }

    @Subscribe
    public void onShowDividerInCompactLayoutPreferenceEvent(ShowDividerInCompactLayoutPreferenceEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setShowDividerInCompactLayout(event.showDividerInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeDefaultLinkPostLayoutEvent(ChangeDefaultLinkPostLayoutEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setDefaultLinkPostLayout(event.defaultLinkPostLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeShowAbsoluteNumberOfVotesEvent(ChangeShowAbsoluteNumberOfVotesEvent changeShowAbsoluteNumberOfVotesEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setShowAbsoluteNumberOfVotes(changeShowAbsoluteNumberOfVotesEvent.showAbsoluteNumberOfVotes);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeVideoAutoplayEvent(ChangeVideoAutoplayEvent changeVideoAutoplayEvent) {
        if (getPostAdapter() != null) {
            boolean autoplay = false;
            if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
                autoplay = true;
            } else if (changeVideoAutoplayEvent.autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                autoplay = Utils.isConnectedToWifi(activity);
            }
            getPostAdapter().setAutoplay(autoplay);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeAutoplayNsfwVideosEvent(ChangeAutoplayNsfwVideosEvent changeAutoplayNsfwVideosEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setAutoplayNsfwVideos(changeAutoplayNsfwVideosEvent.autoplayNsfwVideos);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteAutoplayingVideosEvent(ChangeMuteAutoplayingVideosEvent changeMuteAutoplayingVideosEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setMuteAutoplayingVideos(changeMuteAutoplayingVideosEvent.muteAutoplayingVideos);
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
    public void onChangeSwipeActionEvent(ChangeSwipeActionEvent changeSwipeActionEvent) {
        swipeRightAction = changeSwipeActionEvent.swipeRightAction == -1 ? swipeRightAction : changeSwipeActionEvent.swipeRightAction;
        swipeLeftAction = changeSwipeActionEvent.swipeLeftAction == -1 ? swipeLeftAction : changeSwipeActionEvent.swipeLeftAction;
        initializeSwipeActionDrawable();
    }

    @Subscribe
    public void onChangeSwipeActionThresholdEvent(ChangeSwipeActionThresholdEvent changeSwipeActionThresholdEvent) {
        swipeActionThreshold = changeSwipeActionThresholdEvent.swipeActionThreshold;
    }

    @Subscribe
    public void onChangeVibrateWhenActionTriggeredEvent(ChangeVibrateWhenActionTriggeredEvent changeVibrateWhenActionTriggeredEvent) {
        vibrateWhenActionTriggered = changeVibrateWhenActionTriggeredEvent.vibrateWhenActionTriggered;
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        if (getPostAdapter() != null) {
            String autoplay = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            boolean stateChanged = false;
            if (autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                getPostAdapter().setAutoplay(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_WIFI);
                stateChanged = true;
            }
            if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                getPostAdapter().setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
                stateChanged = true;
            }

            if (stateChanged) {
                refreshAdapter();
            }
        }
    }

    @Subscribe
    public void onShowThumbnailOnTheLeftInCompactLayoutEvent(ShowThumbnailOnTheLeftInCompactLayoutEvent showThumbnailOnTheLeftInCompactLayoutEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setShowThumbnailOnTheLeftInCompactLayout(showThumbnailOnTheLeftInCompactLayoutEvent.showThumbnailOnTheLeftInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeStartAutoplayVisibleAreaOffsetEvent(ChangeStartAutoplayVisibleAreaOffsetEvent changeStartAutoplayVisibleAreaOffsetEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setStartAutoplayVisibleAreaOffset(changeStartAutoplayVisibleAreaOffsetEvent.startAutoplayVisibleAreaOffset);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeMuteNSFWVideoEvent(ChangeMuteNSFWVideoEvent changeMuteNSFWVideoEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setMuteNSFWVideo(changeMuteNSFWVideoEvent.muteNSFWVideo);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeEnableSwipeActionSwitchEvent(ChangeEnableSwipeActionSwitchEvent changeEnableSwipeActionSwitchEvent) {
        if (getNColumns(getResources()) == 1 && touchHelper != null) {
            swipeActionEnabled = changeEnableSwipeActionSwitchEvent.enableSwipeAction;
            if (changeEnableSwipeActionSwitchEvent.enableSwipeAction) {
                touchHelper.attachToRecyclerView(getPostRecyclerView(), 1);
            } else {
                touchHelper.attachToRecyclerView(null, 1);
            }
        }
    }

    @Subscribe
    public void onChangePullToRefreshEvent(ChangePullToRefreshEvent changePullToRefreshEvent) {
        getSwipeRefreshLayout().setEnabled(changePullToRefreshEvent.pullToRefresh);
    }

    @Subscribe
    public void onChangeLongPressToHideToolbarInCompactLayoutEvent(ChangeLongPressToHideToolbarInCompactLayoutEvent changeLongPressToHideToolbarInCompactLayoutEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setLongPressToHideToolbarInCompactLayout(changeLongPressToHideToolbarInCompactLayoutEvent.longPressToHideToolbarInCompactLayout);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeCompactLayoutToolbarHiddenByDefaultEvent(ChangeCompactLayoutToolbarHiddenByDefaultEvent changeCompactLayoutToolbarHiddenByDefaultEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setCompactLayoutToolbarHiddenByDefault(changeCompactLayoutToolbarHiddenByDefaultEvent.compactLayoutToolbarHiddenByDefault);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeDataSavingModeEvent(ChangeDataSavingModeEvent changeDataSavingModeEvent) {
        if (getPostAdapter() != null) {
            boolean dataSavingMode = false;
            if (changeDataSavingModeEvent.dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                dataSavingMode = Utils.isConnectedToCellularData(activity);
            } else if (changeDataSavingModeEvent.dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
                dataSavingMode = true;
            }
            getPostAdapter().setDataSavingMode(dataSavingMode);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeDisableImagePreviewEvent(ChangeDisableImagePreviewEvent changeDisableImagePreviewEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setDisableImagePreview(changeDisableImagePreviewEvent.disableImagePreview);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeOnlyDisablePreviewInVideoAndGifPostsEvent(ChangeOnlyDisablePreviewInVideoAndGifPostsEvent changeOnlyDisablePreviewInVideoAndGifPostsEvent) {
        if (getPostAdapter() != null) {
            getPostAdapter().setOnlyDisablePreviewInVideoPosts(changeOnlyDisablePreviewInVideoAndGifPostsEvent.onlyDisablePreviewInVideoAndGifPosts);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHidePostTypeEvent(ChangeHidePostTypeEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHidePostType(event.hidePostType);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHidePostFlairEvent(ChangeHidePostFlairEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHidePostFlair(event.hidePostFlair);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideSubredditAndUserEvent(ChangeHideSubredditAndUserPrefixEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHideSubredditAndUserPrefix(event.hideSubredditAndUserPrefix);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTheNumberOfVotesEvent(ChangeHideTheNumberOfVotesEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHideTheNumberOfVotes(event.hideTheNumberOfVotes);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTheNumberOfCommentsEvent(ChangeHideTheNumberOfCommentsEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHideTheNumberOfComments(event.hideTheNumberOfComments);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeFixedHeightPreviewCardEvent(ChangeFixedHeightPreviewInCardEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setFixedHeightPreviewInCard(event.fixedHeightPreviewInCard);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeHideTextPostContentEvent(ChangeHideTextPostContent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setHideTextPostContent(event.hideTextPostContent);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangePostFeedMaxResolutionEvent(ChangePostFeedMaxResolutionEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setPostFeedMaxResolution(event.postFeedMaxResolution);
            refreshAdapter();
        }
    }

    @Subscribe
    public void onChangeEasierToWatchInFullScreenEvent(ChangeEasierToWatchInFullScreenEvent event) {
        if (getPostAdapter() != null) {
            getPostAdapter().setEasierToWatchInFullScreen(event.easierToWatchInFullScreen);
        }
    }

    protected static abstract class LazyModeRunnable implements Runnable {
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

    protected static class StaggeredGridLayoutManagerItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private final int mHalfOffset;
        private final int mQuaterOffset;
        private final int mCard3HorizontalSpace;
        private final int mCard3VerticalSpace;
        private final int mNColumns;

        StaggeredGridLayoutManagerItemOffsetDecoration(int itemOffset, int nColumns) {
            mNColumns = nColumns;
            mCard3HorizontalSpace = -itemOffset / 4 * 3;
            mCard3VerticalSpace = -itemOffset / 4;
            mHalfOffset = itemOffset / 2;
            mQuaterOffset = itemOffset / 4;
        }

        StaggeredGridLayoutManagerItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId, int nColumns) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId), nColumns);
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            int spanIndex = layoutParams.getSpanIndex();

            if (parent.getAdapter() != null) {
                RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(view);
                if (viewHolder instanceof PostRecyclerViewAdapter.PostMaterial3CardVideoAutoplayViewHolder ||
                        viewHolder instanceof PostRecyclerViewAdapter.PostMaterial3CardVideoAutoplayLegacyControllerViewHolder ||
                        viewHolder instanceof PostRecyclerViewAdapter.PostMaterial3CardWithPreviewViewHolder ||
                        viewHolder instanceof PostRecyclerViewAdapter.PostMaterial3CardGalleryTypeViewHolder ||
                        viewHolder instanceof PostRecyclerViewAdapter.PostMaterial3CardTextTypeViewHolder) {
                    if (mNColumns == 2) {
                        if (spanIndex == 0) {
                            outRect.set(-mHalfOffset, mCard3VerticalSpace, mCard3HorizontalSpace, mCard3VerticalSpace);
                        } else {
                            outRect.set(mCard3HorizontalSpace, mCard3VerticalSpace, -mHalfOffset, mCard3VerticalSpace);
                        }
                    } else if (mNColumns == 3) {
                        if (spanIndex == 0) {
                            outRect.set(-mHalfOffset, mCard3VerticalSpace, mCard3HorizontalSpace, mCard3VerticalSpace);
                        } else if (spanIndex == 1) {
                            outRect.set(mCard3HorizontalSpace, mCard3VerticalSpace, mCard3HorizontalSpace, mCard3VerticalSpace);
                        } else {
                            outRect.set(mCard3HorizontalSpace, mCard3VerticalSpace, -mHalfOffset, mCard3VerticalSpace);
                        }
                    }
                    return;
                }
            }

            if (mNColumns == 2) {
                if (spanIndex == 0) {
                    outRect.set(mHalfOffset, 0, mQuaterOffset, 0);
                } else {
                    outRect.set(mQuaterOffset, 0, mHalfOffset, 0);
                }
            } else if (mNColumns == 3) {
                if (spanIndex == 0) {
                    outRect.set(mHalfOffset, 0, mQuaterOffset, 0);
                } else if (spanIndex == 1) {
                    outRect.set(mQuaterOffset, 0, mQuaterOffset, 0);
                } else {
                    outRect.set(mQuaterOffset, 0, mHalfOffset, 0);
                }
            }
        }
    }

    public interface LoadIconListener {
        void loadIconSuccess(String subredditOrUserName, String iconUrl);
    }
}