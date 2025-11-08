package ml.docilealligator.infinityforreddit.fragments;

import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.INDEX_UNSET;
import static ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo.TIME_UNSET;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.FetchPostFilterAndConcatenatedSubredditNames;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.PostModerationActionHandler;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.AccountPostsActivity;
import ml.docilealligator.infinityforreddit.activities.AccountSavedThingActivity;
import ml.docilealligator.infinityforreddit.activities.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.activities.CustomizePostFilterActivity;
import ml.docilealligator.infinityforreddit.activities.FilteredPostsActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.Paging3LoadingStateAdapter;
import ml.docilealligator.infinityforreddit.adapters.PostRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostBinding;
import ml.docilealligator.infinityforreddit.events.ChangeDefaultPostLayoutEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSavePostFeedScrolledPositionEvent;
import ml.docilealligator.infinityforreddit.events.NeedForPostListFromPostFragmentEvent;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.events.ProvidePostListToViewPostDetailActivityEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.post.PostViewModel;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.readpost.ReadPostsList;
import ml.docilealligator.infinityforreddit.readpost.ReadPostsListInterface;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.media.VolumeInfo;
import retrofit2.Retrofit;


/**
 * A simple {@link PostFragmentBase} subclass.
 */
public class PostFragment extends PostFragmentBase implements FragmentCommunicator, PostModerationActionHandler {

    public static final String EXTRA_NAME = "EN";
    public static final String EXTRA_USER_NAME = "EUN";
    public static final String EXTRA_USER_WHERE = "EUW";
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_TRENDING_SOURCE = "ETS";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_FILTER = "EF";
    public static final String EXTRA_DISABLE_READ_POSTS = "EDRP";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String RECYCLER_VIEW_POSITION_STATE = "RVPS";
    private static final String POST_FILTER_STATE = "PFS";
    private static final String CONCATENATED_SUBREDDIT_NAMES_STATE = "CSNS";
    private static final String POST_FRAGMENT_ID_STATE = "PFIS";

    PostViewModel mPostViewModel;
    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;
    @Inject
    Provider<StreamableAPI> mStreamableApiProvider;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
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
    private int postType;
    private boolean savePostFeedScrolledPosition;
    private PostRecyclerViewAdapter mAdapter;
    private String subredditName;
    private String username;
    private String query;
    private String trendingSource;
    private String where;
    private String multiRedditPath;
    private String concatenatedSubredditNames;
    private int maxPosition = -1;
    private SortType sortType;
    private PostFilter postFilter;
    private ReadPostsListInterface readPostsList;
    private FragmentPostBinding binding;

