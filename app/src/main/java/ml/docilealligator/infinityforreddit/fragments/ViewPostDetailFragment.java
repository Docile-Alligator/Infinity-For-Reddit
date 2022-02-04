package ml.docilealligator.infinityforreddit.fragments;

import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;
import static ml.docilealligator.infinityforreddit.activities.CommentActivity.RETURN_EXTRA_COMMENT_DATA_KEY;
import static ml.docilealligator.infinityforreddit.activities.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.evernote.android.state.State;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.media.VolumeInfo;
import ml.docilealligator.infinityforreddit.DeleteThing;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.EditPostActivity;
import ml.docilealligator.infinityforreddit.activities.GiveAwardActivity;
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.SubmitCrosspostActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.CommentsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.PostDetailRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostCommentSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.FetchComment;
import ml.docilealligator.infinityforreddit.comment.FetchRemovedComment;
import ml.docilealligator.infinityforreddit.comment.FetchRemovedCommentReveddit;
import ml.docilealligator.infinityforreddit.comment.ParseComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CustomToroContainer;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.events.FlairSelectedEvent;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.post.FetchPost;
import ml.docilealligator.infinityforreddit.post.FetchRemovedPost;
import ml.docilealligator.infinityforreddit.post.HidePost;
import ml.docilealligator.infinityforreddit.post.ParsePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewPostDetailFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_POST_DATA = "EPD";
    public static final String EXTRA_POST_ID = "EPI";
    public static final String EXTRA_SINGLE_COMMENT_ID = "ESCI";
    public static final String EXTRA_CONTEXT_NUMBER = "ECN";
    public static final String EXTRA_MESSAGE_FULLNAME = "EMF";
    public static final String EXTRA_POST_LIST_POSITION = "EPLP";
    private static final int EDIT_POST_REQUEST_CODE = 2;

    @BindView(R.id.swipe_refresh_layout_view_post_detail_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.post_detail_recycler_view_view_post_detail_fragment)
    CustomToroContainer mRecyclerView;
    RecyclerView mCommentsRecyclerView;
    @BindView(R.id.fetch_post_info_linear_layout_view_post_detail_fragment)
    LinearLayout mFetchPostInfoLinearLayout;
    @BindView(R.id.fetch_post_info_image_view_view_post_detail_fragment)
    ImageView mFetchPostInfoImageView;
    @BindView(R.id.fetch_post_info_text_view_view_post_detail_fragment)
    TextView mFetchPostInfoTextView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("pushshift")
    Retrofit pushshiftRetrofit;
    @Inject
    @Named("reveddit")
    Retrofit revedditRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("gfycat")
    Retrofit mGfycatRetrofit;
    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;
    @Inject
    @Named("streamable")
    Retrofit mStreamableRetrofit;
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
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("post_details")
    SharedPreferences mPostDetailsSharedPreferences;
    @Inject
    @Named("post_history")
    SharedPreferences mPostHistorySharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    ExoCreator mExoCreator;
    @Inject
    Executor mExecutor;
    @State
    Post mPost;
    @State
    boolean isLoadingMoreChildren = false;
    @State
    boolean isRefreshing = false;
    @State
    boolean isSingleCommentThreadMode = false;
    @State
    ArrayList<Comment> comments;
    @State
    ArrayList<String> children;
    @State
    int mChildrenStartingIndex = 0;
    @State
    boolean loadMoreChildrenSuccess = true;
    @State
    boolean hasMoreChildren;
    @State
    boolean isFetchingComments = false;
    @State
    String mMessageFullname;
    @State
    String sortType;
    @State
    boolean mRespectSubredditRecommendedSortType;
    @State
    long viewPostDetailFragmentId;
    private ViewPostDetailActivity activity;
    private RequestManager mGlide;
    private Locale mLocale;
    private Menu mMenu;
    private String mAccessToken;
    private String mAccountName;
    private int postListPosition = -1;
    private String mSingleCommentId;
    private String mContextNumber;
    private boolean showToast = false;
    private boolean isSortingComments = false;
    private boolean mIsSmoothScrolling = false;
    private boolean mLockFab;
    private boolean mSwipeUpToHideFab;
    private boolean mExpandChildren;
    private boolean mSeparatePostAndComments = false;
    private boolean mMarkPostsAsRead;
    private ConcatAdapter mConcatAdapter;
    private PostDetailRecyclerViewAdapter mPostAdapter;
    private CommentsRecyclerViewAdapter mCommentsAdapter;
    private RecyclerView.SmoothScroller mSmoothScroller;
    private Drawable mSavedIcon;
    private Drawable mUnsavedIcon;
    private ColorDrawable backgroundSwipeRight;
    private ColorDrawable backgroundSwipeLeft;
    private Drawable drawableSwipeRight;
    private Drawable drawableSwipeLeft;
    private int swipeLeftAction;
    private int swipeRightAction;
    private float swipeActionThreshold;
    private ItemTouchHelper touchHelper;

    public ViewPostDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_post_detail, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        setHasOptionsMenu(true);

        Bridge.restoreInstanceState(this, savedInstanceState);

        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

        applyTheme();

        mRecyclerView.addOnWindowFocusChangedListener(this::onWindowFocusChanged);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        mSavedIcon = getMenuItemIcon(R.drawable.ic_bookmark_toolbar_24dp);
        mUnsavedIcon = getMenuItemIcon(R.drawable.ic_bookmark_border_toolbar_24dp);

        if (getResources().getBoolean(R.bool.isTablet) || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCommentsRecyclerView = rootView.findViewById(R.id.comments_recycler_view_view_post_detail_fragment);
        }
        if (!((mPostDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_POST_AND_COMMENTS_IN_LANDSCAPE_MODE, true)
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                || (mPostDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_POST_AND_COMMENTS_IN_PORTRAIT_MODE, false)))) {
            if (mCommentsRecyclerView != null) {
                mCommentsRecyclerView.setVisibility(View.GONE);
                mCommentsRecyclerView = null;
            }
        } else {
            mSeparatePostAndComments = true;
        }

        if (activity != null && activity.isImmersiveInterface()) {
            mRecyclerView.setPadding(0, 0, 0, activity.getNavBarHeight() + mRecyclerView.getPaddingBottom());
            if (mCommentsRecyclerView != null) {
                mCommentsRecyclerView.setPadding(0, 0, 0, activity.getNavBarHeight() + mCommentsRecyclerView.getPaddingBottom());
            }
            showToast = true;
        }

        mLockFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false);
        mSwipeUpToHideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_UP_TO_HIDE_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false);
        mExpandChildren = !mSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_TOP_LEVEL_COMMENTS_FIRST, false);
        mMarkPostsAsRead = mPostHistorySharedPreferences.getBoolean(mAccountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false);
        if (savedInstanceState == null) {
            mRespectSubredditRecommendedSortType = mSharedPreferences.getBoolean(SharedPreferencesUtils.RESPECT_SUBREDDIT_RECOMMENDED_COMMENT_SORT_TYPE, false);
            viewPostDetailFragmentId = System.currentTimeMillis();
        }

        mGlide = Glide.with(this);
        mLocale = getResources().getConfiguration().locale;

        if (children != null && children.size() > 0) {
            (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!mIsSmoothScrolling && !mLockFab) {
                        if (!recyclerView.canScrollVertically(1)) {
                            activity.hideFab();
                        } else {
                            if (dy > 0) {
                                if (mSwipeUpToHideFab) {
                                    activity.showFab();
                                } else {
                                    activity.hideFab();
                                }
                            } else {
                                if (mSwipeUpToHideFab) {
                                    activity.hideFab();
                                } else {
                                    activity.showFab();
                                }
                            }
                        }
                    }

                    if (!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                        int visibleItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getChildCount();
                        int totalItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getItemCount();
                        int firstVisibleItemPosition = ((LinearLayoutManagerBugFixed) (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager()).findFirstVisibleItemPosition();

                        if (mCommentsAdapter != null && mCommentsAdapter.getItemCount() >= 1 && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                            fetchMoreComments();
                        }
                    }
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        mIsSmoothScrolling = false;
                    }
                }
            });
        } else {
            (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!mIsSmoothScrolling && !mLockFab) {
                        if (!recyclerView.canScrollVertically(1)) {
                            activity.hideFab();
                        } else {
                            if (dy > 0) {
                                if (mSwipeUpToHideFab) {
                                    activity.showFab();
                                } else {
                                    activity.hideFab();
                                }
                            } else {
                                if (mSwipeUpToHideFab) {
                                    activity.hideFab();
                                } else {
                                    activity.showFab();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        mIsSmoothScrolling = false;
                    }
                }
            });
        }

        boolean enableSwipeAction = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false);
        boolean vibrateWhenActionTriggered = mSharedPreferences.getBoolean(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED, true);
        swipeActionThreshold = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD, "0.3"));
        swipeRightAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_RIGHT_ACTION, "1"));
        swipeLeftAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_LEFT_ACTION, "0"));
        initializeSwipeActionDrawable();
        touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            boolean exceedThreshold = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof CommentsRecyclerViewAdapter.CommentViewHolder)) {
                    return makeMovementFlags(0, 0);
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
                    touchHelper.attachToRecyclerView(null);
                    touchHelper.attachToRecyclerView((mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView));
                    if (mCommentsAdapter != null) {
                        mCommentsAdapter.onItemSwipe(viewHolder, direction, swipeLeftAction, swipeRightAction);
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

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
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return swipeActionThreshold;
            }
        });

        if (enableSwipeAction) {
            touchHelper.attachToRecyclerView((mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView));
        }

        mSwipeRefreshLayout.setOnRefreshListener(() -> refresh(true, true));

        mSmoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        mSingleCommentId = getArguments().getString(EXTRA_SINGLE_COMMENT_ID);
        mContextNumber = getArguments().getString(EXTRA_CONTEXT_NUMBER, "8");

        if (savedInstanceState == null) {
            if (mSingleCommentId != null) {
                isSingleCommentThreadMode = true;
            }
            mMessageFullname = getArguments().getString(EXTRA_MESSAGE_FULLNAME);

            if (!mRespectSubredditRecommendedSortType || isSingleCommentThreadMode) {
                sortType = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT, SortType.Type.BEST.value.toUpperCase());
                if (sortType != null) {
                    activity.setTitle(new SortType(SortType.Type.valueOf(sortType)).getType().fullName);
                    sortType = sortType.toLowerCase();
                }
            }
        } else {
            if (sortType != null) {
                activity.setTitle(new SortType(SortType.Type.valueOf(sortType.toUpperCase())).getType().fullName);
            }
        }

        if (getArguments().containsKey(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getArguments().getInt(EXTRA_POST_LIST_POSITION, -1);
        }

        bindView();

        return rootView;
    }

    private void bindView() {
        if (mAccessToken != null && mMessageFullname != null) {
            ReadMessage.readMessage(mOauthRetrofit, mAccessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
                @Override
                public void readSuccess() {
                    mMessageFullname = null;
                }

                @Override
                public void readFailed() {

                }
            });
        }

        if (mPost == null) {
            mPost = getArguments().getParcelable(EXTRA_POST_DATA);
        }

        if (mPost == null) {
            fetchPostAndCommentsById(getArguments().getString(EXTRA_POST_ID));
        } else {
            setupMenu();

            mPostAdapter = new PostDetailRecyclerViewAdapter(activity,
                    this, mExecutor, mCustomThemeWrapper, mRetrofit, mOauthRetrofit, mGfycatRetrofit,
                    mRedgifsRetrofit, mStreamableRetrofit, mRedditDataRoomDatabase, mGlide,
                    mSeparatePostAndComments, mAccessToken, mAccountName, mPost, mLocale,
                    mSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostDetailsSharedPreferences,
                    mExoCreator, post -> EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition)));
            mCommentsAdapter = new CommentsRecyclerViewAdapter(activity,
                    this, mCustomThemeWrapper, mExecutor, mRetrofit, mOauthRetrofit,
                    mAccessToken, mAccountName, mPost, mLocale, mSingleCommentId,
                    isSingleCommentThreadMode, mSharedPreferences,
                    new CommentsRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                        @Override
                        public void retryFetchingComments() {
                            fetchCommentsRespectRecommendedSort(false);
                        }

                        @Override
                        public void retryFetchingMoreComments() {
                            isLoadingMoreChildren = false;
                            loadMoreChildrenSuccess = true;

                            fetchMoreComments();
                        }
                    });
            if (mCommentsRecyclerView != null) {
                mRecyclerView.setAdapter(mPostAdapter);
                mCommentsRecyclerView.setAdapter(mCommentsAdapter);
            } else {
                mConcatAdapter = new ConcatAdapter(mPostAdapter, mCommentsAdapter);
                mRecyclerView.setAdapter(mConcatAdapter);
            }

            if (comments == null) {
                fetchCommentsRespectRecommendedSort(false);
            } else {
                if (isRefreshing) {
                    isRefreshing = false;
                    refresh(true, true);
                } else if (isFetchingComments) {
                    fetchCommentsRespectRecommendedSort(false);
                } else {
                    mCommentsAdapter.addComments(comments, hasMoreChildren);
                    if (isLoadingMoreChildren) {
                        isLoadingMoreChildren = false;
                        fetchMoreComments();
                    }
                }
            }
        }

        mRecyclerView.setCacheManager(mPostAdapter);
        mRecyclerView.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(true, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });
    }

    private void setupMenu() {
        if (mMenu != null) {
            MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_fragment);
            MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_fragment);

            mMenu.findItem(R.id.action_comment_view_post_detail_fragment).setVisible(true);
            mMenu.findItem(R.id.action_sort_view_post_detail_fragment).setVisible(true);

            if (mAccessToken != null) {
                if (mPost.isSaved()) {
                    saveItem.setVisible(true);
                    saveItem.setIcon(mSavedIcon);
                } else {
                    saveItem.setVisible(true);
                    saveItem.setIcon(mUnsavedIcon);
                }

                if (mPost.isHidden()) {
                    hideItem.setVisible(true);
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, hideItem, activity.getString(R.string.action_unhide_post));
                } else {
                    hideItem.setVisible(true);
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, hideItem, activity.getString(R.string.action_hide_post));
                }
            } else {
                saveItem.setVisible(false);
                hideItem.setVisible(false);
            }

            if (mPost.getAuthor().equals(mAccountName)) {
                if (mPost.getPostType() == Post.TEXT_TYPE) {
                    mMenu.findItem(R.id.action_edit_view_post_detail_fragment).setVisible(true);
                }
                mMenu.findItem(R.id.action_delete_view_post_detail_fragment).setVisible(true);

                MenuItem nsfwItem = mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment);
                nsfwItem.setVisible(true);
                if (mPost.isNSFW()) {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, nsfwItem, activity.getString(R.string.action_unmark_nsfw));
                } else {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, nsfwItem, activity.getString(R.string.action_mark_nsfw));
                }

                MenuItem spoilerItem = mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment);
                spoilerItem.setVisible(true);
                if (mPost.isSpoiler()) {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, spoilerItem, activity.getString(R.string.action_unmark_spoiler));
                } else {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, spoilerItem, activity.getString(R.string.action_mark_spoiler));
                }

                mMenu.findItem(R.id.action_edit_flair_view_post_detail_fragment).setVisible(true);
            }

            mMenu.findItem(R.id.action_view_crosspost_parent_view_post_detail_fragment).setVisible(mPost.getCrosspostParentId() != null);

            if ("[deleted]".equals(mPost.getAuthor()) ||
                    "[deleted]".equals(mPost.getSelfText()) ||
                    "[removed]".equals(mPost.getSelfText())
            ) {
                mMenu.findItem(R.id.action_see_removed_view_post_detail_fragment).setVisible(true);
            }
        }
    }

    private void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        }
    }

    private Drawable getMenuItemIcon(int drawableId) {
        Drawable icon = ContextCompat.getDrawable(activity, drawableId);
        if (icon != null) {
            DrawableCompat.setTint(icon, mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        }

        return icon;
    }

    public void addComment(Comment comment) {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.addComment(comment);
        }
        if (mPostAdapter != null) {
            mPostAdapter.addOneComment();
        }
        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
    }

    public void addChildComment(Comment comment, String parentFullname, int parentPosition) {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.addChildComment(comment, parentFullname, parentPosition);
        }
        if (mPostAdapter != null) {
            mPostAdapter.addOneComment();
        }
        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
    }

    public void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.editComment(commentAuthor,
                    commentContentMarkdown,
                    position);
        }
    }

    public void awardGiven(String awardsHTML, int awardCount, int position) {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.giveAward(awardsHTML, awardCount, position);
        }
    }

    public void changeFlair(Flair flair) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        params.put(APIUtils.FLAIR_TEMPLATE_ID_KEY, flair.getId());
        params.put(APIUtils.LINK_KEY, mPost.getFullName());
        params.put(APIUtils.TEXT_KEY, flair.getText());

        mOauthRetrofit.create(RedditAPI.class).selectFlair(mPost.getSubredditNamePrefixed(),
                APIUtils.getOAuthHeader(mAccessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    refresh(true, false);
                    showMessage(R.string.update_flair_success);
                } else {
                    showMessage(R.string.update_flair_failed);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showMessage(R.string.update_flair_failed);
            }
        });
    }

    public void changeSortType(SortType sortType) {
        mFetchPostInfoLinearLayout.setVisibility(View.GONE);
        mGlide.clear(mFetchPostInfoImageView);
        mChildrenStartingIndex = 0;
        if (children != null) {
            children.clear();
        }
        this.sortType = sortType.getType().value;
        if (!mSharedPreferences.getBoolean(SharedPreferencesUtils.RESPECT_SUBREDDIT_RECOMMENDED_COMMENT_SORT_TYPE, false)
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_SORT_TYPE, true)) {
            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT, sortType.getType().name()).apply();
        }
        fetchCommentsRespectRecommendedSort(false, false, sortType.getType().value);
    }

    public void goToTop() {
        ((LinearLayoutManagerBugFixed) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        if (mCommentsRecyclerView != null) {
            ((LinearLayoutManagerBugFixed) mCommentsRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        }
    }

    public void saveComment(int position, boolean isSaved) {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.setSaveComment(position, isSaved);
        }
    }

    public void searchComment(String query, boolean searchNextComment) {
        if (mCommentsAdapter != null) {
            ArrayList<Comment> visibleComments = mCommentsAdapter.getVisibleComments();
            int currentSearchIndex = mCommentsAdapter.getSearchCommentIndex();
            if (currentSearchIndex >= 0) {
                mCommentsAdapter.notifyItemChanged(currentSearchIndex);
            }
            if (visibleComments != null) {
                if (searchNextComment) {
                    for (int i = currentSearchIndex + 1; i < visibleComments.size(); i++) {
                        if (visibleComments.get(i).getCommentRawText() != null &&
                                visibleComments.get(i).getCommentRawText().toLowerCase().contains(query.toLowerCase())) {
                            if (mCommentsAdapter != null) {
                                mCommentsAdapter.highlightSearchResult(i);
                                mCommentsAdapter.notifyItemChanged(i);
                                if (mCommentsRecyclerView == null) {
                                    mRecyclerView.scrollToPosition(i + 1);
                                } else {
                                    mCommentsRecyclerView.scrollToPosition(i);
                                }
                            }
                            return;
                        }
                    }

                    return;
                } else {
                    for (int i = currentSearchIndex - 1; i >= 0; i--) {
                        if (visibleComments.get(i).getCommentRawText() != null &&
                                visibleComments.get(i).getCommentRawText().toLowerCase().contains(query.toLowerCase())) {
                            if (mCommentsAdapter != null) {
                                mCommentsAdapter.highlightSearchResult(i);
                                mCommentsAdapter.notifyItemChanged(i);
                                if (mCommentsRecyclerView == null) {
                                    mRecyclerView.scrollToPosition(i + 1);
                                } else {
                                    mCommentsRecyclerView.scrollToPosition(i);
                                }
                            }
                            return;
                        }
                    }

                    return;
                }
            }
        }
    }

    public void resetSearchCommentIndex() {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.resetCommentSearchIndex();
        }
    }

    public void loadIcon(String authorName, LoadIconListener loadIconListener) {
        if (activity.authorIcons.containsKey(authorName)) {
            loadIconListener.loadIconSuccess(authorName, activity.authorIcons.get(authorName));
        } else {
            LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase, authorName,
                    mRetrofit, iconImageUrl -> {
                        activity.authorIcons.put(authorName, iconImageUrl);
                        loadIconListener.loadIconSuccess(authorName, iconImageUrl);
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.view_post_detail_fragment, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        if (mPost != null) {
            setupMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_view_post_detail_fragment) {
            if (activity.toggleSearchPanelVisibility() && mCommentsAdapter != null) {
                mCommentsAdapter.resetCommentSearchIndex();
            }
        } else if (itemId == R.id.action_refresh_view_post_detail_fragment) {
            refresh(true, true);
            return true;
        } else if (itemId == R.id.action_comment_view_post_detail_fragment) {
            if (mPost != null) {
                if (mPost.isArchived()) {
                    showMessage(R.string.archived_post_reply_unavailable);
                    return true;
                }

                if (mPost.isLocked()) {
                    showMessage(R.string.locked_post_comment_unavailable);
                    return true;
                }

                if (mAccessToken == null) {
                    showMessage(R.string.login_first);
                    return true;
                }

                Intent intent = new Intent(activity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY, mPost.getTitle());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
            }
            return true;
        } else if (itemId == R.id.action_save_view_post_detail_fragment) {
            if (mPost != null && mAccessToken != null) {
                if (mPost.isSaved()) {
                    item.setIcon(mUnsavedIcon);
                    SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    if (isAdded()) {
                                        mPost.setSaved(false);
                                        item.setIcon(mUnsavedIcon);
                                        showMessage(R.string.post_unsaved_success);
                                    }
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                }

                                @Override
                                public void failed() {
                                    if (isAdded()) {
                                        mPost.setSaved(true);
                                        item.setIcon(mSavedIcon);
                                        showMessage(R.string.post_unsaved_failed);
                                    }
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                }
                            });
                } else {
                    item.setIcon(mSavedIcon);
                    SaveThing.saveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    if (isAdded()) {
                                        mPost.setSaved(true);
                                        item.setIcon(mSavedIcon);
                                        showMessage(R.string.post_saved_success);
                                    }
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                }

                                @Override
                                public void failed() {
                                    if (isAdded()) {
                                        mPost.setSaved(false);
                                        item.setIcon(mUnsavedIcon);
                                        showMessage(R.string.post_saved_failed);
                                    }
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                }
                            });
                }
            }
            return true;
        } else if (itemId == R.id.action_sort_view_post_detail_fragment) {
            if (mPost != null) {
                PostCommentSortTypeBottomSheetFragment postCommentSortTypeBottomSheetFragment = new PostCommentSortTypeBottomSheetFragment();
                postCommentSortTypeBottomSheetFragment.show(activity.getSupportFragmentManager(), postCommentSortTypeBottomSheetFragment.getTag());
            }
            return true;
        } else if (itemId == R.id.action_view_crosspost_parent_view_post_detail_fragment) {
            Intent crosspostIntent = new Intent(activity, ViewPostDetailActivity.class);
            crosspostIntent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
            startActivity(crosspostIntent);
            return true;
        } else if (itemId == R.id.action_hide_view_post_detail_fragment) {
            if (mPost != null && mAccessToken != null) {
                if (mPost.isHidden()) {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, getString(R.string.action_hide_post));

                    HidePost.unhidePost(mOauthRetrofit, mAccessToken, mPost.getFullName(), new HidePost.HidePostListener() {
                        @Override
                        public void success() {
                            mPost.setHidden(false);
                            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, activity.getString(R.string.action_hide_post));
                            showMessage(R.string.post_unhide_success);
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                        }

                        @Override
                        public void failed() {
                            mPost.setHidden(true);
                            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, activity.getString(R.string.action_unhide_post));
                            showMessage(R.string.post_unhide_failed);
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                        }
                    });
                } else {
                    Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, getString(R.string.action_unhide_post));

                    HidePost.hidePost(mOauthRetrofit, mAccessToken, mPost.getFullName(), new HidePost.HidePostListener() {
                        @Override
                        public void success() {
                            mPost.setHidden(true);
                            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, activity.getString(R.string.action_unhide_post));
                            showMessage(R.string.post_hide_success);
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                        }

                        @Override
                        public void failed() {
                            mPost.setHidden(false);
                            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, activity.getString(R.string.action_hide_post));
                            showMessage(R.string.post_hide_failed);
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                        }
                    });
                }
            }
            return true;
        } else if (itemId == R.id.action_edit_view_post_detail_fragment) {
            Intent editPostIntent = new Intent(activity, EditPostActivity.class);
            editPostIntent.putExtra(EditPostActivity.EXTRA_FULLNAME, mPost.getFullName());
            editPostIntent.putExtra(EditPostActivity.EXTRA_TITLE, mPost.getTitle());
            editPostIntent.putExtra(EditPostActivity.EXTRA_CONTENT, mPost.getSelfText());
            startActivityForResult(editPostIntent, EDIT_POST_REQUEST_CODE);
            return true;
        } else if (itemId == R.id.action_delete_view_post_detail_fragment) {
            new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.delete_this_post)
                    .setMessage(R.string.are_you_sure)
                    .setPositiveButton(R.string.delete, (dialogInterface, i)
                            -> DeleteThing.delete(mOauthRetrofit, mPost.getFullName(), mAccessToken, new DeleteThing.DeleteThingListener() {
                        @Override
                        public void deleteSuccess() {
                            Toast.makeText(activity, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }

                        @Override
                        public void deleteFailed() {
                            showMessage(R.string.delete_post_failed);
                        }
                    }))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        } else if (itemId == R.id.action_nsfw_view_post_detail_fragment) {
            if (mPost.isNSFW()) {
                unmarkNSFW();
            } else {
                markNSFW();
            }
            return true;
        } else if (itemId == R.id.action_spoiler_view_post_detail_fragment) {
            if (mPost.isSpoiler()) {
                unmarkSpoiler();
            } else {
                markSpoiler();
            }
            return true;
        } else if (itemId == R.id.action_edit_flair_view_post_detail_fragment) {
            FlairBottomSheetFragment flairBottomSheetFragment = new FlairBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
            bundle.putLong(FlairBottomSheetFragment.EXTRA_VIEW_POST_DETAIL_FRAGMENT_ID, viewPostDetailFragmentId);
            flairBottomSheetFragment.setArguments(bundle);
            flairBottomSheetFragment.show(activity.getSupportFragmentManager(), flairBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_give_award_view_post_detail_fragment) {
            if (mAccessToken == null) {
                Toast.makeText(activity, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent giveAwardIntent = new Intent(activity, GiveAwardActivity.class);
            giveAwardIntent.putExtra(GiveAwardActivity.EXTRA_THING_FULLNAME, mPost.getFullName());
            giveAwardIntent.putExtra(GiveAwardActivity.EXTRA_ITEM_POSITION, 0);
            activity.startActivityForResult(giveAwardIntent, ViewPostDetailActivity.GIVE_AWARD_REQUEST_CODE);
            return true;
        } else if (itemId == R.id.action_report_view_post_detail_fragment) {
            if (mAccessToken == null) {
                Toast.makeText(activity, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(activity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, mPost.getFullName());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_see_removed_view_post_detail_fragment) {
            showRemovedPost();
            return true;
        } else if (itemId == R.id.action_crosspost_view_post_detail_fragment) {
            Intent submitCrosspostIntent = new Intent(activity, SubmitCrosspostActivity.class);
            submitCrosspostIntent.putExtra(SubmitCrosspostActivity.EXTRA_POST, mPost);
            startActivity(submitCrosspostIntent);
            return true;
        } else if (itemId == R.id.action_add_to_post_filter_view_post_detail_fragment) {
            Intent intent = new Intent(activity, PostFilterPreferenceActivity.class);
            intent.putExtra(PostFilterPreferenceActivity.EXTRA_POST, mPost);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == WRITE_COMMENT_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(RETURN_EXTRA_COMMENT_DATA_KEY)) {
                    Comment comment = data.getParcelableExtra(RETURN_EXTRA_COMMENT_DATA_KEY);
                    if (comment != null && comment.getDepth() == 0) {
                        addComment(comment);
                    } else {
                        String parentFullname = data.getStringExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY);
                        int parentPosition = data.getIntExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, -1);
                        if (parentFullname != null && parentPosition >= 0) {
                            addChildComment(comment, parentFullname, parentPosition);
                        }
                    }
                } else {
                    Toast.makeText(activity, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == EDIT_POST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                refresh(true, false);
            }
        }
    }

    private void tryMarkingPostAsRead() {
        if (mMarkPostsAsRead && mPost != null && !mPost.isRead()) {
            mPost.markAsRead(true);
            InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountName, mPost.getId());
            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPostAdapter != null) {
            mPostAdapter.setCanStartActivity(true);
        }
        if (mRecyclerView != null) {
            mRecyclerView.onWindowVisibilityChanged(View.VISIBLE);
        }
        tryMarkingPostAsRead();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecyclerView != null) {
            mRecyclerView.onWindowVisibilityChanged(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        comments = mCommentsAdapter == null ? null : mCommentsAdapter.getVisibleComments();
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bridge.clear(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mRecyclerView != null) {
            mRecyclerView.addOnWindowFocusChangedListener(null);
        }
        super.onDestroy();
    }

    @SuppressLint("RestrictedApi")
    protected boolean applyMenuItemTheme(Menu menu) {
        if (mCustomThemeWrapper != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (((MenuItemImpl) item).requestsActionButton()) {
                    Drawable drawable = item.getIcon();
                    if (drawable != null) {
                        DrawableCompat.setTint(drawable, mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
                        item.setIcon(drawable);
                    }
                }
                Utils.setTitleWithCustomFontToMenuItem(activity.typeface, item, null);
            }
        }
        return true;
    }

    private void fetchPostAndCommentsById(String subredditId) {
        mFetchPostInfoLinearLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mGlide.clear(mFetchPostInfoImageView);

        Call<String> postAndComments;
        if (mAccessToken == null) {
            if (isSingleCommentThreadMode && mSingleCommentId != null) {
                postAndComments = mRetrofit.create(RedditAPI.class).getPostAndCommentsSingleThreadById(
                        subredditId, mSingleCommentId, sortType, mContextNumber);
            } else {
                postAndComments = mRetrofit.create(RedditAPI.class).getPostAndCommentsById(subredditId,
                        sortType);
            }
        } else {
            if (isSingleCommentThreadMode && mSingleCommentId != null) {
                postAndComments = mOauthRetrofit.create(RedditAPI.class).getPostAndCommentsSingleThreadByIdOauth(subredditId,
                        mSingleCommentId, sortType, mContextNumber, APIUtils.getOAuthHeader(mAccessToken));
            } else {
                postAndComments = mOauthRetrofit.create(RedditAPI.class).getPostAndCommentsByIdOauth(subredditId,
                        sortType, APIUtils.getOAuthHeader(mAccessToken));
            }
        }
        postAndComments.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (!isAdded()) {
                    return;
                }
                mSwipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    ParsePost.parsePost(mExecutor, new Handler(), response.body(), new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            mPost = post;
                            tryMarkingPostAsRead();

                            setupMenu();

                            mPostAdapter = new PostDetailRecyclerViewAdapter(activity,
                                    ViewPostDetailFragment.this, mExecutor, mCustomThemeWrapper,
                                    mRetrofit, mOauthRetrofit, mGfycatRetrofit, mRedgifsRetrofit,
                                    mStreamableRetrofit, mRedditDataRoomDatabase, mGlide, mSeparatePostAndComments,
                                    mAccessToken, mAccountName, mPost, mLocale, mSharedPreferences,
                                    mNsfwAndSpoilerSharedPreferences, mPostDetailsSharedPreferences,
                                    mExoCreator,
                                    post1 -> EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition)));

                            mCommentsAdapter = new CommentsRecyclerViewAdapter(activity,
                                    ViewPostDetailFragment.this, mCustomThemeWrapper, mExecutor,
                                    mRetrofit, mOauthRetrofit, mAccessToken, mAccountName, mPost, mLocale,
                                    mSingleCommentId, isSingleCommentThreadMode, mSharedPreferences,
                                    new CommentsRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                                        @Override
                                        public void retryFetchingComments() {
                                            fetchCommentsRespectRecommendedSort(false);
                                        }

                                        @Override
                                        public void retryFetchingMoreComments() {
                                            isLoadingMoreChildren = false;
                                            loadMoreChildrenSuccess = true;

                                            fetchMoreComments();
                                        }
                                    });
                            if (mCommentsRecyclerView != null) {
                                mRecyclerView.setAdapter(mPostAdapter);
                                mCommentsRecyclerView.setAdapter(mCommentsAdapter);
                            } else {
                                mConcatAdapter = new ConcatAdapter(mPostAdapter, mCommentsAdapter);
                                mRecyclerView.setAdapter(mConcatAdapter);
                            }

                            if (mRespectSubredditRecommendedSortType) {
                                fetchCommentsRespectRecommendedSort(false);
                            } else {
                                ParseComment.parseComment(mExecutor, new Handler(), response.body(), new ArrayList<>(),
                                        mExpandChildren, new ParseComment.ParseCommentListener() {
                                            @Override
                                            public void onParseCommentSuccess(ArrayList<Comment> expandedComments, String parentId, ArrayList<String> moreChildrenFullnames) {
                                                ViewPostDetailFragment.this.children = moreChildrenFullnames;

                                                hasMoreChildren = children.size() != 0;
                                                mCommentsAdapter.addComments(expandedComments, hasMoreChildren);

                                                if (children.size() > 0) {
                                                    (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).clearOnScrollListeners();
                                                    (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                        @Override
                                                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                                            super.onScrolled(recyclerView, dx, dy);
                                                            if (!mIsSmoothScrolling && !mLockFab) {
                                                                if (!recyclerView.canScrollVertically(1)) {
                                                                    activity.hideFab();
                                                                } else {
                                                                    if (dy > 0) {
                                                                        if (mSwipeUpToHideFab) {
                                                                            activity.showFab();
                                                                        } else {
                                                                            activity.hideFab();
                                                                        }
                                                                    } else {
                                                                        if (mSwipeUpToHideFab) {
                                                                            activity.hideFab();
                                                                        } else {
                                                                            activity.showFab();
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                                                                int visibleItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getChildCount();
                                                                int totalItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getItemCount();
                                                                int firstVisibleItemPosition = ((LinearLayoutManagerBugFixed) (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager()).findFirstVisibleItemPosition();

                                                                if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                                                                    fetchMoreComments();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                                                mIsSmoothScrolling = false;
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onParseCommentFailed() {
                                                mCommentsAdapter.initiallyLoadCommentsFailed();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onParsePostFail() {
                            showErrorView(subredditId);
                        }
                    });
                } else {
                    showErrorView(subredditId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (isAdded()) {
                    showErrorView(subredditId);
                }
            }
        });
    }

    private void fetchCommentsRespectRecommendedSort(boolean changeRefreshState, boolean checkSortState, String sortType) {
        if (mRespectSubredditRecommendedSortType && mPost != null) {
            FetchSubredditData.fetchSubredditData(mOauthRetrofit, mRetrofit, mPost.getSubredditName(), mAccessToken,
                    new FetchSubredditData.FetchSubredditDataListener() {
                        @Override
                        public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                            if (subredditData.getSuggestedCommentSort() == null || subredditData.getSuggestedCommentSort().equals("null") || subredditData.getSuggestedCommentSort().equals("")) {
                                mRespectSubredditRecommendedSortType = false;
                                ViewPostDetailFragment.this.sortType = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT, SortType.Type.BEST.value.toUpperCase());
                                if (ViewPostDetailFragment.this.sortType != null) {
                                    activity.setTitle(new SortType(SortType.Type.valueOf(ViewPostDetailFragment.this.sortType)).getType().fullName);
                                    ViewPostDetailFragment.this.sortType = ViewPostDetailFragment.this.sortType.toLowerCase();
                                }
                                fetchComments(changeRefreshState, checkSortState, ViewPostDetailFragment.this.sortType);
                            } else {
                                ViewPostDetailFragment.this.sortType = subredditData.getSuggestedCommentSort();
                                String sortTypeTemp = ViewPostDetailFragment.this.sortType.toLowerCase().substring(0, 1).toUpperCase() + ViewPostDetailFragment.this.sortType.substring(1);
                                activity.setTitle(sortTypeTemp);
                                fetchComments(changeRefreshState, checkSortState, subredditData.getSuggestedCommentSort());
                            }
                        }

                        @Override
                        public void onFetchSubredditDataFail(boolean isQuarantined) {
                            mRespectSubredditRecommendedSortType = false;
                            ViewPostDetailFragment.this.sortType = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT, SortType.Type.BEST.value.toUpperCase());
                            if (ViewPostDetailFragment.this.sortType != null) {
                                activity.setTitle(new SortType(SortType.Type.valueOf(ViewPostDetailFragment.this.sortType)).getType().fullName);
                                ViewPostDetailFragment.this.sortType = ViewPostDetailFragment.this.sortType.toLowerCase();
                            }
                        }
                    });
        } else {
            fetchComments(changeRefreshState, checkSortState, sortType);
        }
    }

    private void fetchComments(boolean changeRefreshState, boolean checkSortState, String sortType) {
        isFetchingComments = true;
        mCommentsAdapter.setSingleComment(mSingleCommentId, isSingleCommentThreadMode);
        mCommentsAdapter.initiallyLoading();
        String commentId = null;
        if (isSingleCommentThreadMode) {
            commentId = mSingleCommentId;
        }

        Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
        FetchComment.fetchComments(mExecutor, new Handler(), retrofit, mAccessToken, mPost.getId(), commentId, sortType,
                mContextNumber, mExpandChildren, mLocale, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(ArrayList<Comment> expandedComments,
                                                      String parentId, ArrayList<String> children) {
                        if (checkSortState && isSortingComments) {
                            if (changeRefreshState) {
                                isRefreshing = false;
                            }

                            return;
                        }

                        ViewPostDetailFragment.this.children = children;

                        comments = expandedComments;
                        hasMoreChildren = children.size() != 0;
                        mCommentsAdapter.addComments(expandedComments, hasMoreChildren);

                        if (children.size() > 0) {
                            (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).clearOnScrollListeners();
                            (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);
                                    if (!mIsSmoothScrolling && !mLockFab) {
                                        if (!recyclerView.canScrollVertically(1)) {
                                            activity.hideFab();
                                        } else {
                                            if (dy > 0) {
                                                if (mSwipeUpToHideFab) {
                                                    activity.showFab();
                                                } else {
                                                    activity.hideFab();
                                                }
                                            } else {
                                                if (mSwipeUpToHideFab) {
                                                    activity.hideFab();
                                                } else {
                                                    activity.showFab();
                                                }
                                            }
                                        }
                                    }

                                    if (!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                                        int visibleItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getChildCount();
                                        int totalItemCount = (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager().getItemCount();
                                        int firstVisibleItemPosition = ((LinearLayoutManagerBugFixed) (mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView).getLayoutManager()).findFirstVisibleItemPosition();

                                        if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                                            fetchMoreComments();
                                        }
                                    }
                                }

                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                        mIsSmoothScrolling = false;
                                    }
                                }
                            });
                        }
                        if (changeRefreshState) {
                            isRefreshing = false;
                        }

                        isFetchingComments = false;
                    }

                    @Override
                    public void onFetchCommentFailed() {
                        isFetchingComments = false;
                        if (checkSortState && isSortingComments) {
                            if (changeRefreshState) {
                                isRefreshing = false;
                            }

                            return;
                        }

                        mCommentsAdapter.initiallyLoadCommentsFailed();
                        if (changeRefreshState) {
                            isRefreshing = false;
                        }
                    }
                });
    }

    private void fetchCommentsRespectRecommendedSort(boolean changeRefreshState) {
        fetchCommentsRespectRecommendedSort(changeRefreshState, true, sortType);
    }

    void fetchMoreComments() {
        if (isFetchingComments || isLoadingMoreChildren || !loadMoreChildrenSuccess) {
            return;
        }

        isLoadingMoreChildren = true;

        Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
        FetchComment.fetchMoreComment(mExecutor, new Handler(), retrofit, mAccessToken, children, mChildrenStartingIndex,
                0, mExpandChildren, new FetchComment.FetchMoreCommentListener() {
                    @Override
                    public void onFetchMoreCommentSuccess(ArrayList<Comment> expandedComments, int childrenStartingIndex) {
                        hasMoreChildren = childrenStartingIndex < children.size();
                        mCommentsAdapter.addComments(expandedComments, hasMoreChildren);
                        mChildrenStartingIndex = childrenStartingIndex;
                        isLoadingMoreChildren = false;
                        loadMoreChildrenSuccess = true;
                    }

                    @Override
                    public void onFetchMoreCommentFailed() {
                        isLoadingMoreChildren = false;
                        loadMoreChildrenSuccess = false;
                        mCommentsAdapter.loadMoreCommentsFailed();
                    }
                });
    }

    public void refresh(boolean fetchPost, boolean fetchComments) {
        if (mPostAdapter != null && !isRefreshing) {
            isRefreshing = true;
            mChildrenStartingIndex = 0;

            mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            mGlide.clear(mFetchPostInfoImageView);

            if (fetchComments) {
                fetchCommentsRespectRecommendedSort(!fetchPost);
            }

            if (fetchPost) {
                Retrofit retrofit;
                if (mAccessToken == null) {
                    retrofit = mRetrofit;
                } else {
                    retrofit = mOauthRetrofit;
                }
                FetchPost.fetchPost(mExecutor, new Handler(), retrofit, mPost.getId(), mAccessToken,
                        new FetchPost.FetchPostListener() {
                            @Override
                            public void fetchPostSuccess(Post post) {
                                if (isAdded()) {
                                    mPost = post;
                                    mPostAdapter.updatePost(mPost);
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                    isRefreshing = false;
                                    setupMenu();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }

                            @Override
                            public void fetchPostFailed() {
                                if (isAdded()) {
                                    showMessage(R.string.refresh_post_failed);
                                    isRefreshing = false;
                                }
                            }
                        });
            }
        }
    }

    private void showErrorView(String subredditId) {
        mSwipeRefreshLayout.setRefreshing(false);
        mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
        mFetchPostInfoLinearLayout.setOnClickListener(view -> fetchPostAndCommentsById(subredditId));
        mFetchPostInfoTextView.setText(R.string.load_post_error);
        mGlide.load(R.drawable.error_image).into(mFetchPostInfoImageView);
    }

    private void showMessage(int resId) {
        if (showToast) {
            Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
        } else {
            activity.showSnackBar(resId);
        }
    }

    private void markNSFW() {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_unmark_nsfw);
        }

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).markNSFW(APIUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_unmark_nsfw);
                            }

                            refresh(true, false);
                            showMessage(R.string.mark_nsfw_success);
                        } else {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_mark_nsfw);
                            }

                            showMessage(R.string.mark_nsfw_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        if (mMenu != null) {
                            mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_mark_nsfw);
                        }

                        showMessage(R.string.mark_nsfw_failed);
                    }
                });
    }

    private void unmarkNSFW() {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_mark_nsfw);
        }

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).unmarkNSFW(APIUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_mark_nsfw);
                            }

                            refresh(true, false);
                            showMessage(R.string.unmark_nsfw_success);
                        } else {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_unmark_nsfw);
                            }

                            showMessage(R.string.unmark_nsfw_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        if (mMenu != null) {
                            mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment).setTitle(R.string.action_unmark_nsfw);
                        }

                        showMessage(R.string.unmark_nsfw_failed);
                    }
                });
    }

    private void markSpoiler() {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_unmark_spoiler);
        }

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).markSpoiler(APIUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_unmark_spoiler);
                            }

                            refresh(true, false);
                            showMessage(R.string.mark_spoiler_success);
                        } else {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_mark_spoiler);
                            }

                            showMessage(R.string.mark_spoiler_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        if (mMenu != null) {
                            mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_mark_spoiler);
                        }

                        showMessage(R.string.mark_spoiler_failed);
                    }
                });
    }

    private void unmarkSpoiler() {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_mark_spoiler);
        }

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).unmarkSpoiler(APIUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_mark_spoiler);
                            }

                            refresh(true, false);
                            showMessage(R.string.unmark_spoiler_success);
                        } else {
                            if (mMenu != null) {
                                mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_unmark_spoiler);
                            }

                            showMessage(R.string.unmark_spoiler_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        if (mMenu != null) {
                            mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment).setTitle(R.string.action_unmark_spoiler);
                        }

                        showMessage(R.string.unmark_spoiler_failed);
                    }
                });
    }

    public void deleteComment(String fullName, int position) {
        new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete_this_comment)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteThing.delete(mOauthRetrofit, fullName, mAccessToken, new DeleteThing.DeleteThingListener() {
                    @Override
                    public void deleteSuccess() {
                        Toast.makeText(activity, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                        mCommentsAdapter.deleteComment(position);
                    }

                    @Override
                    public void deleteFailed() {
                        Toast.makeText(activity, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void showRemovedComment(Comment comment, int position) {
        Toast.makeText(activity, R.string.fetching_removed_comment, Toast.LENGTH_SHORT).show();
        FetchRemovedComment.searchRemovedComment(
                mExecutor, new Handler(), pushshiftRetrofit, comment,
                new FetchRemovedComment.FetchRemovedCommentListener() {
                    @Override
                    public void fetchSuccess(Comment comment) {
                        mCommentsAdapter.editComment(comment.getAuthor(), comment.getCommentMarkdown(), position);
                    }

                    @Override
                    public void fetchFailed() {
                        // Reveddit fallback
                        FetchRemovedCommentReveddit.fetchRemovedComment(mExecutor, new Handler(), revedditRetrofit,
                                comment, mPost.getPostTimeMillis(), mPost.getNComments(),
                                new FetchRemovedCommentReveddit.FetchRemovedCommentListener() {
                                    @Override
                                    public void fetchSuccess(Comment comment) {
                                        mCommentsAdapter.editComment(comment.getAuthor(), comment.getCommentMarkdown(), position);
                                    }

                                    @Override
                                    public void fetchFailed() {
                                        Toast.makeText(activity, R.string.show_removed_comment_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    public void changeToNomalThreadMode() {
        isSingleCommentThreadMode = false;
        mSingleCommentId = null;
        mRespectSubredditRecommendedSortType = mSharedPreferences.getBoolean(SharedPreferencesUtils.RESPECT_SUBREDDIT_RECOMMENDED_COMMENT_SORT_TYPE, false);
        refresh(false, true);
    }

    public void scrollToNextParentComment() {
        RecyclerView chooseYourView = mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView;
        if (mCommentsAdapter != null && chooseYourView != null) {
            int currentPosition = ((LinearLayoutManagerBugFixed) chooseYourView.getLayoutManager()).findFirstVisibleItemPosition();
            //int nextParentPosition = mCommentsAdapter.getNextParentCommentPosition(mCommentsRecyclerView == null ? currentPosition - 1 : currentPosition);
            int nextParentPosition = mCommentsAdapter.getNextParentCommentPosition(mCommentsRecyclerView == null && !isSingleCommentThreadMode ? currentPosition - 1 : currentPosition);
            if (nextParentPosition < 0) {
                return;
            }
            mSmoothScroller.setTargetPosition(mCommentsRecyclerView == null && !isSingleCommentThreadMode ? nextParentPosition + 1 : nextParentPosition);
            mIsSmoothScrolling = true;
            chooseYourView.getLayoutManager().startSmoothScroll(mSmoothScroller);
        }
    }

    public void scrollToPreviousParentComment() {
        RecyclerView chooseYourView = mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView;
        if (mCommentsAdapter != null && chooseYourView != null) {
            int currentPosition = ((LinearLayoutManagerBugFixed) chooseYourView.getLayoutManager()).findFirstVisibleItemPosition();
            //int previousParentPosition = mCommentsAdapter.getPreviousParentCommentPosition(mCommentsRecyclerView == null ? currentPosition - 1 : currentPosition);
            int previousParentPosition = mCommentsAdapter.getPreviousParentCommentPosition(mCommentsRecyclerView == null && !isSingleCommentThreadMode ? currentPosition - 1 : currentPosition);
            if (previousParentPosition < 0) {
                return;
            }
            mSmoothScroller.setTargetPosition(mCommentsRecyclerView == null && !isSingleCommentThreadMode ? previousParentPosition + 1 : previousParentPosition);
            mIsSmoothScrolling = true;
            chooseYourView.getLayoutManager().startSmoothScroll(mSmoothScroller);
        }
    }

    public void delayTransition() {
        TransitionManager.beginDelayedTransition((mCommentsRecyclerView == null ? mRecyclerView : mCommentsRecyclerView), new AutoTransition());
    }

    public void showRemovedPost() {
        Toast.makeText(activity, R.string.fetching_removed_post, Toast.LENGTH_SHORT).show();
        FetchRemovedPost.fetchRemovedPost(
                pushshiftRetrofit,
                mPost,
                new FetchRemovedPost.FetchRemovedPostListener() {
                    @Override
                    public void fetchSuccess(Post post) {
                        mPost = post;
                        tryMarkingPostAsRead();
                        if (mPostAdapter != null) {
                            mPostAdapter.updatePost(post);
                        }
                    }

                    @Override
                    public void fetchFailed() {
                        Toast.makeText(activity, R.string.show_removed_post_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean getIsNsfwSubreddit() {
        if (activity != null) {
            return activity.isNsfwSubreddit();
        }
        return false;
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostDetailFragment event) {
        if (mPost.getId().equals(event.post.getId())) {
            mPost.setVoteType(event.post.getVoteType());
            mPost.setSaved(event.post.isSaved());
            if (mMenu != null) {
                if (event.post.isSaved()) {
                    mMenu.findItem(R.id.action_save_view_post_detail_fragment).setIcon(mSavedIcon);
                } else {
                    mMenu.findItem(R.id.action_save_view_post_detail_fragment).setIcon(mUnsavedIcon);
                }
            }
            if (mPostAdapter != null) {
                mPostAdapter.updatePost(mPost);
            }
        }
    }

    @Subscribe
    public void onChangeNSFWBlurEvent(ChangeNSFWBlurEvent event) {
        if (mPostAdapter != null) {
            mPostAdapter.setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(event.needBlurNSFW, event.doNotBlurNsfwInNsfwSubreddits);
        }
        if (mCommentsRecyclerView != null) {
            refreshAdapter(mRecyclerView, mConcatAdapter);
        } else {
            refreshAdapter(mRecyclerView, mPostAdapter);
        }
    }

    @Subscribe
    public void onChangeSpoilerBlurEvent(ChangeSpoilerBlurEvent event) {
        if (mPostAdapter != null) {
            mPostAdapter.setBlurSpoiler(event.needBlurSpoiler);
        }
        if (mCommentsRecyclerView != null) {
            refreshAdapter(mRecyclerView, mConcatAdapter);
        } else {
            refreshAdapter(mRecyclerView, mPostAdapter);
        }
    }

    private void refreshAdapter(RecyclerView recyclerView, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        int previousPosition = -1;
        if (recyclerView.getLayoutManager() != null) {
            previousPosition = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        if (previousPosition > 0) {
            recyclerView.scrollToPosition(previousPosition);
        }
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        if (mPostAdapter != null) {
            String autoplay = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            boolean stateChanged = false;
            if (autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                mPostAdapter.setAutoplay(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_WIFI);
                stateChanged = true;
            }
            if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                mPostAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
                stateChanged = true;
            }

            if (stateChanged) {
                if (mCommentsRecyclerView == null) {
                    refreshAdapter(mRecyclerView, mConcatAdapter);
                } else {
                    refreshAdapter(mRecyclerView, mPostAdapter);
                    refreshAdapter(mCommentsRecyclerView, mCommentsAdapter);
                }
            }
        }
    }

    @Subscribe
    public void onFlairSelectedEvent(FlairSelectedEvent event) {
        if (event.viewPostDetailFragmentId == viewPostDetailFragmentId) {
            changeFlair(event.flair);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ViewPostDetailActivity) context;
    }

    @Override
    public void applyTheme() {
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        mSwipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        mFetchPostInfoTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (activity.typeface != null) {
            mFetchPostInfoTextView.setTypeface(activity.contentTypeface);
        }
    }

    private void onWindowFocusChanged(boolean hasWindowsFocus) {
        if (mPostAdapter != null) {
            mPostAdapter.setCanPlayVideo(hasWindowsFocus);
        }
    }

    public interface LoadIconListener {
        void loadIconSuccess(String authorName, String iconUrl);
    }
}
