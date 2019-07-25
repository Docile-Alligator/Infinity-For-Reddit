package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

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

    static final String EXTRA_SUBREDDIT_NAME = "EN";
    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_POST_TYPE = "EPT";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";

    @BindView(R.id.coordinator_layout_post_fragment) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view_post_fragment) RecyclerView mPostRecyclerView;
    @BindView(R.id.progress_bar_post_fragment) CircleProgressBar mProgressBar;
    @BindView(R.id.fetch_post_info_linear_layout_post_fragment) LinearLayout mFetchPostInfoLinearLayout;
    @BindView(R.id.fetch_post_info_image_view_post_fragment) ImageView mFetchPostInfoImageView;
    @BindView(R.id.fetch_post_info_text_view_post_fragment) TextView mFetchPostInfoTextView;

    private RequestManager mGlide;

    private Activity activity;
    private LinearLayoutManager mLinearLayoutManager;

    private boolean isInLazyMode = false;
    private boolean isLazyModePaused = false;

    private PostRecyclerViewAdapter mAdapter;
    private RecyclerView.SmoothScroller smoothScroller;

    PostViewModel mPostViewModel;

    private Window window;
    private Handler lazyModeHandler;
    private Runnable lazyModeRunnable;
    private CountDownTimer resumeLazyModeCountDownTimer;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject @Named("auth_info")
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

        ((Infinity) activity.getApplication()).getmAppComponent().inject(this);

        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

        lazyModeHandler = new Handler();

        smoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        window = activity.getWindow();

        mGlide = Glide.with(activity);

        lazyModeRunnable = new Runnable() {
            @Override
            public void run() {
                if(isInLazyMode && !isLazyModePaused) {
                    int nPosts = mAdapter.getItemCount();
                    int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                    if(firstVisiblePosition != RecyclerView.NO_POSITION && nPosts > firstVisiblePosition) {
                        smoothScroller.setTargetPosition(firstVisiblePosition + 1);
                        mLinearLayoutManager.startSmoothScroll(smoothScroller);
                    }
                }
                lazyModeHandler.postDelayed(this, 2500);
            }
        };

        resumeLazyModeCountDownTimer = new CountDownTimer(2500, 2500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                resumeLazyMode(true);
            }
        };

        if(savedInstanceState != null) {
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            if(isInLazyMode) {
                resumeLazyMode(false);
            }
        }

        mLinearLayoutManager = new LinearLayoutManager(activity);
        mPostRecyclerView.setLayoutManager(mLinearLayoutManager);
        mPostRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if(isInLazyMode) {
                pauseLazyMode(true);
            }
            return false;
        });

        int postType = getArguments().getInt(EXTRA_POST_TYPE);

        String accessToken = activity.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        PostViewModel.Factory factory;

        if(postType == PostDataSource.TYPE_SEARCH) {
            String subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);
            String query = getArguments().getString(EXTRA_QUERY);

            mAdapter = new PostRecyclerViewAdapter(activity, mRetrofit,
                    mSharedPreferences, postType, () -> mPostViewModel.retryLoadingMore());

            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, subredditName, query, postType, new PostDataSource.OnPostFetchedCallback() {
                @Override
                public void hasPost() {
                    mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void noPost() {
                    mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                        //Do nothing
                    });
                    showErrorView(R.string.no_posts);
                }
            });
        } else if(postType != PostDataSource.TYPE_FRONT_PAGE) {
            if(postType == PostDataSource.TYPE_USER) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFetchPostInfoLinearLayout.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mFetchPostInfoLinearLayout.setLayoutParams(params);
            }

            String subredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);

            mAdapter = new PostRecyclerViewAdapter(activity, mRetrofit,
                    mSharedPreferences, postType, () -> mPostViewModel.retryLoadingMore());

            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, subredditName, postType, new PostDataSource.OnPostFetchedCallback() {
                @Override
                public void hasPost() {
                    mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void noPost() {
                    mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                        //Do nothing
                    });
                    showErrorView(R.string.no_posts);
                }
            });
        } else {
            mAdapter = new PostRecyclerViewAdapter(activity, mOauthRetrofit,
                    mSharedPreferences, postType, () -> mPostViewModel.retryLoadingMore());

            factory = new PostViewModel.Factory(mOauthRetrofit, accessToken,
                    getResources().getConfiguration().locale, postType, new PostDataSource.OnPostFetchedCallback() {
                @Override
                public void hasPost() {
                    mFetchPostInfoLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void noPost() {
                    mFetchPostInfoLinearLayout.setOnClickListener(view -> {
                        //Do nothing
                    });
                    showErrorView(R.string.no_posts);
                }
            });
        }

        mPostRecyclerView.setAdapter(mAdapter);

        mPostViewModel = ViewModelProviders.of(this, factory).get(PostViewModel.class);
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

        mPostViewModel.getPaginationNetworkState().observe(this, networkState -> {
            mAdapter.setNetworkState(networkState);
        });

        return rootView;
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
    }

    @Override
    public void refresh() {
        mPostViewModel.refresh();
    }

    private void showErrorView(int stringResId) {
        mProgressBar.setVisibility(View.GONE);
        if(activity != null && isAdded()) {
            mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
            mFetchPostInfoTextView.setText(stringResId);
            mGlide.load(R.drawable.load_post_error_indicator).into(mFetchPostInfoImageView);
        }
    }

    @Override
    public void startLazyMode() {
        isInLazyMode = true;
        isLazyModePaused = false;
        lazyModeHandler.postDelayed(lazyModeRunnable, 2500);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(activity, getString(R.string.lazy_mode_start, 2.5), Toast.LENGTH_SHORT).show();
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
            if(resumeNow) {
                lazyModeHandler.post(lazyModeRunnable);
            } else {
                lazyModeHandler.postDelayed(lazyModeRunnable, 2500);
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

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToPostList event) {
        Post post = mAdapter.getCurrentList().get(event.positionInList);
        if(post != null) {
            post.setTitle(event.post.getTitle());
            post.setVoteType(event.post.getVoteType());
            post.setScore(event.post.getScore());
            mAdapter.notifyItemChanged(event.positionInList);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isInLazyMode) {
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
}