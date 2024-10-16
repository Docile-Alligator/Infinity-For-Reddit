package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.ChangeRememberMutingOptionInPostFeedEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionThresholdEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVibrateWhenActionTriggeredEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
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
    protected LinearLayoutManagerBugFixed mLinearLayoutManager;
    protected StaggeredGridLayoutManager mStaggeredGridLayoutManager;
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
    protected ItemTouchHelper touchHelper;
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

        return super.onCreateView(inflater, container, savedInstanceState);
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

    public abstract boolean startLazyMode();

    public abstract void stopLazyMode();

    public abstract void resumeLazyMode(boolean resumeNow);

    public abstract void pauseLazyMode(boolean startTimer);

    public final boolean isInLazyMode() {
        return isInLazyMode;
    }

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

    public abstract boolean isRecyclerViewItemSwipeable(RecyclerView.ViewHolder viewHolder);

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
                LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditOrUserName,
                        mRetrofit, iconImageUrl -> {
                            subredditOrUserIcons.put(subredditOrUserName, iconImageUrl);
                            loadIconListener.loadIconSuccess(subredditOrUserName, iconImageUrl);
                        });
            }
        }
    }

    public void markPostAsRead(Post post) {
        // no-op
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