package ml.docilealligator.infinityforreddit.activities;

import static ml.docilealligator.infinityforreddit.activities.CommentActivity.RETURN_EXTRA_COMMENT_DATA_KEY;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.evernote.android.state.State;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.post.LoadingMorePostsStatus;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SaveThing;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.AccountManagement;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewPostDetailBinding;
import ml.docilealligator.infinityforreddit.events.NeedForPostListFromPostFragmentEvent;
import ml.docilealligator.infinityforreddit.events.ProvidePostListToViewPostDetailActivityEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.MorePostsInfoFragment;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.post.HistoryPostPagingSource;
import ml.docilealligator.infinityforreddit.post.ParsePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewPostDetailActivity extends BaseActivity implements SortTypeSelectionCallback, ActivityToolbarInterface {

    public static final String EXTRA_POST_DATA = "EPD";
    public static final String EXTRA_POST_ID = "EPI";
    public static final String EXTRA_POST_LIST_POSITION = "EPLP";
    public static final String EXTRA_SINGLE_COMMENT_ID = "ESCI";
    public static final String EXTRA_CONTEXT_NUMBER = "ECN";
    public static final String EXTRA_MESSAGE_FULLNAME = "ENI";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_POST_FRAGMENT_ID = "EPFI";
    public static final String EXTRA_IS_NSFW_SUBREDDIT = "EINS";
    public static final int EDIT_COMMENT_REQUEST_CODE = 3;
    @State
    String mNewAccountName;
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
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("post_details")
    SharedPreferences mPostDetailsSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    @State
    ArrayList<Post> posts;
    @State
    int postType;
    @State
    String subredditName;
    @State
    String concatenatedSubredditNames;
    @State
    String username;
    @State
    String userWhere;
    @State
    String multiPath;
    @State
    String query;
    @State
    String trendingSource;
    @State
    PostFilter postFilter;
    @State
    SortType.Type sortType;
    @State
    SortType.Time sortTime;
    @State
    ArrayList<String> readPostList;
    @State
    Post post;
    @State
    @LoadingMorePostsStatus
    int loadingMorePostsStatus = LoadingMorePostsStatus.NOT_LOADING;
    public Map<String, String> authorIcons = new HashMap<>();
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private long postFragmentId;
    private int postListPosition;
    private int orientation;
    private boolean mVolumeKeysNavigateComments;
    private boolean isNsfwSubreddit;
    private ActivityViewPostDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        BigImageViewer.initialize(GlideImageLoader.with(this.getApplicationContext()));

        binding = ActivityViewPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bridge.restoreInstanceState(this, savedInstanceState);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutViewPostDetailActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarViewPostDetailActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.fabViewPostDetailActivity.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    binding.fabViewPostDetailActivity.setLayoutParams(params);

                    binding.searchPanelMaterialCardViewViewPostDetailActivity.setContentPadding(binding.searchPanelMaterialCardViewViewPostDetailActivity.getPaddingStart(),
                            binding.searchPanelMaterialCardViewViewPostDetailActivity.getPaddingTop(),
                            binding.searchPanelMaterialCardViewViewPostDetailActivity.getPaddingEnd(),
                            binding.searchPanelMaterialCardViewViewPostDetailActivity.getPaddingBottom() + navBarHeight);
                }
            }
        }

        boolean swipeBetweenPosts = mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_BETWEEN_POSTS, false);
        if (!swipeBetweenPosts) {
            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
                mSliderPanel = Slidr.attach(this);
            }
            binding.viewPager2ViewPostDetailActivity.setUserInputEnabled(false);
        } else {
            mViewPager2 = binding.viewPager2ViewPostDetailActivity;
        }
        postFragmentId = getIntent().getLongExtra(EXTRA_POST_FRAGMENT_ID, -1);
        if (swipeBetweenPosts && posts == null && postFragmentId > 0) {
            EventBus.getDefault().post(new NeedForPostListFromPostFragmentEvent(postFragmentId));
        }

        postListPosition = getIntent().getIntExtra(EXTRA_POST_LIST_POSITION, -1);
        isNsfwSubreddit = getIntent().getBooleanExtra(EXTRA_IS_NSFW_SUBREDDIT, false);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            post = getIntent().getParcelableExtra(EXTRA_POST_DATA);
        }

        orientation = getResources().getConfiguration().orientation;

        binding.toolbarViewPostDetailActivity.setTitle("");
        setSupportActionBar(binding.toolbarViewPostDetailActivity);
        setToolbarGoToTop(binding.toolbarViewPostDetailActivity);

        if (savedInstanceState == null) {
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }

        mVolumeKeysNavigateComments = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOLUME_KEYS_NAVIGATE_COMMENTS, false);

        binding.fabViewPostDetailActivity.setOnClickListener(view -> {
            if (sectionsPagerAdapter != null) {
                ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (fragment != null) {
                    fragment.scrollToNextParentComment();
                }
            }
        });

        binding.fabViewPostDetailActivity.setOnLongClickListener(view -> {
            if (sectionsPagerAdapter != null) {
                ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (fragment != null) {
                    fragment.scrollToPreviousParentComment();
                    return true;
                }
            }
            return false;
        });

        if (accountName.equals(Account.ANONYMOUS_ACCOUNT) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.searchTextInputEditTextViewPostDetailActivity.setImeOptions(binding.searchTextInputEditTextViewPostDetailActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        if (loadingMorePostsStatus == LoadingMorePostsStatus.LOADING) {
            loadingMorePostsStatus = LoadingMorePostsStatus.NOT_LOADING;
            fetchMorePosts(false);
        }

        binding.fabViewPostDetailActivity.bindRequiredData(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? getDisplay() : null,
                mPostDetailsSharedPreferences,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        );

        binding.fabViewPostDetailActivity.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.fabViewPostDetailActivity.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                binding.fabViewPostDetailActivity.setCoordinates();
            }
        });

        sectionsPagerAdapter = new SectionsPagerAdapter(this);

        checkNewAccountAndBindView(savedInstanceState);
    }

    public void setTitle(String title) {
        binding.toolbarViewPostDetailActivity.setTitle(title);
    }

    public void showFab() {
        binding.fabViewPostDetailActivity.show();
    }

    public void hideFab() {
        binding.fabViewPostDetailActivity.hide();
    }

    public void showSnackBar(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutViewPostDetailActivity,
                binding.collapsingToolbarLayoutViewPostDetailActivity, binding.toolbarViewPostDetailActivity);
        applyFABTheme(binding.fabViewPostDetailActivity);
        binding.searchPanelMaterialCardViewViewPostDetailActivity.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimary()));
        int searchPanelTextAndIconColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        binding.searchTextInputLayoutViewPostDetailActivity.setBoxStrokeColor(searchPanelTextAndIconColor);
        binding.searchTextInputLayoutViewPostDetailActivity.setDefaultHintTextColor(ColorStateList.valueOf(searchPanelTextAndIconColor));
        binding.searchTextInputEditTextViewPostDetailActivity.setTextColor(searchPanelTextAndIconColor);
        binding.previousResultImageViewViewPostDetailActivity.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.nextResultImageViewViewPostDetailActivity.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.closeSearchPanelImageViewViewPostDetailActivity.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if (typeface != null) {
            binding.searchTextInputLayoutViewPostDetailActivity.setTypeface(typeface);
            binding.searchTextInputEditTextViewPostDetailActivity.setTypeface(typeface);
        }
    }

    private void checkNewAccountAndBindView(Bundle savedInstanceState) {
        if (mNewAccountName != null) {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT) || !accountName.equals(mNewAccountName)) {
                AccountManagement.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;
                            if (newAccount != null) {
                                accessToken = newAccount.getAccessToken();
                                accountName = newAccount.getAccountName();
                            }

                            bindView(savedInstanceState);
                        });
            } else {
                bindView(savedInstanceState);
            }
        } else {
            bindView(savedInstanceState);
        }
    }

    private void bindView(Bundle savedInstanceState) {
        binding.viewPager2ViewPostDetailActivity.setAdapter(sectionsPagerAdapter);
        if (savedInstanceState == null) {
            binding.viewPager2ViewPostDetailActivity.setCurrentItem(getIntent().getIntExtra(EXTRA_POST_LIST_POSITION, 0), false);
        }
        binding.viewPager2ViewPostDetailActivity.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (posts != null && position > posts.size() - 5) {
                    fetchMorePosts(false);
                }
            }
        });

        binding.searchPanelMaterialCardViewViewPostDetailActivity.setOnClickListener(null);
        
        binding.nextResultImageViewViewPostDetailActivity.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                searchComment(fragment, true);
            }
        });

        binding.previousResultImageViewViewPostDetailActivity.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                searchComment(fragment, false);
            }
        });

        binding.closeSearchPanelImageViewViewPostDetailActivity.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.resetSearchCommentIndex();
            }

            binding.searchPanelMaterialCardViewViewPostDetailActivity.setVisibility(View.GONE);
        });
    }

    public boolean isNsfwSubreddit() {
        return isNsfwSubreddit;
    }

    private void editComment(Comment comment, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.editComment(comment, position);
            }
        }
    }

    private void editComment(String commentContentMarkdown, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.editComment(commentContentMarkdown, position);
            }
        }
    }

    public void deleteComment(String fullName, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.deleteComment(fullName, position);
            }
        }
    }

    public void toggleReplyNotifications(Comment comment, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.toggleReplyNotifications(comment, position);
            }
        }
    }

    public void saveComment(@NonNull Comment comment, int position) {
        if (comment.isSaved()) {
            comment.setSaved(false);
            SaveThing.unsaveThing(mOauthRetrofit, accessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                @Override
                public void success() {
                    ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment != null) {
                        fragment.saveComment(position, false);
                    }
                    Toast.makeText(ViewPostDetailActivity.this, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failed() {
                    ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment != null) {
                        fragment.saveComment(position, true);
                    }
                    Toast.makeText(ViewPostDetailActivity.this, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            comment.setSaved(true);
            SaveThing.saveThing(mOauthRetrofit, accessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                @Override
                public void success() {
                    ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment != null) {
                        fragment.saveComment(position, true);
                    }
                    Toast.makeText(ViewPostDetailActivity.this, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failed() {
                    ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment != null) {
                        fragment.saveComment(position, false);
                    }
                    Toast.makeText(ViewPostDetailActivity.this, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean toggleSearchPanelVisibility() {
        if (binding.searchPanelMaterialCardViewViewPostDetailActivity.getVisibility() == View.GONE) {
            binding.searchPanelMaterialCardViewViewPostDetailActivity.setVisibility(View.VISIBLE);
            return false;
        } else {
            binding.searchPanelMaterialCardViewViewPostDetailActivity.setVisibility(View.GONE);
            binding.searchTextInputEditTextViewPostDetailActivity.setText("");
            return true;
        }
    }

    public void searchComment(ViewPostDetailFragment fragment, boolean searchNextComment) {
        if (!binding.searchTextInputEditTextViewPostDetailActivity.getText().toString().isEmpty()) {
            fragment.searchComment(binding.searchTextInputEditTextViewPostDetailActivity.getText().toString(), searchNextComment);
        }
    }

    public void fetchMorePosts(boolean changePage) {
        if (loadingMorePostsStatus == LoadingMorePostsStatus.LOADING || loadingMorePostsStatus == LoadingMorePostsStatus.NO_MORE_POSTS) {
            return;
        }

        loadingMorePostsStatus = LoadingMorePostsStatus.LOADING;

        MorePostsInfoFragment morePostsFragment = sectionsPagerAdapter.getMorePostsInfoFragment();
        if (morePostsFragment != null) {
            morePostsFragment.setStatus(LoadingMorePostsStatus.LOADING);
        }

        Handler handler = new Handler(Looper.getMainLooper());

        if (postType != HistoryPostPagingSource.TYPE_READ_POSTS) {
            mExecutor.execute(() -> {
                RedditAPI api = (accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit).create(RedditAPI.class);
                Call<String> call;
                String afterKey = posts.isEmpty() ? null : posts.get(posts.size() - 1).getFullName();
                switch (postType) {
                    case PostPagingSource.TYPE_SUBREDDIT:
                        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            call = api.getSubredditBestPosts(subredditName, sortType, sortTime, afterKey);
                        } else {
                            call = api.getSubredditBestPostsOauth(subredditName, sortType,
                                    sortTime, afterKey, APIUtils.getOAuthHeader(accessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_USER:
                        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            call = api.getUserPosts(username, afterKey, sortType, sortTime);
                        } else {
                            call = api.getUserPostsOauth(username, userWhere, afterKey, sortType,
                                    sortTime, APIUtils.getOAuthHeader(accessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_SEARCH:
                        if (subredditName == null) {
                            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                                call = api.searchPosts(query, afterKey, sortType, sortTime,
                                        trendingSource);
                            } else {
                                call = api.searchPostsOauth(query, afterKey, sortType,
                                        sortTime, trendingSource, APIUtils.getOAuthHeader(accessToken));
                            }
                        } else {
                            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                                call = api.searchPostsInSpecificSubreddit(subredditName, query,
                                        sortType, sortTime, afterKey);
                            } else {
                                call = api.searchPostsInSpecificSubredditOauth(subredditName, query,
                                        sortType, sortTime, afterKey,
                                        APIUtils.getOAuthHeader(accessToken));
                            }
                        }
                        break;
                    case PostPagingSource.TYPE_MULTI_REDDIT:
                        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            call = api.getMultiRedditPosts(multiPath, afterKey, sortTime);
                        } else {
                            call = api.getMultiRedditPostsOauth(multiPath, afterKey,
                                    sortTime, APIUtils.getOAuthHeader(accessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                    case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                        call = api.getSubredditBestPosts(concatenatedSubredditNames, sortType, sortTime, afterKey);
                        break;
                    default:
                        call = api.getBestPosts(sortType, sortTime, afterKey,
                                APIUtils.getOAuthHeader(accessToken));
                }

                try {
                    Response<String> response = call.execute();
                    if (response.isSuccessful()) {
                        String responseString = response.body();
                        LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, readPostList);
                        if (newPosts == null) {
                            handler.post(() -> {
                                loadingMorePostsStatus = LoadingMorePostsStatus.NO_MORE_POSTS;
                                MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                if (fragment != null) {
                                    fragment.setStatus(LoadingMorePostsStatus.NO_MORE_POSTS);
                                }
                            });
                        } else {
                            LinkedHashSet<Post> postLinkedHashSet = new LinkedHashSet<>(posts);
                            int currentPostsSize = postLinkedHashSet.size();
                            postLinkedHashSet.addAll(newPosts);
                            if (currentPostsSize == postLinkedHashSet.size()) {
                                handler.post(() -> {
                                    loadingMorePostsStatus = LoadingMorePostsStatus.NO_MORE_POSTS;
                                    MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                    if (fragment != null) {
                                        fragment.setStatus(LoadingMorePostsStatus.NO_MORE_POSTS);
                                    }
                                });
                            } else {
                                posts = new ArrayList<>(postLinkedHashSet);
                                handler.post(() -> {
                                    if (changePage) {
                                        binding.viewPager2ViewPostDetailActivity.setCurrentItem(currentPostsSize - 1, false);
                                    }
                                    sectionsPagerAdapter.notifyItemRangeInserted(currentPostsSize, postLinkedHashSet.size() - currentPostsSize);
                                    loadingMorePostsStatus = LoadingMorePostsStatus.NOT_LOADING;
                                    MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                    if (fragment != null) {
                                        fragment.setStatus(LoadingMorePostsStatus.NOT_LOADING);
                                    }
                                });
                            }
                        }
                    } else {
                        handler.post(() -> {
                            loadingMorePostsStatus = LoadingMorePostsStatus.FAILED;
                            MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                            if (fragment != null) {
                                fragment.setStatus(LoadingMorePostsStatus.FAILED);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        loadingMorePostsStatus = LoadingMorePostsStatus.FAILED;
                        MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                        if (fragment != null) {
                            fragment.setStatus(LoadingMorePostsStatus.FAILED);
                        }
                    });
                }
            });
        } else {
            mExecutor.execute(() -> {
                long lastItem = 0;
                if (!posts.isEmpty()) {
                    lastItem = mRedditDataRoomDatabase.readPostDao().getReadPost(posts.get(posts.size() - 1).getId()).getTime();
                }
                List<ReadPost> readPosts = mRedditDataRoomDatabase.readPostDao().getAllReadPosts(accountName, lastItem);
                StringBuilder ids = new StringBuilder();
                for (ReadPost readPost : readPosts) {
                    ids.append("t3_").append(readPost.getId()).append(",");
                }
                if (ids.length() > 0) {
                    ids.deleteCharAt(ids.length() - 1);
                }

                Call<String> historyPosts;
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    historyPosts = mOauthRetrofit.create(RedditAPI.class).getInfoOauth(ids.toString(), APIUtils.getOAuthHeader(accessToken));
                } else {
                    historyPosts = mRetrofit.create(RedditAPI.class).getInfo(ids.toString());
                }

                try {
                    Response<String> response = historyPosts.execute();
                    if (response.isSuccessful()) {
                        String responseString = response.body();
                        LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, null);
                        if (newPosts == null || newPosts.isEmpty()) {
                            handler.post(() -> {
                                loadingMorePostsStatus = LoadingMorePostsStatus.NO_MORE_POSTS;
                                MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                if (fragment != null) {
                                    fragment.setStatus(LoadingMorePostsStatus.NO_MORE_POSTS);
                                }
                            });
                        } else {
                            LinkedHashSet<Post> postLinkedHashSet = new LinkedHashSet<>(posts);
                            int currentPostsSize = postLinkedHashSet.size();
                            postLinkedHashSet.addAll(newPosts);
                            if (currentPostsSize == postLinkedHashSet.size()) {
                                handler.post(() -> {
                                    loadingMorePostsStatus = LoadingMorePostsStatus.NO_MORE_POSTS;
                                    MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                    if (fragment != null) {
                                        fragment.setStatus(LoadingMorePostsStatus.NO_MORE_POSTS);
                                    }
                                });
                            } else {
                                posts = new ArrayList<>(postLinkedHashSet);
                                handler.post(() -> {
                                    if (changePage) {
                                        binding.viewPager2ViewPostDetailActivity.setCurrentItem(currentPostsSize - 1, false);
                                    }
                                    sectionsPagerAdapter.notifyItemRangeInserted(currentPostsSize, postLinkedHashSet.size() - currentPostsSize);
                                    loadingMorePostsStatus = LoadingMorePostsStatus.NOT_LOADING;
                                    MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                                    if (fragment != null) {
                                        fragment.setStatus(LoadingMorePostsStatus.NOT_LOADING);
                                    }
                                });
                            }
                        }
                    } else {
                        handler.post(() -> {
                            loadingMorePostsStatus = LoadingMorePostsStatus.FAILED;
                            MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                            if (fragment != null) {
                                fragment.setStatus(LoadingMorePostsStatus.FAILED);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        loadingMorePostsStatus = LoadingMorePostsStatus.FAILED;
                        MorePostsInfoFragment fragment = sectionsPagerAdapter.getMorePostsInfoFragment();
                        if (fragment != null) {
                            fragment.setStatus(LoadingMorePostsStatus.FAILED);
                        }
                    });
                }
            });
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onProvidePostListToViewPostDetailActivityEvent(ProvidePostListToViewPostDetailActivityEvent event) {
        if (event.postFragmentId == postFragmentId && posts == null) {
            this.posts = event.posts;
            this.postType = event.postType;
            this.subredditName = event.subredditName;
            this.concatenatedSubredditNames = event.concatenatedSubredditNames;
            this.username = event.username;
            this.userWhere = event.userWhere;
            this.multiPath = event.multiPath;
            this.query = event.query;
            this.trendingSource = event.trendingSource;
            this.postFilter = event.postFilter;
            this.sortType = event.sortType.getType();
            this.sortTime = event.sortType.getTime();
            this.readPostList = event.readPostList;

            if (sectionsPagerAdapter != null) {
                if (postListPosition > 0)
                    sectionsPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_post_detail_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_reset_fab_position_view_post_detail_activity) {
            binding.fabViewPostDetailActivity.resetCoordinates();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_COMMENT_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(EditCommentActivity.RETURN_EXTRA_EDITED_COMMENT)) {
                    editComment((Comment) data.getParcelableExtra(EditCommentActivity.RETURN_EXTRA_EDITED_COMMENT),
                            data.getIntExtra(EditCommentActivity.RETURN_EXTRA_EDITED_COMMENT_POSITION, -1));
                } else {
                    editComment(data.getStringExtra(EditCommentActivity.RETURN_EXTRA_EDITED_COMMENT_CONTENT),
                            data.getIntExtra(EditCommentActivity.RETURN_EXTRA_EDITED_COMMENT_POSITION, -1));
                }
            }
        } else if (requestCode == CommentActivity.WRITE_COMMENT_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(RETURN_EXTRA_COMMENT_DATA_KEY)) {
                    ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment != null) {
                        Comment comment = data.getParcelableExtra(RETURN_EXTRA_COMMENT_DATA_KEY);
                        if (comment != null && comment.getDepth() == 0) {
                            fragment.addComment(comment);
                        } else {
                            String parentFullname = data.getStringExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY);
                            int parentPosition = data.getIntExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, -1);
                            if (parentFullname != null && parentPosition >= 0) {
                                fragment.addChildComment(comment, parentFullname, parentPosition);
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public void onBackPressed() {
        if (orientation == getResources().getConfiguration().orientation) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        Bridge.clear(this);
        BigImageViewer.imageLoader().cancelAll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVolumeKeysNavigateComments) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        fragment.scrollToPreviousParentComment();
                        return true;
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        fragment.scrollToNextParentComment();
                        return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.changeSortType(sortType);
            binding.toolbarViewPostDetailActivity.setTitle(sortType.getType().fullName);
        }
    }

    @Override
    public void onLongPress() {
        ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.goToTop();
        }
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ViewPostDetailFragment fragment = new ViewPostDetailFragment();
            Bundle bundle = new Bundle();
            if (posts != null) {
                if (postListPosition == position && post != null) {
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, post);
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, position);
                    bundle.putString(ViewPostDetailFragment.EXTRA_SINGLE_COMMENT_ID, getIntent().getStringExtra(EXTRA_SINGLE_COMMENT_ID));
                    bundle.putString(ViewPostDetailFragment.EXTRA_CONTEXT_NUMBER, getIntent().getStringExtra(EXTRA_CONTEXT_NUMBER));
                    bundle.putString(ViewPostDetailFragment.EXTRA_MESSAGE_FULLNAME, getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME));
                } else {
                    if (position >= posts.size()) {
                        MorePostsInfoFragment morePostsInfoFragment = new MorePostsInfoFragment();
                        Bundle moreBundle = new Bundle();
                        moreBundle.putInt(MorePostsInfoFragment.EXTRA_STATUS, loadingMorePostsStatus);
                        morePostsInfoFragment.setArguments(moreBundle);
                        return morePostsInfoFragment;
                    }
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, posts.get(position));
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, position);
                }
            } else {
                if (post == null) {
                    bundle.putString(ViewPostDetailFragment.EXTRA_POST_ID, getIntent().getStringExtra(EXTRA_POST_ID));
                } else {
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, post);
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, postListPosition);
                }
                bundle.putString(ViewPostDetailFragment.EXTRA_SINGLE_COMMENT_ID, getIntent().getStringExtra(EXTRA_SINGLE_COMMENT_ID));
                bundle.putString(ViewPostDetailFragment.EXTRA_CONTEXT_NUMBER, getIntent().getStringExtra(EXTRA_CONTEXT_NUMBER));
                bundle.putString(ViewPostDetailFragment.EXTRA_MESSAGE_FULLNAME, getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME));
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return posts == null ? 1 : posts.size() + 1;
        }

        @Nullable
        ViewPostDetailFragment getCurrentFragment() {
            if (fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + binding.viewPager2ViewPostDetailActivity.getCurrentItem());
            if (fragment instanceof ViewPostDetailFragment) {
                return (ViewPostDetailFragment) fragment;
            }
            return null;
        }

        @Nullable
        MorePostsInfoFragment getMorePostsInfoFragment() {
            if (posts == null || fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + posts.size());
            if (fragment instanceof MorePostsInfoFragment) {
                return (MorePostsInfoFragment) fragment;
            }
            return null;
        }
    }
}
