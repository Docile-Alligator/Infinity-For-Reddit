package ml.docilealligator.infinityforreddit.fragments;

import static ml.docilealligator.infinityforreddit.activities.CommentActivity.RETURN_EXTRA_COMMENT_DATA_KEY;
import static ml.docilealligator.infinityforreddit.activities.CommentActivity.WRITE_COMMENT_REQUEST_CODE;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.INDEX_UNSET;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.TIME_UNSET;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.evernote.android.state.State;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import kotlin.Unit;
import ml.docilealligator.infinityforreddit.CommentModerationActionHandler;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.PostDetailCommentsCache;
import ml.docilealligator.infinityforreddit.PostDetailCommentsCacheManager;
import ml.docilealligator.infinityforreddit.PostModerationActionHandler;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.EditPostActivity;
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.SubmitCrosspostActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.CommentsFooterRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.CommentsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.CommentsRecyclerViewAdapterNew;
import ml.docilealligator.infinityforreddit.adapters.CommentsStatusRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.PostDetailRecyclerViewAdapterNew;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostCommentSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AdjustableTouchSlopItemTouchHelper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentViewPostDetailBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.events.FlairSelectedEvent;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.extensions.ConcatAdapterKt;
import ml.docilealligator.infinityforreddit.managers.VideoMuteManager;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;
import ml.docilealligator.infinityforreddit.viewmodels.ViewPostDetailActivityViewModel;
import ml.docilealligator.infinityforreddit.viewmodels.ViewPostDetailFragmentViewModelNew;
import retrofit2.Retrofit;

public class ViewPostDetailFragmentNew extends Fragment implements FragmentCommunicator, PostModerationActionHandler, CommentModerationActionHandler {

    public static final String EXTRA_POST_ID = "EPI";
    public static final String EXTRA_SINGLE_COMMENT_ID = "ESCI";
    public static final String EXTRA_CONTEXT_NUMBER = "ECN";
    public static final String EXTRA_MESSAGE_FULLNAME = "EMF";
    public static final String EXTRA_POST_LIST_POSITION = "EPLP";
    private static final int EDIT_POST_REQUEST_CODE = 2;
    private static final String SCROLL_POSITION_STATE = "SPS";

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
    @Inject
    PostDetailCommentsCacheManager postDetailCommentsCacheManager;
    @Inject
    VideoMuteManager mVideoMuteManager;
    @State
    ArrayList<Comment> comments;
    @State
    ArrayList<String> children;
    @State
    String mMessageFullname;
    @State
    SortType.Type sortType;
    @State
    long viewPostDetailFragmentId;
    private ViewPostDetailActivity mActivity;
    private RequestManager mGlide;
    private Menu mMenu;
    private Post mPost;
    @Nullable
    private String postId;
    private int postListPosition = -1;
    private boolean showToast = false;
    private boolean mIsSmoothScrolling = false;
    private boolean mLockFab;
    private boolean mSwipeUpToHideFab;
    private boolean mSeparatePostAndComments = false;
    private ConcatAdapter mConcatAdapter;
    private PostDetailRecyclerViewAdapterNew mPostAdapter;
    private CommentsStatusRecyclerViewAdapter mCommentsStatusAdapter;
    private CommentsRecyclerViewAdapterNew mCommentsAdapter;
    private CommentsFooterRecyclerViewAdapter mCommentsFooterAdapter;
    private RecyclerView.SmoothScroller mSmoothScroller;
    private ColorDrawable backgroundSwipeRight;
    private ColorDrawable backgroundSwipeLeft;
    private Drawable drawableSwipeRight;
    private Drawable drawableSwipeLeft;
    private int swipeLeftAction;
    private int swipeRightAction;
    private float swipeActionThreshold;
    private boolean shouldSwipeBack;
    private int commentScrollPosition = -1;
    private FragmentViewPostDetailBinding binding;
    private RecyclerView mCommentsRecyclerView;
    public ViewPostDetailFragmentViewModelNew viewPostDetailFragmentViewModel;
    public ViewPostDetailActivityViewModel viewPostDetailActivityViewModel;

    public ViewPostDetailFragmentNew() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewPostDetailBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        setHasOptionsMenu(true);

        Bridge.restoreInstanceState(this, savedInstanceState);

        EventBus.getDefault().register(this);

        applyTheme();

        binding.postDetailRecyclerViewViewPostDetailFragment.addOnWindowFocusChangedListener(this::onWindowFocusChanged);

