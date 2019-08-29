package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements FragmentCommunicator {

    static final String EXTRA_NAME = "EN";
    static final String EXTRA_USER_NAME = "EUN";
    static final String EXTRA_USER_WHERE = "EUW";
    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_POST_TYPE = "EPT";
    static final String EXTRA_SORT_TYPE = "EST";
    static final String EXTRA_FILTER = "EF";
    static final int EXTRA_NO_FILTER = -1;
    static final String EXTRA_ACCESS_TOKEN = "EAT";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String RECYCLER_VIEW_POSITION_STATE = "RVPS";

    @BindView(R.id.recycler_view_post_fragment) RecyclerView mPostRecyclerView;
    @BindView(R.id.progress_bar_post_fragment) CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_post_info_linear_layout_post_fragment) LinearLayout mFetchPostInfoLinearLayout;
    @BindView(R.id.fetch_post_info_image_view_post_fragment) ImageView mFetchPostInfoImageView;
    @BindView(R.id.fetch_post_info_text_view_post_fragment) TextView mFetchPostInfoTextView;

    private RequestManager mGlide;

    private Activity activity;
    private LinearLayoutManager mLinearLayoutManager;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    private boolean isInLazyMode = false;
    private boolean isLazyModePaused = false;
    private boolean hasPost = false;

    private PostRecyclerViewAdapter mAdapter;
    private RecyclerView.SmoothScroller smoothScroller;

    PostViewModel mPostViewModel;

    private Window window;
    private Handler lazyModeHandler;
    private LazyModeRunnable lazyModeRunnable;
    private CountDownTimer resumeLazyModeCountDownTimer;

    private float lazyModeInterval;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPostRecyclerView.getAdapter() != null) {
            ((PostRecyclerViewAdapter) mPostRecyclerView.getAdapter()).setCanStartActivity(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    mPostRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
                }
            }
        }

        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !resources.getBoolean(R.bool.isTablet)) {
            mLinearLayoutManager = new LinearLayoutManager(activity);
            mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mPostRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset);
            mPostRecyclerView.addItemDecoration(itemDecoration);
        }

        mGlide = Glide.with(activity);

        lazyModeRunnable = new LazyModeRunnable() {

            @Override
            public void run() {
                if(isInLazyMode && !isLazyModePaused) {
                    int nPosts = mAdapter.getItemCount();
                    if(getCurrentPosition() == -1) {
                        if(mLinearLayoutManager != null) {
                            setCurrentPosition(mLinearLayoutManager.findFirstVisibleItemPosition());
                        } else {
                            int[] into = new int[2];
                            setCurrentPosition(mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[1]);
                        }
                    }

                    if(getCurrentPosition() != RecyclerView.NO_POSITION && nPosts > getCurrentPosition()) {
                        incrementCurrentPosition();
                        smoothScroller.setTargetPosition(getCurrentPosition());
                        if(mLinearLayoutManager != null) {
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

        if(savedInstanceState != null) {
            int recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_STATE);
            if(recyclerViewPosition > 0) {
                mPostRecyclerView.scrollToPosition(recyclerViewPosition);
            }

            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            if(isInLazyMode) {
                resumeLazyMode(false);
            }
        }

        mPostRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if(isInLazyMode) {
                pauseLazyMode(true);
            }
            return false;
        });

        if(activity instanceof MainActivity) {
            mPostRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((MainActivity) activity).postScrollDown();
                    } else if (dy < 0) {
                        ((MainActivity) activity).postScrollUp();
                    }

                }
            });
        } else if(activity instanceof ViewSubredditDetailActivity) {
            mPostRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        ((ViewSubredditDetailActivity) activity).postScrollDown();
                    } else if (dy < 0) {
                        ((ViewSubredditDetailActivity) activity).postScrollUp();
                    }

                }
            });
        }

        int postType = getArguments().getInt(EXTRA_POST_TYPE);
        String sortType = getArguments().getString(EXTRA_SORT_TYPE);
        int filter = getArguments().getInt(EXTRA_FILTER);
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        boolean nsfw = mSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_KEY, false);

        PostViewModel.Factory factory;

        if(postType == PostDataSource.TYPE_SEARCH) {
            String subredditName = getArguments().getString(EXTRA_NAME);
            String query = getArguments().getString(EXTRA_QUERY);

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    accessToken, postType, true, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void retryLoadingMore() {
                    mPostViewModel.retryLoadingMore();
                }

                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredThingActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, sortType);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                    startActivity(intent);
                }
            });

            if(accessToken == null) {
                factory = new PostViewModel.Factory(mRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, query, postType,
                        sortType, filter, nsfw);
            } else {
                factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, query, postType,
                        sortType, filter, nsfw);
            }
        } else if(postType == PostDataSource.TYPE_SUBREDDIT) {
            String subredditName = getArguments().getString(EXTRA_NAME);

            boolean displaySubredditName = subredditName.equals("popular") || subredditName.equals("all");
            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    accessToken, postType, displaySubredditName, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void retryLoadingMore() {
                    mPostViewModel.retryLoadingMore();
                }

                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, sortType);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                    startActivity(intent);
                }
            });

            if(accessToken == null) {
                factory = new PostViewModel.Factory(mRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, postType, sortType,
                        filter, nsfw);
            } else {
                factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, subredditName, postType, sortType,
                        filter, nsfw);
            }
        } else if(postType == PostDataSource.TYPE_USER) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFetchPostInfoLinearLayout.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mFetchPostInfoLinearLayout.setLayoutParams(params);

            String username = getArguments().getString(EXTRA_USER_NAME);
            String where = getArguments().getString(EXTRA_USER_WHERE);

            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    accessToken, postType, true, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void retryLoadingMore() {
                    mPostViewModel.retryLoadingMore();
                }

                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, sortType);
                    intent.putExtra(FilteredThingActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                    startActivity(intent);
                }
            });

            if(accessToken == null) {
                factory = new PostViewModel.Factory(mRetrofit, accessToken,
                        getResources().getConfiguration().locale, username, postType, sortType, where,
                        filter, nsfw);
            } else {
                factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                        getResources().getConfiguration().locale, username, postType, sortType, where,
                        filter, nsfw);
            }
        } else {
            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit, mRetrofit, mRedditDataRoomDatabase,
                    accessToken, postType, true, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void retryLoadingMore() {
                    mPostViewModel.retryLoadingMore();
                }

                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, activity.getString(R.string.best));
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, sortType);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, filter);
                    startActivity(intent);
                }
            });

            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, postType, sortType, filter, nsfw);
        }

        mPostRecyclerView.setAdapter(mAdapter);

        mPostViewModel = new ViewModelProvider(this, factory).get(PostViewModel.class);
        mPostViewModel.getPosts().observe(this, posts -> mAdapter.submitList(posts));

        mPostViewModel.getInitialLoadingState().observe(this, networkState -> {
            if(networkState.getStatus().equals(NetworkState.Status.SUCCESS)) {
                mProgressBar.setVisibility(View.GONE);
            } else if(networkState.getStatus().equals(NetworkState.Status.FAILED)) {
                mFetchPostInfoLinearLayout.setOnClickListener(view -> mPostViewModel.retry());
                showErrorView(R.string.load_posts_error);
            } else {
                mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mPostViewModel.hasPost().observe(this, hasPost -> {
            this.hasPost = hasPost;
            if(hasPost) {
                mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            } else {
                if(isInLazyMode) {
                    stopLazyMode();
                }

                showErrorView(R.string.no_posts);
            }
        });

        mPostViewModel.getPaginationNetworkState().observe(this, networkState -> mAdapter.setNetworkState(networkState));

        return rootView;
    }

    void changeSortType(String sortType) {
        mPostViewModel.changeSortType(sortType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        if(mLinearLayoutManager != null) {
            outState.putInt(RECYCLER_VIEW_POSITION_STATE, mLinearLayoutManager.findFirstVisibleItemPosition());
        } else if(mStaggeredGridLayoutManager != null) {
            int[] into = new int[2];
            outState.putInt(RECYCLER_VIEW_POSITION_STATE,
                    mStaggeredGridLayoutManager.findFirstVisibleItemPositions(into)[0]);
        }
    }

    @Override
    public void refresh() {
        if(isInLazyMode) {
            stopLazyMode();
        }

        hasPost = false;
        mPostViewModel.refresh();
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(activity != null && isAdded()) {
            mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchPostInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(mFetchPostInfoImageView);
        }
    }

    @Override
    public void changeNSFW(boolean nsfw) {
        mPostViewModel.changeNSFW(nsfw);
    }

    @Override
    public boolean startLazyMode() {
        if(!hasPost) {
            Toast.makeText(activity, R.string.no_posts_no_lazy_mode, Toast.LENGTH_SHORT).show();
            return false;
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
        isInLazyMode = false;
        isLazyModePaused = false;
        lazyModeHandler.removeCallbacks(lazyModeRunnable);
        resumeLazyModeCountDownTimer.cancel();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_stop), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumeLazyMode(boolean resumeNow) {
        if(isInLazyMode) {
            isLazyModePaused = false;
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            lazyModeRunnable.resetOldPosition();

            if(resumeNow) {
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

        if(startTimer) {
            resumeLazyModeCountDownTimer.start();
        }
    }

    @Override
    public boolean isInLazyMode() {
        return isInLazyMode;
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostList event) {
        PagedList<Post> posts = mAdapter.getCurrentList();
        if (posts != null && event.positionInList >= 0 && event.positionInList < posts.size()) {
            Post post = posts.get(event.positionInList);
            if (post != null && post.getFullName().equals(event.post.getFullName())) {
                post.setTitle(event.post.getTitle());
                post.setVoteType(event.post.getVoteType());
                post.setScore(event.post.getScore());
                post.setNSFW(event.post.isNSFW());
                post.setSpoiler(event.post.isSpoiler());
                post.setFlair(event.post.getFlair());
                post.setSaved(event.post.isSaved());
                mAdapter.notifyItemChanged(event.positionInList);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isInLazyMode && isLazyModePaused) {
            resumeLazyMode(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isInLazyMode) {
            pauseLazyMode(false);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

        private int mItemOffset;

        StaggeredGridLayoutManagerItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        StaggeredGridLayoutManagerItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            int spanIndex = layoutParams.getSpanIndex();

            int halfOffset = mItemOffset / 2;

            if(spanIndex == 0) {
                outRect.set(0, 0, halfOffset, 0);
            } else {
                outRect.set(halfOffset, 0, 0, 0);
            }
        }
    }
}