    public PostFragment() {
        // Required empty public constructor
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
        if (mAdapter != null && binding.recyclerViewPostFragment != null) {
            binding.recyclerViewPostFragment.onWindowVisibilityChanged(View.VISIBLE);
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostBinding.inflate(inflater, container, false);

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
                    Log.i("asfsaf", "s " + postFragmentId);
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        binding.recyclerViewPostFragment.addOnWindowFocusChangedListener(this::onWindowFocusChanged);

        Resources resources = getResources();

        binding.swipeRefreshLayoutPostFragment.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        binding.swipeRefreshLayoutPostFragment.setOnRefreshListener(this::refresh);

        int recyclerViewPosition = 0;
        if (savedInstanceState != null) {
            recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_STATE);

            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            postFilter = savedInstanceState.getParcelable(POST_FILTER_STATE);
            concatenatedSubredditNames = savedInstanceState.getString(CONCATENATED_SUBREDDIT_NAMES_STATE);
            postFragmentId = savedInstanceState.getLong(POST_FRAGMENT_ID_STATE);
        } else {
            postFilter = getArguments().getParcelable(EXTRA_FILTER);
            postFragmentId = System.currentTimeMillis() + new Random().nextInt(1000);
        }

        readPostsList = new ReadPostsList(mRedditDataRoomDatabase.readPostDao(), activity.accountName,
                getArguments().getBoolean(EXTRA_DISABLE_READ_POSTS, false));

        if (activity instanceof RecyclerViewContentScrollingInterface) {
            binding.recyclerViewPostFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        postType = getArguments().getInt(EXTRA_POST_TYPE);

        int defaultPostLayout = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY, "0"));
        savePostFeedScrolledPosition = mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION, false);
        Locale locale = resources.getConfiguration().locale;

        int usage;
        String nameOfUsage;

        if (postType == PostPagingSource.TYPE_SEARCH) {
            subredditName = getArguments().getString(EXTRA_NAME);
            query = getArguments().getString(EXTRA_QUERY);
            trendingSource = getArguments().getString(EXTRA_TRENDING_SOURCE);
            if (savedInstanceState == null) {
                postFragmentId += query.hashCode();
            }

            usage = PostFilterUsage.SEARCH_TYPE;
            nameOfUsage = PostFilterUsage.NO_USAGE;

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SEARCH_POST, SortType.Type.RELEVANCE.name());
            String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_SEARCH_POST, SortType.Time.ALL.name());
            sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_TRENDING_SOURCE, trendingSource);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_TRENDING_SOURCE, trendingSource);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_TRENDING_SOURCE, trendingSource);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else if (postType == PostPagingSource.TYPE_SUBREDDIT) {
            subredditName = getArguments().getString(EXTRA_NAME);
            if (savedInstanceState == null) {
                postFragmentId += subredditName.hashCode();
            }

            usage = PostFilterUsage.SUBREDDIT_TYPE;
            nameOfUsage = subredditName;

            String sort;
            String sortTime = null;

            sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + subredditName,
                    mSharedPreferences.getString(SharedPreferencesUtils.SUBREDDIT_DEFAULT_SORT_TYPE, SortType.Type.HOT.name()));
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + subredditName,
                        mSharedPreferences.getString(SharedPreferencesUtils.SUBREDDIT_DEFAULT_SORT_TIME, SortType.Time.ALL.name()));
            }
            boolean displaySubredditName = subredditName != null && (subredditName.equals("popular") || subredditName.equals("all"));
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, defaultPostLayout);

            if (sortTime != null) {
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, displaySubredditName,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else if (postType == PostPagingSource.TYPE_MULTI_REDDIT) {
            multiRedditPath = getArguments().getString(EXTRA_NAME);
            query = getArguments().getString(EXTRA_QUERY);
            if (savedInstanceState == null) {
                postFragmentId += multiRedditPath.hashCode() + (query == null ? 0 : query.hashCode());
            }

            usage = PostFilterUsage.MULTIREDDIT_TYPE;
            nameOfUsage = multiRedditPath;

            String sort;
            String sortTime = null;

            sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_MULTI_REDDIT_POST_BASE + multiRedditPath,
                    SortType.Type.HOT.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_MULTI_REDDIT_POST_BASE + multiRedditPath,
                        SortType.Time.ALL.name());
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiRedditPath,
                    defaultPostLayout);

            if (sortTime != null) {
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else if (postType == PostPagingSource.TYPE_USER) {
            username = getArguments().getString(EXTRA_USER_NAME);
            where = getArguments().getString(EXTRA_USER_WHERE);
            if (savedInstanceState == null) {
                postFragmentId += username.hashCode();
            }

            usage = PostFilterUsage.USER_TYPE;
            nameOfUsage = username;

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_USER_POST_BASE + username,
                    mSharedPreferences.getString(SharedPreferencesUtils.USER_DEFAULT_SORT_TYPE, SortType.Type.NEW.name()));
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_USER_POST_BASE + username,
                        mSharedPreferences.getString(SharedPreferencesUtils.USER_DEFAULT_SORT_TIME, SortType.Time.ALL.name()));
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + username, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else if (postType == PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE) {
            usage = PostFilterUsage.HOME_TYPE;
            nameOfUsage = PostFilterUsage.NO_USAGE;

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + Account.ANONYMOUS_ACCOUNT, SortType.Type.HOT.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + Account.ANONYMOUS_ACCOUNT, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else if (postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
            multiRedditPath = getArguments().getString(EXTRA_NAME);
            if (savedInstanceState == null) {
                postFragmentId += multiRedditPath.hashCode();
            }

            usage = PostFilterUsage.MULTIREDDIT_TYPE;
            nameOfUsage = multiRedditPath;

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_MULTI_REDDIT_POST_BASE + multiRedditPath, SortType.Type.HOT.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_MULTI_REDDIT_POST_BASE + multiRedditPath, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }

            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiRedditPath, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        } else {
            usage = PostFilterUsage.HOME_TYPE;
            nameOfUsage = PostFilterUsage.NO_USAGE;

            String sort = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TYPE_BEST_POST, SortType.Type.BEST.name());
            if (sort.equals(SortType.Type.CONTROVERSIAL.name()) || sort.equals(SortType.Type.TOP.name())) {
                String sortTime = mSortTypeSharedPreferences.getString(SharedPreferencesUtils.SORT_TIME_BEST_POST, SortType.Time.ALL.name());
                sortType = new SortType(SortType.Type.valueOf(sort), SortType.Time.valueOf(sortTime));
            } else {
                sortType = new SortType(SortType.Type.valueOf(sort));
            }
            postLayout = mPostLayoutSharedPreferences.getInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, defaultPostLayout);

            mAdapter = new PostRecyclerViewAdapter(activity, this, mExecutor, mOauthRetrofit,
                    mRedgifsRetrofit, mStreamableApiProvider, mCustomThemeWrapper, locale,
                    activity.accessToken, activity.accountName, postType, postLayout, true,
                    mSharedPreferences, mCurrentAccountSharedPreferences, mNsfwAndSpoilerSharedPreferences, mPostHistorySharedPreferences,
                    mExoCreator, new PostRecyclerViewAdapter.Callback() {
                @Override
                public void typeChipClicked(int filter) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, filter);
                    startActivity(intent);
                }

                @Override
                public void flairChipClicked(String flair) {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, flair);
                    startActivity(intent);
                }

                @Override
                public void nsfwChipClicked() {
                    Intent intent = new Intent(activity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE_FILTER, Post.NSFW_TYPE);
                    startActivity(intent);
                }

                @Override
                public void currentlyBindItem(int position) {
                    if (maxPosition < position) {
                        maxPosition = position;
                    }
                }

                @Override
                public void delayTransition() {
                    TransitionManager.beginDelayedTransition(binding.recyclerViewPostFragment, new AutoTransition());
                }
            });
        }

        int nColumns = getNColumns(resources);
        if (nColumns == 1) {
            mLinearLayoutManager = new LinearLayoutManagerBugFixed(activity);
            binding.recyclerViewPostFragment.setLayoutManager(mLinearLayoutManager);
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(nColumns, StaggeredGridLayoutManager.VERTICAL);
            binding.recyclerViewPostFragment.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset, nColumns);
            binding.recyclerViewPostFragment.addItemDecoration(itemDecoration);
        }

        if (recyclerViewPosition > 0) {
            binding.recyclerViewPostFragment.scrollToPosition(recyclerViewPosition);
        }

        if (activity instanceof ActivityToolbarInterface) {
            ((ActivityToolbarInterface) activity).displaySortType();
        }

        if (!activity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            if (postFilter == null) {
                FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilter(mRedditDataRoomDatabase, mExecutor,
                        new Handler(), usage, nameOfUsage, (postFilter) -> {
                            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                this.postFilter = postFilter;
                                this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(activity.accountName + SharedPreferencesUtils.NSFW_BASE, false);
                                initializeAndBindPostViewModel();
                            }
                        });
            } else {
                initializeAndBindPostViewModel();
            }
        } else {
            if (postFilter == null) {
                if (postType == PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE) {
                    if (concatenatedSubredditNames == null) {
                        FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilterAndConcatenatedSubredditNames(mRedditDataRoomDatabase, mExecutor, new Handler(), usage, nameOfUsage,
                                (postFilter, concatenatedSubredditNames) -> {
                                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                        this.postFilter = postFilter;
                                        this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_BASE, false);
                                        this.concatenatedSubredditNames = concatenatedSubredditNames;
                                        if (concatenatedSubredditNames == null) {
                                            showErrorView(R.string.anonymous_front_page_no_subscriptions);
                                        } else {
                                            initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                                        }
                                    }
                                });
                    } else {
                        initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                    }
                } else if (postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
                    if (concatenatedSubredditNames == null) {
                        FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilterAndConcatenatedSubredditNames(mRedditDataRoomDatabase, mExecutor, new Handler(), multiRedditPath, usage, nameOfUsage,
                                (postFilter, concatenatedSubredditNames) -> {
                                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                        this.postFilter = postFilter;
                                        this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_BASE, false);
                                        this.concatenatedSubredditNames = concatenatedSubredditNames;
                                        if (concatenatedSubredditNames == null) {
                                            showErrorView(R.string.anonymous_multireddit_no_subreddit);
                                        } else {
                                            initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                                        }
                                    }
                                });
                    } else {
                        initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                    }
                } else {
                    FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilter(mRedditDataRoomDatabase, mExecutor,
                            new Handler(), usage, nameOfUsage, (postFilter) -> {
                                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                    this.postFilter = postFilter;
                                    this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_BASE, false);
                                    initializeAndBindPostViewModelForAnonymous(null);
                                }
                            });
                }
            } else {
                if (postType == PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE) {
                    if (concatenatedSubredditNames == null) {
                        FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilterAndConcatenatedSubredditNames(mRedditDataRoomDatabase, mExecutor, new Handler(), usage, nameOfUsage,
                                (postFilter, concatenatedSubredditNames) -> {
                                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                        this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_BASE, false);
                                        this.concatenatedSubredditNames = concatenatedSubredditNames;
                                        if (concatenatedSubredditNames == null) {
                                            showErrorView(R.string.anonymous_front_page_no_subscriptions);
                                        } else {
                                            initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                                        }
                                    }
                                });
                    } else {
                        initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                    }
                } else if (postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
                    if (concatenatedSubredditNames == null) {
                        FetchPostFilterAndConcatenatedSubredditNames.fetchPostFilterAndConcatenatedSubredditNames(mRedditDataRoomDatabase, mExecutor, new Handler(), multiRedditPath, usage, nameOfUsage,
                                (postFilter, concatenatedSubredditNames) -> {
                                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && !isDetached()) {
                                        this.postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_BASE, false);
                                        this.concatenatedSubredditNames = concatenatedSubredditNames;
                                        if (concatenatedSubredditNames == null) {
                                            showErrorView(R.string.anonymous_multireddit_no_subreddit);
                                        } else {
                                            initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                                        }
                                    }
                                });
                    } else {
                        initializeAndBindPostViewModelForAnonymous(concatenatedSubredditNames);
                    }
                } else {
                    initializeAndBindPostViewModelForAnonymous(null);
                }
            }
        }

        if (nColumns == 1 && mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SWIPE_ACTION, false)) {
            swipeActionEnabled = true;
            touchHelper.attachToRecyclerView(binding.recyclerViewPostFragment, 1);
        }
        binding.recyclerViewPostFragment.setAdapter(mAdapter);
        binding.recyclerViewPostFragment.setCacheManager(mAdapter);
        binding.recyclerViewPostFragment.setPlayerInitializer(order -> {
            VolumeInfo volumeInfo = new VolumeInfo(true, 0f);
            return new PlaybackInfo(INDEX_UNSET, TIME_UNSET, volumeInfo);
        });

        return binding.getRoot();
    }

    private void initializeAndBindPostViewModel() {
        if (postType == PostPagingSource.TYPE_SEARCH) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, activity.accessToken, activity.accountName, mSharedPreferences,
                    mPostFeedScrolledPositionSharedPreferences, mPostHistorySharedPreferences, subredditName,
                    query, trendingSource, postType, sortType, postFilter, readPostsList)).get(PostViewModel.class);
        } else if (postType == PostPagingSource.TYPE_SUBREDDIT) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, activity.accessToken, activity.accountName, mSharedPreferences, mPostFeedScrolledPositionSharedPreferences,
                    mPostHistorySharedPreferences, subredditName, postType, sortType, postFilter, readPostsList))
                    .get(PostViewModel.class);
        } else if (postType == PostPagingSource.TYPE_MULTI_REDDIT) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, activity.accessToken, activity.accountName, mSharedPreferences, mPostFeedScrolledPositionSharedPreferences,
                    mPostHistorySharedPreferences, multiRedditPath, query, postType, sortType, postFilter, readPostsList))
                    .get(PostViewModel.class);
        } else if (postType == PostPagingSource.TYPE_USER) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit, activity.accessToken, activity.accountName, mSharedPreferences, mPostFeedScrolledPositionSharedPreferences,
                    mPostHistorySharedPreferences, username, postType, sortType, postFilter, where, readPostsList))
                    .get(PostViewModel.class);
        } else {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    mOauthRetrofit, activity.accessToken,
                    activity.accountName, mSharedPreferences, mPostFeedScrolledPositionSharedPreferences,
                    mPostHistorySharedPreferences, postType, sortType, postFilter, readPostsList)).get(PostViewModel.class);
        }

        bindPostViewModel();
    }

    private void initializeAndBindPostViewModelForAnonymous(String concatenatedSubredditNames) {
        if (postType == PostPagingSource.TYPE_SEARCH) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    mRetrofit, null, activity.accountName, mSharedPreferences,
                    mPostFeedScrolledPositionSharedPreferences, null, subredditName, query, trendingSource,
                    postType, sortType, postFilter, readPostsList)).get(PostViewModel.class);
        } else if (postType == PostPagingSource.TYPE_SUBREDDIT) {
            mPostViewModel = new ViewModelProvider(this, new PostViewModel.Factory(mExecutor,
                    mRetrofit, null, activity.accountName, mSharedPreferences,
                    mPostFeedScrolledPositionSharedPreferences, null, subredditName, postType, sortType,
                    postFilter, readPostsList)).get(PostViewModel.class);
        } else if (postType == PostPagingSource.TYPE_USER) {
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    mRetrofit, null, activity.accountName, mSharedPreferences,
                    mPostFeedScrolledPositionSharedPreferences, null, username, postType, sortType, postFilter,
                    where, readPostsList)).get(PostViewModel.class);
        } else {
            //Anonymous front page or multireddit
            mPostViewModel = new ViewModelProvider(PostFragment.this, new PostViewModel.Factory(mExecutor,
                    mRetrofit, mSharedPreferences, concatenatedSubredditNames, postType, sortType, postFilter, readPostsList))
                    .get(PostViewModel.class);
        }

        bindPostViewModel();
    }

    private void bindPostViewModel() {
        mPostViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> mAdapter.submitData(getViewLifecycleOwner().getLifecycle(), posts));

        mPostViewModel.moderationEventLiveData.observe(getViewLifecycleOwner(), moderationEvent -> {
            EventBus.getDefault().post(new PostUpdateEventToPostList(moderationEvent.getPost(), moderationEvent.getPosition()));
            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(moderationEvent.getPost()));
            Toast.makeText(activity, moderationEvent.getToastMessageResId(), Toast.LENGTH_SHORT).show();
        });

        mAdapter.addLoadStateListener(combinedLoadStates -> {
            LoadState refreshLoadState = combinedLoadStates.getRefresh();
            LoadState appendLoadState = combinedLoadStates.getAppend();

            binding.swipeRefreshLayoutPostFragment.setRefreshing(refreshLoadState instanceof LoadState.Loading);
            if (refreshLoadState instanceof LoadState.NotLoading) {
                if (refreshLoadState.getEndOfPaginationReached() && mAdapter.getItemCount() < 1) {
                    noPostFound();
                } else {
                    hasPost = true;
                }
            } else if (refreshLoadState instanceof LoadState.Error) {
                binding.fetchPostInfoLinearLayoutPostFragment.setOnClickListener(view -> refresh());
                showErrorView(R.string.load_posts_error);
            }
            if (!(refreshLoadState instanceof LoadState.Loading) && appendLoadState instanceof LoadState.NotLoading) {
                if (appendLoadState.getEndOfPaginationReached() && mAdapter.getItemCount() < 1) {
                    noPostFound();
                }
            }
            return null;
        });

        binding.recyclerViewPostFragment.setAdapter(mAdapter.withLoadStateFooter(new Paging3LoadingStateAdapter(activity, mCustomThemeWrapper, R.string.load_more_posts_error,
                view -> mAdapter.retry())));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.post_fragment, menu);
        for (int i = 0; i < menu.size(); i++) {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, menu.getItem(i), null);
        }
        lazyModeItem = menu.findItem(R.id.action_lazy_mode_post_fragment);

        if (isInLazyMode) {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_stop_lazy_mode));
        } else {
            Utils.setTitleWithCustomFontToMenuItem(activity.typeface, lazyModeItem, getString(R.string.action_start_lazy_mode));
        }

        if (activity instanceof FilteredPostsActivity) {
            menu.findItem(R.id.action_filter_posts_post_fragment).setVisible(false);
        }

        if (activity instanceof FilteredPostsActivity || activity instanceof AccountPostsActivity
                || activity instanceof AccountSavedThingActivity) {
            menu.findItem(R.id.action_more_options_post_fragment).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_lazy_mode_post_fragment) {
            if (isInLazyMode) {
                stopLazyMode();
            } else {
                startLazyMode();
            }
            return true;
        } else if (item.getItemId() == R.id.action_filter_posts_post_fragment) {
            filterPosts();
            return true;
        } else if (item.getItemId() == R.id.action_more_options_post_fragment) {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment= new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, activity.accountName.equals(Account.ANONYMOUS_ACCOUNT));
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(activity.getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    private void noPostFound() {
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }

        binding.fetchPostInfoLinearLayoutPostFragment.setOnClickListener(null);
        showErrorView(R.string.no_posts);
    }

    public void changeSortType(SortType sortType) {
        if (mPostViewModel != null) {
            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_SORT_TYPE, true)) {
                switch (postType) {
                    case PostPagingSource.TYPE_FRONT_PAGE:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_BEST_POST, sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_BEST_POST, sortType.getTime().name()).apply();
                        }
                        break;
                    case PostPagingSource.TYPE_SUBREDDIT:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + subredditName, sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + subredditName, sortType.getTime().name()).apply();
                        }
                        break;
                    case PostPagingSource.TYPE_USER:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_USER_POST_BASE + username, sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_USER_POST_BASE + username, sortType.getTime().name()).apply();
                        }
                        break;
                    case PostPagingSource.TYPE_SEARCH:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_POST, sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_SEARCH_POST, sortType.getTime().name()).apply();
                        }
                        break;
                    case PostPagingSource.TYPE_MULTI_REDDIT:
                    case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_MULTI_REDDIT_POST_BASE + multiRedditPath,
                                sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_MULTI_REDDIT_POST_BASE + multiRedditPath,
                                    sortType.getTime().name()).apply();
                        }
                        break;
                    case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                        mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + Account.ANONYMOUS_ACCOUNT, sortType.getType().name()).apply();
                        if (sortType.getTime() != null) {
                            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + Account.ANONYMOUS_ACCOUNT, sortType.getTime().name()).apply();
                        }
                        break;
                }
            }
            if (binding.fetchPostInfoLinearLayoutPostFragment.getVisibility() != View.GONE) {
                binding.fetchPostInfoLinearLayoutPostFragment.setVisibility(View.GONE);
                mGlide.clear(binding.fetchPostInfoImageViewPostFragment);
            }
            hasPost = false;
            if (isInLazyMode) {
                stopLazyMode();
            }
            this.sortType = sortType;
            mPostViewModel.changeSortType(sortType);
            goBackToTop();
        }
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
        outState.putString(CONCATENATED_SUBREDDIT_NAMES_STATE, concatenatedSubredditNames);
        outState.putLong(POST_FRAGMENT_ID_STATE, postFragmentId);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveCache();
    }

    private void saveCache() {
        if (savePostFeedScrolledPosition && postType == PostPagingSource.TYPE_FRONT_PAGE && sortType != null && sortType.getType() == SortType.Type.BEST && mAdapter != null) {
            Post currentPost = mAdapter.getItemByPosition(maxPosition);
            if (currentPost != null) {
                String accountNameForCache = activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) ? SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_ANONYMOUS : activity.accountName;
                String key = accountNameForCache + SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_FRONT_PAGE_BASE;
                String value = currentPost.getFullName();
                mPostFeedScrolledPositionSharedPreferences.edit().putString(key, value).apply();
            }
        }
    }

    @Override
    public void refresh() {
        binding.fetchPostInfoLinearLayoutPostFragment.setVisibility(View.GONE);
        hasPost = false;
        if (isInLazyMode) {
            stopLazyMode();
        }
        saveCache();
        mAdapter.refresh();
        goBackToTop();
    }

    @Override
    protected void showErrorView(int stringResId) {
        if (activity != null && isAdded()) {
            binding.swipeRefreshLayoutPostFragment.setRefreshing(false);
            binding.fetchPostInfoLinearLayoutPostFragment.setVisibility(View.VISIBLE);
            binding.fetchPostInfoTextViewPostFragment.setText(stringResId);
            mGlide.load(R.drawable.error_image).into(binding.fetchPostInfoImageViewPostFragment);
        }
    }

    @NonNull
    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return binding.swipeRefreshLayoutPostFragment;
    }

    @NonNull
    @Override
    protected RecyclerView getPostRecyclerView() {
        return binding.recyclerViewPostFragment;
    }

    @Nullable
    @Override
    protected PostRecyclerViewAdapter getPostAdapter() {
        return mAdapter;
    }

    @Override
    public void changeNSFW(boolean nsfw) {
        postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && nsfw;
        if (mPostViewModel != null) {
            mPostViewModel.changePostFilter(postFilter);
        }
    }

    @Override
    public void changePostLayout(int postLayout, boolean temporary) {
        this.postLayout = postLayout;
        if (!temporary) {
            switch (postType) {
                case PostPagingSource.TYPE_FRONT_PAGE:
                case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_SUBREDDIT:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_USER:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + username, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_SEARCH:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_MULTI_REDDIT:
                case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiRedditPath, postLayout).apply();
                    break;
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
            if (binding.recyclerViewPostFragment.getItemDecorationCount() > 0) {
                binding.recyclerViewPostFragment.removeItemDecorationAt(0);
            }
            binding.recyclerViewPostFragment.setLayoutManager(mLinearLayoutManager);
            mStaggeredGridLayoutManager = null;
        } else {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(nColumns, StaggeredGridLayoutManager.VERTICAL);
            if (binding.recyclerViewPostFragment.getItemDecorationCount() > 0) {
                binding.recyclerViewPostFragment.removeItemDecorationAt(0);
            }
            binding.recyclerViewPostFragment.setLayoutManager(mStaggeredGridLayoutManager);
            StaggeredGridLayoutManagerItemOffsetDecoration itemDecoration =
                    new StaggeredGridLayoutManagerItemOffsetDecoration(activity, R.dimen.staggeredLayoutManagerItemOffset, nColumns);
            binding.recyclerViewPostFragment.addItemDecoration(itemDecoration);
            mLinearLayoutManager = null;
        }

        if (previousPosition > 0) {
            binding.recyclerViewPostFragment.scrollToPosition(previousPosition);
        }

        if (mAdapter != null) {
            mAdapter.setPostLayout(postLayout);
            refreshAdapter();
        }
    }

    @Override
    public void applyTheme() {
        binding.swipeRefreshLayoutPostFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        binding.swipeRefreshLayoutPostFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        binding.fetchPostInfoTextViewPostFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (activity.typeface != null) {
            binding.fetchPostInfoTextViewPostFragment.setTypeface(activity.typeface);
        }
    }

    @Override
    public void hideReadPosts() {
        mPostViewModel.hideReadPosts();
    }

    @Override
    public void changePostFilter(PostFilter postFilter) {
        this.postFilter = postFilter;
        if (mPostViewModel != null) {
            mPostViewModel.changePostFilter(postFilter);
        }
    }

    @Override
    public PostFilter getPostFilter() {
        return postFilter;
    }

    @Override
    public void filterPosts() {
        if (postType == PostPagingSource.TYPE_SEARCH) {
            Intent intent = new Intent(activity, CustomizePostFilterActivity.class);
            intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
            intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
            intent.putExtra(FilteredPostsActivity.EXTRA_TRENDING_SOURCE, trendingSource);
            intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
            intent.putExtra(CustomizePostFilterActivity.EXTRA_START_FILTERED_POSTS_WHEN_FINISH, true);
            startActivity(intent);
        } else if (postType == PostPagingSource.TYPE_SUBREDDIT) {
            Intent intent = new Intent(activity, CustomizePostFilterActivity.class);
            intent.putExtra(FilteredPostsActivity.EXTRA_NAME, subredditName);
            intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
            intent.putExtra(CustomizePostFilterActivity.EXTRA_START_FILTERED_POSTS_WHEN_FINISH, true);
            startActivity(intent);
        } else if (postType == PostPagingSource.TYPE_MULTI_REDDIT || postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
            Intent intent = new Intent(activity, CustomizePostFilterActivity.class);
            intent.putExtra(FilteredPostsActivity.EXTRA_NAME, multiRedditPath);
            intent.putExtra(FilteredPostsActivity.EXTRA_QUERY, query);
            intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
            intent.putExtra(CustomizePostFilterActivity.EXTRA_START_FILTERED_POSTS_WHEN_FINISH, true);
            startActivity(intent);
        } else if (postType == PostPagingSource.TYPE_USER) {
            Intent intent = new Intent(activity, CustomizePostFilterActivity.class);
            intent.putExtra(FilteredPostsActivity.EXTRA_NAME, username);
            intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
            intent.putExtra(FilteredPostsActivity.EXTRA_USER_WHERE, where);
            intent.putExtra(CustomizePostFilterActivity.EXTRA_START_FILTERED_POSTS_WHEN_FINISH, true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(activity, CustomizePostFilterActivity.class);
            intent.putExtra(FilteredPostsActivity.EXTRA_NAME, activity.getString(R.string.best));
            intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, postType);
            intent.putExtra(CustomizePostFilterActivity.EXTRA_START_FILTERED_POSTS_WHEN_FINISH, true);
            startActivity(intent);
        }
    }

    @Override
    public boolean getIsNsfwSubreddit() {
        if (activity instanceof ViewSubredditDetailActivity) {
            return ((ViewSubredditDetailActivity) activity).isNsfwSubreddit();
        } else if (activity instanceof FilteredPostsActivity) {
            return ((FilteredPostsActivity) activity).isNsfwSubreddit();
        } else {
            return false;
        }
    }

    @Subscribe
    public void onChangeDefaultPostLayoutEvent(ChangeDefaultPostLayoutEvent changeDefaultPostLayoutEvent) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            switch (postType) {
                case PostPagingSource.TYPE_SUBREDDIT:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + bundle.getString(EXTRA_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
                case PostPagingSource.TYPE_USER:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + bundle.getString(EXTRA_USER_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
                case PostPagingSource.TYPE_MULTI_REDDIT:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + bundle.getString(EXTRA_NAME))) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
                case PostPagingSource.TYPE_SEARCH:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST)) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
                case PostPagingSource.TYPE_FRONT_PAGE:
                    if (!mPostLayoutSharedPreferences.contains(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST)) {
                        changePostLayout(changeDefaultPostLayoutEvent.defaultPostLayout, true);
                    }
                    break;
            }
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
    public void onChangeSavePostFeedScrolledPositionEvent(ChangeSavePostFeedScrolledPositionEvent changeSavePostFeedScrolledPositionEvent) {
        savePostFeedScrolledPosition = changeSavePostFeedScrolledPositionEvent.savePostFeedScrolledPosition;
    }

    @Subscribe
    public void onNeedForPostListFromPostRecyclerViewAdapterEvent(NeedForPostListFromPostFragmentEvent event) {
        if (postFragmentId == event.postFragmentTimeId && mAdapter != null) {
            EventBus.getDefault().post(new ProvidePostListToViewPostDetailActivityEvent(postFragmentId,
                    new ArrayList<>(mAdapter.snapshot()), postType, subredditName,
                    concatenatedSubredditNames, username, where, multiRedditPath, query, trendingSource,
                    postFilter, sortType, readPostsList));
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

        RecyclerView.LayoutManager layoutManager = binding.recyclerViewPostFragment.getLayoutManager();
        binding.recyclerViewPostFragment.setAdapter(null);
        binding.recyclerViewPostFragment.setLayoutManager(null);
        binding.recyclerViewPostFragment.setAdapter(mAdapter);
        binding.recyclerViewPostFragment.setLayoutManager(layoutManager);
        if (previousPosition > 0) {
            binding.recyclerViewPostFragment.scrollToPosition(previousPosition);
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

    public SortType getSortType() {
        return sortType;
    }

    public int getPostType() {
        return postType;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isInLazyMode) {
            pauseLazyMode(false);
        }
        if (mAdapter != null) {
            binding.recyclerViewPostFragment.onWindowVisibilityChanged(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        binding.recyclerViewPostFragment.addOnWindowFocusChangedListener(null);
        super.onDestroyView();
    }

    private void onWindowFocusChanged(boolean hasWindowsFocus) {
        if (mAdapter != null) {
            mAdapter.setCanPlayVideo(hasWindowsFocus);
        }
    }

    @Override
    public void approvePost(@NonNull Post post, int position) {
        mPostViewModel.approvePost(post, position);
    }

    @Override
    public void removePost(@NonNull Post post, int position, boolean isSpam) {
        mPostViewModel.removePost(post, position, isSpam);
    }

    @Override
    public void toggleSticky(@NonNull Post post, int position) {
        mPostViewModel.toggleSticky(post, position);
    }

    @Override
    public void toggleLock(@NonNull Post post, int position) {
        mPostViewModel.toggleLock(post, position);
    }

    @Override
    public void toggleNSFW(@NonNull Post post, int position) {
        mPostViewModel.toggleNSFW(post, position);
    }

    @Override
    public void toggleSpoiler(@NonNull Post post, int position) {
        mPostViewModel.toggleSpoiler(post, position);
    }

    @Override
    public void toggleMod(@NonNull Post post, int position) {
        mPostViewModel.toggleMod(post, position);
    }
}