        mCommentsRecyclerView = binding.commentsRecyclerViewViewPostDetailFragment;
        if (!((mPostDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_POST_AND_COMMENTS_IN_LANDSCAPE_MODE, true)
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                || (mPostDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_POST_AND_COMMENTS_IN_PORTRAIT_MODE, false)
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT))) {
            if (mCommentsRecyclerView != null) {
                mCommentsRecyclerView.setVisibility(View.GONE);
                mCommentsRecyclerView = null;
            }
        } else {
            mSeparatePostAndComments = true;
        }

        if (mActivity.isImmersiveInterfaceRespectForcedEdgeToEdge()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false, mActivity.isForcedImmersiveInterface());
                    binding.postDetailRecyclerViewViewPostDetailFragment.setPadding(
                            0, 0, 0, (int) Utils.convertDpToPixel(144, mActivity) + allInsets.bottom
                    );
                    if (mCommentsRecyclerView != null) {
                        mCommentsRecyclerView.setPadding(0, 0, 0, (int) Utils.convertDpToPixel(144, mActivity) + allInsets.bottom);
                    }
                    return WindowInsetsCompat.CONSUMED;
                }
            });
            showToast = true;
        }

        mLockFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false);
        mSwipeUpToHideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_UP_TO_HIDE_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false);
        if (savedInstanceState == null) {
            viewPostDetailFragmentId = System.currentTimeMillis();
        } else {
            commentScrollPosition = savedInstanceState.getInt(SCROLL_POSITION_STATE);
        }

        mGlide = Glide.with(this);
        Locale locale = getResources().getConfiguration().locale;

        (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView).addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!mIsSmoothScrolling && !mLockFab) {
                    if (!recyclerView.canScrollVertically(1)) {
                        mActivity.hideFab();
                    } else {
                        if (dy > 0) {
                            if (mSwipeUpToHideFab) {
                                mActivity.showFab();
                            } else {
                                mActivity.hideFab();
                            }
                        } else {
                            if (mSwipeUpToHideFab) {
                                mActivity.hideFab();
                            } else {
                                mActivity.showFab();
                            }
                        }
                    }
                }

                ViewPostDetailFragmentViewModelNew.UiState uiState = viewPostDetailFragmentViewModel.getUiState().getValue();
                ViewPostDetailFragmentViewModelNew.DataState dataState = viewPostDetailFragmentViewModel.getDataState().getValue();
                if (dataState != null && dataState.getComments() != null && !dataState.getComments().isEmpty()
                        && uiState != null && !uiState.isLoadingMoreChildren() && uiState.getLoadMoreChildrenSuccess()) {
                    int visibleItemCount = (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView).getLayoutManager().getChildCount();
                    int totalItemCount = (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView).getLayoutManager().getItemCount();
                    int firstVisibleItemPosition = ((LinearLayoutManagerBugFixed) (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView).getLayoutManager()).findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                        viewPostDetailFragmentViewModel.fetchMoreComments();
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

        boolean enableSwipeAction = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false);
        boolean vibrateWhenActionTriggered = mSharedPreferences.getBoolean(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED, true);
        swipeActionThreshold = Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD, "0.3"));
        swipeRightAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_RIGHT_ACTION, "1"));
        swipeLeftAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_LEFT_ACTION, "0"));
        initializeSwipeActionDrawable();
        AdjustableTouchSlopItemTouchHelper touchHelper = new AdjustableTouchSlopItemTouchHelper(new AdjustableTouchSlopItemTouchHelper.Callback() {
            boolean exceedThreshold = false;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof CommentsRecyclerViewAdapter.CommentBaseViewHolder)) {
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
                int horizontalOffset = (int) Utils.convertDpToPixel(16, mActivity);
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

                if (!isCurrentlyActive && exceedThreshold && mCommentsAdapter != null) {
                    mCommentsAdapter.onItemSwipe(viewHolder, dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START, swipeLeftAction, swipeRightAction);
                    exceedThreshold = false;
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1;
            }
        });

        (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView).setOnTouchListener((view, motionEvent) -> {
            shouldSwipeBack = motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP;
            return false;
        });

        if (enableSwipeAction) {
            touchHelper.attachToRecyclerView(
                    (mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView),
                    Float.parseFloat(mSharedPreferences.getString(SharedPreferencesUtils.SWIPE_ACTION_SENSITIVITY_IN_COMMENTS, "5"))
            );
        }

        binding.swipeRefreshLayoutViewPostDetailFragment.setOnRefreshListener(() -> viewPostDetailFragmentViewModel.refresh(true, true));

        mSmoothScroller = new LinearSmoothScroller(mActivity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        String singleCommentId = getArguments().getString(EXTRA_SINGLE_COMMENT_ID);

        if (savedInstanceState == null) {
            mMessageFullname = getArguments().getString(EXTRA_MESSAGE_FULLNAME);
        }

        if (getArguments().containsKey(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getArguments().getInt(EXTRA_POST_LIST_POSITION, -1);
        }

        postId = getArguments().getString(EXTRA_POST_ID);

        viewPostDetailActivityViewModel = new ViewModelProvider(requireActivity())
                .get(ViewPostDetailActivityViewModel.class);

        if (mPost == null) {
            mPost = viewPostDetailActivityViewModel.getPost(postListPosition);
            if (mPost == null) {
                mPost = viewPostDetailActivityViewModel.getPost();
            }
        }

        viewPostDetailFragmentViewModel = new ViewModelProvider(
                this,
                ViewPostDetailFragmentViewModelNew.Companion.provideFactory(
                        mRetrofit, mOauthRetrofit, mRedditDataRoomDatabase, mActivity.accessToken,
                        mActivity.accountName, mPost, postId, singleCommentId, comments, children,
                        sortType, mSortTypeSharedPreferences, mPostHistorySharedPreferences,
                        mSharedPreferences.getBoolean(SharedPreferencesUtils.RESPECT_SUBREDDIT_RECOMMENDED_COMMENT_SORT_TYPE, false),
                        mPostHistorySharedPreferences.getBoolean(mActivity.accountName + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false),
                        !mSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_TOP_LEVEL_COMMENTS_FIRST, false),
                        getArguments().getString(EXTRA_CONTEXT_NUMBER, "8")
                )
        ).get(ViewPostDetailFragmentViewModelNew.class);

        mPostAdapter = new PostDetailRecyclerViewAdapterNew(mActivity,
                this, mExecutor, mCustomThemeWrapper, mOauthRetrofit, mRetrofit,
                mRedgifsRetrofit, mStreamableApiProvider, mRedditDataRoomDatabase, mGlide,
                mVideoMuteManager, mSeparatePostAndComments, mActivity.accessToken,
                mActivity.accountName, mPost, locale, mSharedPreferences, mCurrentAccountSharedPreferences,
                mNsfwAndSpoilerSharedPreferences, mPostDetailsSharedPreferences,
                mPostHistorySharedPreferences, mExoCreator,
                post -> {
                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                    setupMenu();
                });

        mCommentsStatusAdapter = new CommentsStatusRecyclerViewAdapter(
                mActivity, () -> {
            changeToNormalThreadMode();
            return Unit.INSTANCE;
        }, () -> {
            viewPostDetailFragmentViewModel.fetchCommentsRespectRecommendedSort(false);
            return Unit.INSTANCE;
        });

        mCommentsAdapter = new CommentsRecyclerViewAdapterNew(mActivity,
                this, mCustomThemeWrapper, mOauthRetrofit,
                mActivity.accessToken, mActivity.accountName, mPost, locale, singleCommentId,
                mSharedPreferences, mNsfwAndSpoilerSharedPreferences,
                new CommentsRecyclerViewAdapterNew.CommentRecyclerViewAdapterCallback() {
                    @Override
                    public void expandComment(int position) {
                        viewPostDetailFragmentViewModel.expandComment(position);
                    }

                    @Override
                    public void collapseComment(int position) {
                        viewPostDetailFragmentViewModel.collapseComment(position);
                    }

                    @Override
                    public void fetchMoreChildComments(int position) {
                        viewPostDetailFragmentViewModel.fetchMoreChildComments(position);
                    }
                });

        mCommentsFooterAdapter = new CommentsFooterRecyclerViewAdapter(
                mActivity, () -> {
            viewPostDetailFragmentViewModel.fetchMoreComments();
            return Unit.INSTANCE;
        }
        );

        if (mCommentsRecyclerView != null) {
            mConcatAdapter = new ConcatAdapter(mCommentsStatusAdapter, mCommentsAdapter, mCommentsFooterAdapter);
        } else {
            mConcatAdapter = new ConcatAdapter(mPostAdapter, mCommentsStatusAdapter, mCommentsAdapter, mCommentsFooterAdapter);
        }

        viewPostDetailFragmentViewModel.getUiState().observe(getViewLifecycleOwner(), uiState -> {
            RecyclerView recyclerView = mCommentsRecyclerView != null ? mCommentsRecyclerView : binding.postDetailRecyclerViewViewPostDetailFragment;
            mCommentsStatusAdapter.setSingleCommentThreadMode(uiState.getSingleCommentId() != null && !uiState.getSingleCommentId().isEmpty());
            mCommentsStatusAdapter.setInitiallyLoading(uiState.isInitialLoading());
            mCommentsStatusAdapter.setInitiallyLoadingFailed(uiState.isInitialLoadingFailed());
            recyclerView.post(() -> mCommentsStatusAdapter.notifyDataSetChanged());

            mCommentsFooterAdapter.setLoadingMoreChildren(uiState.isLoadingMoreChildren());
            mCommentsFooterAdapter.setLoadMoreChildrenSuccess(uiState.getLoadMoreChildrenSuccess());
            recyclerView.post(() -> mCommentsFooterAdapter.notifyDataSetChanged());

            if (uiState.isInitialLoading()) {
                binding.fetchPostInfoLinearLayoutViewPostDetailFragment.setVisibility(View.GONE);
                binding.swipeRefreshLayoutViewPostDetailFragment.setRefreshing(true);
                mGlide.clear(binding.fetchPostInfoImageViewViewPostDetailFragment);
            } else {
                binding.swipeRefreshLayoutViewPostDetailFragment.setRefreshing(false);

                if (uiState.getShouldShowErrorView()) {
                    showErrorView(viewPostDetailFragmentViewModel.getDerivedPostId());
                } else {
                    if (!renderContent()) {
                        return;
                    }

                    tryMarkingPostAsRead();
                }
            }

            if (uiState.getFetchPostFailed()) {
                showMessage(R.string.refresh_post_failed);
            }

            if (uiState.isFetchingComments()) {
                if (mCommentsAdapter != null) {
                    mCommentsAdapter.initiallyLoading();
                }
            }

            if (uiState.getSortType() != null) {
                SortType.Type sortType = uiState.getSortType();
                mActivity.setTitle(sortType.fullName);
                binding.fetchPostInfoLinearLayoutViewPostDetailFragment.setVisibility(View.GONE);
                mGlide.clear(binding.fetchPostInfoImageViewViewPostDetailFragment);

                if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_SORT_TYPE, true)) {
                    mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT, sortType.name()).apply();
                }
            }
        });

        viewPostDetailFragmentViewModel.getDataState().observe(getViewLifecycleOwner(), dataState -> {
            if (dataState.getPost() != null) {
                mPost = dataState.getPost();
                if (mPostAdapter != null) {
                    mPostAdapter.updatePost(dataState.getPost());
                }
                if (mCommentsAdapter != null) {
                    mCommentsAdapter.updatePost(dataState.getPost());
                }
                EventBus.getDefault().post(new PostUpdateEventToPostList(dataState.getPost(), postListPosition));
                setupMenu();
                binding.swipeRefreshLayoutViewPostDetailFragment.setRefreshing(false);
            }

            comments = dataState.getComments();
            children = dataState.getChildren();
            mCommentsAdapter.submitList(comments);
            mCommentsStatusAdapter.setEmptyComments(comments == null || comments.isEmpty());
            mCommentsStatusAdapter.notifyDataSetChanged();

            mCommentsFooterAdapter.setHasMoreChildren(dataState.getHasMoreChildren());
            mCommentsFooterAdapter.notifyDataSetChanged();
        });

        bindView(savedInstanceState);

        return binding.getRoot();
    }

    private void bindView(Bundle savedInstanceState) {
        if (!mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT) && mMessageFullname != null) {
            ReadMessage.readMessage(mOauthRetrofit, mActivity.accessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
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
            PostDetailCommentsCache cache = savedInstanceState == null && viewPostDetailFragmentViewModel.getSingleCommentId() == null
                    ? postDetailCommentsCacheManager.getCache(postId) : null;
            if (restoreCache(cache)) {
                postDetailCommentsCacheManager.removeCache(postId);

                if (!renderContent()) {
                    return;
                }

                restoreCommentScrollPosition();
            } else {
                viewPostDetailFragmentViewModel.fetchPostAndCommentsById(postId);
            }
        } else {
            if (!renderContent()) {
                return;
            }

            PostDetailCommentsCache cache = savedInstanceState == null && viewPostDetailFragmentViewModel.getSingleCommentId() == null
                    ? postDetailCommentsCacheManager.getCache(mPost) : null;
            if (restoreCache(cache)) {
                postDetailCommentsCacheManager.removeCache(mPost);

                restoreCommentScrollPosition();
            } else {
                if (comments == null) {
                    viewPostDetailFragmentViewModel.fetchCommentsRespectRecommendedSort(false);
                } else {
                    restoreCommentScrollPosition();
                }
            }
        }

        binding.postDetailRecyclerViewViewPostDetailFragment.setCacheManager(mPostAdapter);
        binding.postDetailRecyclerViewViewPostDetailFragment.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(true, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });

        viewPostDetailFragmentViewModel.getPostModerationEventLiveData().observe(getViewLifecycleOwner(), moderationEvent -> {
            showMessage(moderationEvent.getToastMessageResId());

            if (moderationEvent instanceof PostModerationEvent.Saved
                    || moderationEvent instanceof PostModerationEvent.Unsaved
                    || moderationEvent instanceof PostModerationEvent.Hid
                    || moderationEvent instanceof PostModerationEvent.Unhid
            ) {
                setupMenu();
            } else if (moderationEvent instanceof PostModerationEvent.Deleted) {
                mActivity.finish();
            }
        });

        viewPostDetailFragmentViewModel.getCommentModerationEventLiveData().observe(getViewLifecycleOwner(), moderationEvent -> {
            showMessage(moderationEvent.getToastMessageResId());
        });
    }

    private boolean renderContent() {
        if (showSensitiveWarning()) {
            return false;
        }
        setupMenu();

        if (mCommentsRecyclerView != null) {
            if (binding.postDetailRecyclerViewViewPostDetailFragment.getAdapter() == null) {
                binding.postDetailRecyclerViewViewPostDetailFragment.setAdapter(mPostAdapter);
            }
            if (mCommentsRecyclerView.getAdapter() == null) {
                mCommentsRecyclerView.setAdapter(mConcatAdapter);
            }
        } else {
            if (binding.postDetailRecyclerViewViewPostDetailFragment.getAdapter() == null) {
                binding.postDetailRecyclerViewViewPostDetailFragment.setAdapter(mConcatAdapter);
            }
        }

        return true;
    }

    private boolean restoreCache(@Nullable PostDetailCommentsCache cache) {
        if (cache != null) {
            viewPostDetailFragmentViewModel.restoreCache(cache);
            if (viewPostDetailFragmentViewModel.getPost() == null) {
                viewPostDetailFragmentViewModel.setPost(cache.getPost());
                viewPostDetailActivityViewModel.setPost(cache.getPost());
            }
            commentScrollPosition = cache.getScrollPosition();

            return true;
        }

        return false;
    }

    private void restoreCommentScrollPosition() {
        // if the scrollPosition < 0 do nothing
        if (commentScrollPosition >= 0) {
            if (mCommentsRecyclerView != null) {
                mCommentsRecyclerView.scrollToPosition(
                        ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, commentScrollPosition));
            } else {
                binding.postDetailRecyclerViewViewPostDetailFragment.scrollToPosition(
                        ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, commentScrollPosition));
            }

            commentScrollPosition = -1;
        }
    }

    private void setupMenu() {
        if (mMenu != null) {
            MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_fragment);
            MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_fragment);

            saveItem.setVisible(true);
            hideItem.setVisible(true);
            mMenu.findItem(R.id.action_comment_view_post_detail_fragment).setVisible(true);
            mMenu.findItem(R.id.action_sort_view_post_detail_fragment).setVisible(true);
            mMenu.findItem(R.id.action_report_view_post_detail_fragment).setVisible(true);
            mMenu.findItem(R.id.action_crosspost_view_post_detail_fragment).setVisible(true);
            mMenu.findItem(R.id.action_add_to_post_filter_view_post_detail_fragment).setVisible(true);

            if (mPost.isHidden()) {
                Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, hideItem, mActivity.getString(R.string.action_unhide_post));
            } else {
                Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, hideItem, mActivity.getString(R.string.action_hide_post));
            }

            if (mPost.isSaved()) {
                Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, saveItem, mActivity.getString(R.string.action_unsave_post));
            } else {
                Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, saveItem, mActivity.getString(R.string.action_save_post));
            }

            if (Account.ANONYMOUS_ACCOUNT.equals(mActivity.accountName)) {
                mMenu.findItem(R.id.action_crosspost_view_post_detail_fragment).setVisible(false);
            }

            if (mPost.getAuthor().equals(mActivity.accountName)) {
                if (mPost.getPostType() == Post.TEXT_TYPE) {
                    mMenu.findItem(R.id.action_edit_view_post_detail_fragment).setVisible(true);
                }
                mMenu.findItem(R.id.action_delete_view_post_detail_fragment).setVisible(true);

                MenuItem nsfwItem = mMenu.findItem(R.id.action_nsfw_view_post_detail_fragment);
                nsfwItem.setVisible(true);
                if (mPost.isNSFW()) {
                    Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, nsfwItem, mActivity.getString(R.string.action_unmark_nsfw));
                } else {
                    Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, nsfwItem, mActivity.getString(R.string.action_mark_nsfw));
                }

                MenuItem spoilerItem = mMenu.findItem(R.id.action_spoiler_view_post_detail_fragment);
                spoilerItem.setVisible(true);
                if (mPost.isSpoiler()) {
                    Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, spoilerItem, mActivity.getString(R.string.action_unmark_spoiler));
                } else {
                    Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, spoilerItem, mActivity.getString(R.string.action_mark_spoiler));
                }

                mMenu.findItem(R.id.action_edit_flair_view_post_detail_fragment).setVisible(true);
            }

            mMenu.findItem(R.id.action_view_crosspost_parent_view_post_detail_fragment).setVisible(mPost.getCrosspostParentId() != null);
        }
    }

    private void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward_day_night_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(mCustomThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_downward_day_night_24dp, null);
        }
    }

    public void addComment(Comment comment) {
        viewPostDetailFragmentViewModel.addComment(comment);
    }

    public void addChildComment(Comment comment, String parentFullname, int parentPosition) {
        viewPostDetailFragmentViewModel.addChildComment(comment, parentFullname, parentPosition);
    }

    public void editComment(Comment comment, int position) {
        viewPostDetailFragmentViewModel.editComment(comment, position);
    }

    public void editComment(String commentContentMarkdown, int position) {
        viewPostDetailFragmentViewModel.editComment(commentContentMarkdown, position);
    }

    public void changeSortType(SortType sortType) {
        viewPostDetailFragmentViewModel.updateSortType(sortType.getType());
        viewPostDetailFragmentViewModel.fetchCommentsRespectRecommendedSort(sortType.getType(), false);
    }

    public void goToTop() {
        ((LinearLayoutManagerBugFixed) binding.postDetailRecyclerViewViewPostDetailFragment.getLayoutManager()).scrollToPositionWithOffset(0, 0);
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
            int currentSearchIndex = mCommentsAdapter.getSearchedPosition();
            int searchedPosition = viewPostDetailFragmentViewModel.getNextSearchedPosition(
                    query, currentSearchIndex, searchNextComment
            );
            if (searchedPosition < 0) {
                return;
            }

            int absoluteSearchedPosition = ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, searchedPosition);
            if (absoluteSearchedPosition < 0) {
                return;
            }

            if (mCommentsAdapter != null) {
                if (currentSearchIndex >= 0) {
                    mCommentsAdapter.notifyItemChanged(currentSearchIndex);
                }

                mCommentsAdapter.highlightSearchResult(searchedPosition);

                if (mCommentsRecyclerView == null) {
                    binding.postDetailRecyclerViewViewPostDetailFragment.scrollToPosition(absoluteSearchedPosition);
                } else {
                    mCommentsRecyclerView.scrollToPosition(absoluteSearchedPosition);
                }
            }
        }
    }

    public void resetSearchedPosition() {
        if (mCommentsAdapter != null) {
            mCommentsAdapter.resetSearchedPosition(
                    viewPostDetailFragmentViewModel.checkIfNotifyOldSearchedPositionNeeded(
                            mCommentsAdapter.getSearchedPosition()
                    )
            );
        }
    }

    public void loadIcon(List<Comment> comments, UserProfileImagesBatchLoader.LoadIconListener loadIconListener) {
        mActivity.loadAuthorIcons(comments, loadIconListener);
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
            if (mActivity.toggleSearchPanelVisibility() && mCommentsAdapter != null) {
                mCommentsAdapter.resetSearchedPosition(
                        viewPostDetailFragmentViewModel.checkIfNotifyOldSearchedPositionNeeded(
                                mCommentsAdapter.getSearchedPosition()
                        )
                );
            }
        } else if (itemId == R.id.action_refresh_view_post_detail_fragment) {
            viewPostDetailFragmentViewModel.refresh(true, true);
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

                if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    showMessage(R.string.login_first);
                    return true;
                }

                Intent intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TITLE_KEY, mPost.getTitle());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                intent.putExtra(CommentActivity.EXTRA_SUBREDDIT_NAME_KEY, mPost.getSubredditName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
            }
            return true;
        } else if (itemId == R.id.action_save_view_post_detail_fragment) {
            viewPostDetailFragmentViewModel.toggleSavePost(postListPosition);
            return true;
        } else if (itemId == R.id.action_sort_view_post_detail_fragment) {
            if (mPost != null) {
                ViewPostDetailFragmentViewModelNew.UiState uiState = viewPostDetailFragmentViewModel.getUiState().getValue();
                PostCommentSortTypeBottomSheetFragment postCommentSortTypeBottomSheetFragment = PostCommentSortTypeBottomSheetFragment.getNewInstance(uiState == null ? null : uiState.getSortType());
                postCommentSortTypeBottomSheetFragment.show(mActivity.getSupportFragmentManager(), postCommentSortTypeBottomSheetFragment.getTag());
            }
            return true;
        } else if (itemId == R.id.action_view_crosspost_parent_view_post_detail_fragment) {
            Intent crosspostIntent = new Intent(mActivity, ViewPostDetailActivity.class);
            crosspostIntent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
            startActivity(crosspostIntent);
            return true;
        } else if (itemId == R.id.action_hide_view_post_detail_fragment) {
            viewPostDetailFragmentViewModel.toggleHidePost(postListPosition);
            return true;
        } else if (itemId == R.id.action_edit_view_post_detail_fragment) {
            if (mPost.getMediaMetadataMap() == null) {
                Intent editPostIntent = new Intent(mActivity, EditPostActivity.class);
                editPostIntent.putExtra(EditPostActivity.EXTRA_FULLNAME, mPost.getFullName());
                editPostIntent.putExtra(EditPostActivity.EXTRA_TITLE, mPost.getTitle());
                editPostIntent.putExtra(EditPostActivity.EXTRA_CONTENT, mPost.getSelfText());
                startActivityForResult(editPostIntent, EDIT_POST_REQUEST_CODE);
            } else {
                Toast.makeText(mActivity, R.string.cannot_edit_post_with_images, Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (itemId == R.id.action_delete_view_post_detail_fragment) {
            new MaterialAlertDialogBuilder(mActivity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.delete_this_post)
                    .setMessage(R.string.are_you_sure)
                    .setPositiveButton(R.string.delete, (dialogInterface, i)
                            -> viewPostDetailFragmentViewModel.deletePost(postListPosition))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        } else if (itemId == R.id.action_nsfw_view_post_detail_fragment) {
            viewPostDetailFragmentViewModel.toggleNSFW(postListPosition);
            return true;
        } else if (itemId == R.id.action_spoiler_view_post_detail_fragment) {
            viewPostDetailFragmentViewModel.toggleSpoiler(postListPosition);
            return true;
        } else if (itemId == R.id.action_edit_flair_view_post_detail_fragment) {
            FlairBottomSheetFragment flairBottomSheetFragment = new FlairBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
            bundle.putLong(FlairBottomSheetFragment.EXTRA_VIEW_POST_DETAIL_FRAGMENT_ID, viewPostDetailFragmentId);
            flairBottomSheetFragment.setArguments(bundle);
            flairBottomSheetFragment.show(mActivity.getSupportFragmentManager(), flairBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_report_view_post_detail_fragment) {
            if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(mActivity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, mPost.getFullName());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_crosspost_view_post_detail_fragment) {
            Intent submitCrosspostIntent = new Intent(mActivity, SubmitCrosspostActivity.class);
            submitCrosspostIntent.putExtra(SubmitCrosspostActivity.EXTRA_POST, mPost);
            startActivity(submitCrosspostIntent);
            return true;
        } else if (itemId == R.id.action_add_to_post_filter_view_post_detail_fragment) {
            Intent intent = new Intent(mActivity, PostFilterPreferenceActivity.class);
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
                    Toast.makeText(mActivity, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == EDIT_POST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                viewPostDetailFragmentViewModel.refresh(true, false);
            }
        }
    }

    private void tryMarkingPostAsRead() {
        viewPostDetailFragmentViewModel.tryMarkingPostAsRead();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPostAdapter != null) {
            mPostAdapter.setCanStartActivity(true);
        }
        if (mCommentsAdapter != null) {
            mCommentsAdapter.setCanStartActivity(true);
        }
        binding.postDetailRecyclerViewViewPostDetailFragment.onWindowVisibilityChanged(View.VISIBLE);
        tryMarkingPostAsRead();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.postDetailRecyclerViewViewPostDetailFragment.onWindowVisibilityChanged(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPost == null) {
            return;
        }

        if (viewPostDetailFragmentViewModel.getSingleCommentId() != null) {
            return;
        }

        if (mCommentsAdapter == null) {
            return;
        }

        comments = mCommentsAdapter.getVisibleComments();
        if (comments == null) {
            return;
        }

        updateCommentScrollPosition();

        postDetailCommentsCacheManager.saveCache(
                mPost,
                comments,
                children,
                viewPostDetailFragmentViewModel.getSortType(),
                commentScrollPosition
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        sortType = viewPostDetailFragmentViewModel.getSortType();
        outState.putInt(SCROLL_POSITION_STATE, commentScrollPosition);
        Bridge.saveInstanceState(this, outState);
    }

    private void updateCommentScrollPosition() {
        LinearLayoutManager layoutManager;
        if (mCommentsRecyclerView != null) {
            layoutManager = (LinearLayoutManager) mCommentsRecyclerView.getLayoutManager();
        } else {
            layoutManager = (LinearLayoutManager) binding.postDetailRecyclerViewViewPostDetailFragment.getLayoutManager();
        }
        commentScrollPosition = layoutManager != null ? ConcatAdapterKt.getLocalPosition(mConcatAdapter, mCommentsAdapter, layoutManager.findFirstVisibleItemPosition()) : 0;
    }

    @Override
    public void onDestroyView() {
        Bridge.clear(this);
        EventBus.getDefault().unregister(this);
        binding.postDetailRecyclerViewViewPostDetailFragment.addOnWindowFocusChangedListener(null);
        super.onDestroyView();
    }

    @SuppressLint("RestrictedApi")
    protected boolean applyMenuItemTheme(Menu menu) {
        if (mCustomThemeWrapper != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (((MenuItemImpl) item).requestsActionButton()) {
                    MenuItemCompat.setIconTintList(item, ColorStateList
                            .valueOf(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor()));
                }
                Utils.setTitleWithCustomFontToMenuItem(mActivity.typeface, item, null);
            }
        }
        return true;
    }

    private void showErrorView(String postId) {
        binding.swipeRefreshLayoutViewPostDetailFragment.setRefreshing(false);
        binding.fetchPostInfoLinearLayoutViewPostDetailFragment.setVisibility(View.VISIBLE);
        binding.fetchPostInfoLinearLayoutViewPostDetailFragment.setOnClickListener(view -> viewPostDetailFragmentViewModel.fetchPostAndCommentsById(postId));
        binding.fetchPostInfoTextViewViewPostDetailFragment.setText(R.string.load_post_error);
        mGlide.load(R.drawable.error_image).into(binding.fetchPostInfoImageViewViewPostDetailFragment);
    }

    private void showMessage(int resId) {
        if (showToast) {
            Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT).show();
        } else {
            mActivity.showSnackBar(resId);
        }
    }

    private boolean showSensitiveWarning() {
        if (mPost != null && mPost.isNSFW()
                && (mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false)
                || !mNsfwAndSpoilerSharedPreferences.getBoolean((mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : (mActivity.accountName)) + SharedPreferencesUtils.NSFW_BASE, false))) {
            MaterialAlertDialogBuilder sensitiveWarningBuilder = new MaterialAlertDialogBuilder(mActivity, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.this_post_contains_sensitive_content)
                    .setPositiveButton(R.string.leave, (dialogInterface, i)
                            -> {
                        mActivity.finish();
                    })
                    .setCancelable(false);
            sensitiveWarningBuilder.show();
            return true;
        }

        return false;
    }

    public void deleteComment(String fullName, int position) {
        new MaterialAlertDialogBuilder(mActivity, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete_this_comment)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> viewPostDetailFragmentViewModel.deleteComment(fullName, position))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void toggleReplyNotifications(Comment comment, int position) {
        viewPostDetailFragmentViewModel.toggleNotification(comment, position);
    }

    public void changeToNormalThreadMode() {
        viewPostDetailFragmentViewModel.clearSingleCommentId(mSharedPreferences.getBoolean(SharedPreferencesUtils.RESPECT_SUBREDDIT_RECOMMENDED_COMMENT_SORT_TYPE, false));
    }

    public void scrollToNextParentComment() {
        RecyclerView recyclerView = mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView;
        LinearLayoutManagerBugFixed layoutManager = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager());
        if (mCommentsAdapter == null || layoutManager == null) {
            return;
        }

        int currentPosition = ConcatAdapterKt.getLocalPosition(
                mConcatAdapter, mCommentsAdapter, layoutManager.findFirstVisibleItemPosition()
        );
        if (currentPosition < 0) {
            currentPosition = 0;
        }

        int nextParentPosition = viewPostDetailFragmentViewModel.getNextParentCommentPosition(currentPosition);
        if (nextParentPosition < 0) {
            return;
        }

        int absoluteParentPosition = ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, nextParentPosition);
        if (absoluteParentPosition < 0) {
            return;
        }

        mSmoothScroller.setTargetPosition(absoluteParentPosition);
        mIsSmoothScrolling = true;
        recyclerView.getLayoutManager().startSmoothScroll(mSmoothScroller);
    }

    public void scrollToPreviousParentComment() {
        RecyclerView recyclerView = mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView;
        LinearLayoutManagerBugFixed layoutManager = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager());
        if (mCommentsAdapter == null || layoutManager == null) {
            return;
        }

        int currentPosition = ConcatAdapterKt.getLocalPosition(
                mConcatAdapter, mCommentsAdapter, layoutManager.findFirstVisibleItemPosition()
        );
        if (currentPosition < 0) {
            currentPosition = 0;
        }

        int previousParentPosition = viewPostDetailFragmentViewModel.getPreviousParentCommentPosition(currentPosition);
        if (previousParentPosition < 0) {
            return;
        }

        int absoluteParentPosition = ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, previousParentPosition);
        if (absoluteParentPosition < 0) {
            return;
        }

        mSmoothScroller.setTargetPosition(absoluteParentPosition);
        mIsSmoothScrolling = true;
        recyclerView.getLayoutManager().startSmoothScroll(mSmoothScroller);
    }

    public void scrollToParentComment(int position, int currentDepth) {
        RecyclerView recyclerView = mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (mCommentsAdapter == null || layoutManager == null) {
            return;
        }

        int parentPosition = viewPostDetailFragmentViewModel.getParentCommentPosition(position, currentDepth);
        if (parentPosition < 0) {
            return;
        }

        int absoluteParentPosition = ConcatAdapterKt.getAbsolutePosition(mConcatAdapter, mCommentsAdapter, parentPosition);
        if (absoluteParentPosition < 0) {
            return;
        }

        mSmoothScroller.setTargetPosition(absoluteParentPosition);
        mIsSmoothScrolling = true;
        layoutManager.startSmoothScroll(mSmoothScroller);
    }

    public void delayTransition() {
        TransitionManager.beginDelayedTransition((mCommentsRecyclerView == null ? binding.postDetailRecyclerViewViewPostDetailFragment : mCommentsRecyclerView), new AutoTransition());
    }

    public boolean getIsNsfwSubreddit() {
        if (mActivity != null) {
            return mActivity.isNsfwSubreddit();
        }
        return false;
    }

    public int getPostListPosition() {
        return postListPosition;
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostDetailFragment event) {
        if (mPost.getId().equals(event.post.getId())) {
            Post updatedPost = new Post(event.post);

            viewPostDetailFragmentViewModel.setPost(updatedPost);
        }
    }

    @Subscribe
    public void onChangeNSFWBlurEvent(ChangeNSFWBlurEvent event) {
        if (mPostAdapter != null) {
            mPostAdapter.setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(event.needBlurNSFW, event.doNotBlurNsfwInNsfwSubreddits);
        }
        if (mCommentsRecyclerView != null) {
            refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
        } else {
            refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
        }
    }

    @Subscribe
    public void onChangeSpoilerBlurEvent(ChangeSpoilerBlurEvent event) {
        if (mPostAdapter != null) {
            mPostAdapter.setBlurSpoiler(event.needBlurSpoiler);
        }
        if (mCommentsRecyclerView != null) {
            refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
        } else {
            refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
        }
    }

    private void refreshAdapter(RecyclerView recyclerView) {
        int previousPosition = -1;
        if (recyclerView.getLayoutManager() != null) {
            previousPosition = ((LinearLayoutManagerBugFixed) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
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
        String autoplay = mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
        String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        boolean stateChanged = false;
        if (autoplay.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
            if (mPostAdapter != null) {
                mPostAdapter.setAutoplay(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_WIFI);
            }
            stateChanged = true;
        }
        if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            if (mPostAdapter != null) {
                mPostAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
            }
            if (mCommentsAdapter != null) {
                mCommentsAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
            }
            stateChanged = true;
        }

        if (stateChanged) {
            if (mCommentsRecyclerView == null) {
                refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
            } else {
                if (mPostAdapter != null) {
                    refreshAdapter(binding.postDetailRecyclerViewViewPostDetailFragment);
                }
                refreshAdapter(mCommentsRecyclerView);
            }
        }
    }

    @Subscribe
    public void onFlairSelectedEvent(FlairSelectedEvent event) {
        if (event.viewPostDetailFragmentId == viewPostDetailFragmentId) {
            viewPostDetailFragmentViewModel.changeFlair(event.flair, postListPosition);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (ViewPostDetailActivity) context;
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayoutViewPostDetailFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutViewPostDetailFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchPostInfoTextViewViewPostDetailFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.fetchPostInfoTextViewViewPostDetailFragment.setTypeface(mActivity.contentTypeface);
        }
    }

    private void onWindowFocusChanged(boolean hasWindowsFocus) {
        if (mPostAdapter != null) {
            mPostAdapter.setCanPlayVideo(hasWindowsFocus);
        }
    }

    @Override
    public void approvePost(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.approvePost(post, position);
    }

    @Override
    public void removePost(@NonNull Post post, int position, boolean isSpam) {
        viewPostDetailFragmentViewModel.removePost(post, position, isSpam);
    }

    @Override
    public void toggleSticky(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleSticky(post, position);
    }

    @Override
    public void toggleLock(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleLock(post, position);
    }

    @Override
    public void toggleNSFW(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleNSFW(position);
    }

    @Override
    public void toggleSpoiler(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleSpoiler(position);
    }

    @Override
    public void toggleMod(@NonNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleMod(post, position);
    }

    @Override
    public void toggleNotification(@NotNull Post post, int position) {
        viewPostDetailFragmentViewModel.toggleNotification(post, position);
    }

    @Override
    public void approveComment(@NonNull Comment comment, int position) {
        viewPostDetailFragmentViewModel.approveComment(comment, position);
    }

    @Override
    public void removeComment(@NonNull Comment comment, int position, boolean isSpam) {
        viewPostDetailFragmentViewModel.removeComment(comment, position, isSpam);
    }

    @Override
    public void toggleLock(@NonNull Comment comment, int position) {
        viewPostDetailFragmentViewModel.toggleLock(comment, position);
    }
}
