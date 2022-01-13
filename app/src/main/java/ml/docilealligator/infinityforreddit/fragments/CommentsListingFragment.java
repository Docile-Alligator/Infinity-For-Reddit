package ml.docilealligator.infinityforreddit.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.CommentsListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.comment.CommentViewModel;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_USERNAME = "EN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ARE_SAVED_COMMENTS = "EISC";

    @BindView(R.id.coordinator_layout_comments_listing_fragment)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swipe_refresh_layout_view_comments_listing_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_comments_listing_fragment)
    RecyclerView mCommentRecyclerView;
    @BindView(R.id.fetch_comments_info_linear_layout_comments_listing_fragment)
    LinearLayout mFetchCommentInfoLinearLayout;
    @BindView(R.id.fetch_comments_info_image_view_comments_listing_fragment)
    ImageView mFetchCommentInfoImageView;
    @BindView(R.id.fetch_comments_info_text_view_comments_listing_fragment)
    TextView mFetchCommentInfoTextView;
    CommentViewModel mCommentViewModel;
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
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private RequestManager mGlide;
    private BaseActivity mActivity;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private CommentsListingRecyclerViewAdapter mAdapter;
    private SortType sortType;
    private ColorDrawable backgroundSwipeRight;
    private ColorDrawable backgroundSwipeLeft;
    private Drawable drawableSwipeRight;
    private Drawable drawableSwipeLeft;
    private int swipeLeftAction;
    private int swipeRightAction;
    private float swipeActionThreshold;
    private ItemTouchHelper touchHelper;

    public CommentsListingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments_listing, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        applyTheme();

        mGlide = Glide.with(mActivity);

        Resources resources = getResources();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            mCommentRecyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mCommentRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
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
                if (!(viewHolder instanceof CommentsListingRecyclerViewAdapter.CommentViewHolder)) {
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
                    touchHelper.attachToRecyclerView(mCommentRecyclerView);
                    if (mAdapter != null) {
                        mAdapter.onItemSwipe(viewHolder, direction, swipeLeftAction, swipeRightAction);
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int horizontalOffset = (int) Utils.convertDpToPixel(16, mActivity);
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
            touchHelper.attachToRecyclerView(mCommentRecyclerView);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        new Handler().postDelayed(() -> bindView(resources), 0);

        return rootView;
    }

    private void bindView(Resources resources) {
        if (mActivity != null && !mActivity.isFinishing() && !mActivity.isDestroyed()) {
            mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
            mCommentRecyclerView.setLayoutManager(mLinearLayoutManager);

            mAdapter = new CommentsListingRecyclerViewAdapter(mActivity, mOauthRetrofit, customThemeWrapper,
                    getResources().getConfiguration().locale, mSharedPreferences,
                    getArguments().getString(EXTRA_ACCESS_TOKEN), getArguments().getString(EXTRA_ACCOUNT_NAME),
                    () -> mCommentViewModel.retryLoadingMore());

            String username = getArguments().getString(EXTRA_USERNAME);
            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_USER_COMMENT, SortType.Type.NEW.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_USER_COMMENT, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()), SortType.Time.valueOf(sortTime.toUpperCase()));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort.toUpperCase()));
            }

            mCommentRecyclerView.setAdapter(mAdapter);

            if (mActivity instanceof RecyclerViewContentScrollingInterface) {
                mCommentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

            CommentViewModel.Factory factory;

            if (mAccessToken == null) {
                factory = new CommentViewModel.Factory(mRetrofit,
                        resources.getConfiguration().locale, null, username, sortType,
                        getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
            } else {
                factory = new CommentViewModel.Factory(mOauthRetrofit,
                        resources.getConfiguration().locale, mAccessToken, username, sortType,
                        getArguments().getBoolean(EXTRA_ARE_SAVED_COMMENTS));
            }

            mCommentViewModel = new ViewModelProvider(this, factory).get(CommentViewModel.class);
            mCommentViewModel.getComments().observe(getViewLifecycleOwner(), comments -> mAdapter.submitList(comments));

            mCommentViewModel.hasComment().observe(getViewLifecycleOwner(), hasComment -> {
                mSwipeRefreshLayout.setRefreshing(false);
                if (hasComment) {
                    mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
                } else {
                    mFetchCommentInfoLinearLayout.setOnClickListener(null);
                    showErrorView(R.string.no_comments);
                }
            });

            mCommentViewModel.getInitialLoadingState().observe(getViewLifecycleOwner(), networkState -> {
                if (networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else if (networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mFetchCommentInfoLinearLayout.setOnClickListener(view -> refresh());
                    showErrorView(R.string.load_comments_failed);
                } else {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            mCommentViewModel.getPaginationNetworkState().observe(getViewLifecycleOwner(), networkState -> mAdapter.setNetworkState(networkState));

            mSwipeRefreshLayout.setOnRefreshListener(() -> mCommentViewModel.refresh());
        }
    }

    public void changeSortType(SortType sortType) {
        mCommentViewModel.changeSortType(sortType);
        this.sortType = sortType;
    }

    private void initializeSwipeActionDrawable() {
        if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
            backgroundSwipeRight = new ColorDrawable(customThemeWrapper.getDownvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        } else {
            backgroundSwipeRight = new ColorDrawable(customThemeWrapper.getUpvoted());
            drawableSwipeRight = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        }

        if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
            backgroundSwipeLeft = new ColorDrawable(customThemeWrapper.getUpvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_upward_black_24dp, null);
        } else {
            backgroundSwipeLeft = new ColorDrawable(customThemeWrapper.getDownvoted());
            drawableSwipeLeft = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_arrow_downward_black_24dp, null);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (BaseActivity) context;
    }

    @Override
    public void refresh() {
        mFetchCommentInfoLinearLayout.setVisibility(View.GONE);
        mCommentViewModel.refresh();
        mAdapter.setNetworkState(null);
    }

    @Override
    public void applyTheme() {
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(customThemeWrapper.getCircularProgressBarBackground());
        mSwipeRefreshLayout.setColorSchemeColors(customThemeWrapper.getColorAccent());
        mFetchCommentInfoTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            mFetchCommentInfoTextView.setTypeface(mActivity.typeface);
        }
    }

    private void showErrorView(int stringResId) {
        if (mActivity != null && isAdded()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mFetchCommentInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchCommentInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(mFetchCommentInfoImageView);
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

    public void giveAward(String awardsHTML, int position) {
        if (mAdapter != null) {
            mAdapter.giveAward(awardsHTML, position);
        }
    }

    public void editComment(String commentMarkdown, int position) {
        if (mAdapter != null) {
            mAdapter.editComment(commentMarkdown, position);
        }
    }
}
