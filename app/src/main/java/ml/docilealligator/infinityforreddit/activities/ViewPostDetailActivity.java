package ml.docilealligator.infinityforreddit.activities;

import static ml.docilealligator.infinityforreddit.activities.CommentActivity.RETURN_EXTRA_COMMENT_DATA_KEY;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.livefront.bridge.Bridge;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.LoadingMorePostsStatus;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccount;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
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
    public static final int GIVE_AWARD_REQUEST_CODE = 100;
    @State
    String mNewAccountName;
    @BindView(R.id.coordinator_layout_view_post_detail)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_view_post_detail_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_post_detail_activity)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar_view_post_detail_activity)
    Toolbar mToolbar;
    @BindView(R.id.view_pager_2_view_post_detail_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.fab_view_post_detail_activity)
    FloatingActionButton fab;
    @BindView(R.id.search_panel_material_card_view_view_post_detail_activity)
    MaterialCardView searchPanelMaterialCardView;
    @BindView(R.id.search_text_input_layout_view_post_detail_activity)
    TextInputLayout searchTextInputLayout;
    @BindView(R.id.search_text_input_edit_text_view_post_detail_activity)
    TextInputEditText searchTextInputEditText;
    @BindView(R.id.previous_result_image_view_view_post_detail_activity)
    ImageView previousResultImageView;
    @BindView(R.id.next_result_image_view_view_post_detail_activity)
    ImageView nextResultImageView;
    @BindView(R.id.close_search_panel_image_view_view_post_detail_activity)
    ImageView closeSearchPanelImageView;
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
    private SlidrInterface mSlidrInterface;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private String mAccessToken;
    private String mAccountName;
    private long postFragmentId;
    private int postListPosition;
    private int orientation;
    private boolean mVolumeKeysNavigateComments;
    private boolean isNsfwSubreddit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        BigImageViewer.initialize(GlideImageLoader.with(this.getApplicationContext()));

        setContentView(R.layout.activity_view_post_detail);

        Bridge.restoreInstanceState(this, savedInstanceState);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);

                    searchPanelMaterialCardView.setContentPadding(searchPanelMaterialCardView.getPaddingStart(),
                            searchPanelMaterialCardView.getPaddingTop(),
                            searchPanelMaterialCardView.getPaddingEnd(),
                            searchPanelMaterialCardView.getPaddingBottom() + navBarHeight);
                }
            }
        }

        boolean swipeBetweenPosts = mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_BETWEEN_POSTS, false);
        if (!swipeBetweenPosts) {
            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
                mSlidrInterface = Slidr.attach(this);
            }
            viewPager2.setUserInputEnabled(false);
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

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        if (savedInstanceState == null) {
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        mVolumeKeysNavigateComments = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOLUME_KEYS_NAVIGATE_COMMENTS, false);

        fab.setOnClickListener(view -> {
            if (sectionsPagerAdapter != null) {
                ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (fragment != null) {
                    fragment.scrollToNextParentComment();
                }
            }
        });

        fab.setOnLongClickListener(view -> {
            if (sectionsPagerAdapter != null) {
                ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (fragment != null) {
                    fragment.scrollToPreviousParentComment();
                    return true;
                }
            }
            return false;
        });

        if (mAccessToken == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            searchTextInputEditText.setImeOptions(searchTextInputEditText.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        if (loadingMorePostsStatus == LoadingMorePostsStatus.LOADING) {
            loadingMorePostsStatus = LoadingMorePostsStatus.NOT_LOADING;
            fetchMorePosts(false);
        }

        checkNewAccountAndBindView(savedInstanceState);
    }

    public void setTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    public void showFab() {
        fab.show();
    }

    public void hideFab() {
        fab.hide();
    }

    public void showSnackBar(int resId) {
        Snackbar.make(mCoordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(mAppBarLayout, mCollapsingToolbarLayout, mToolbar);
        applyFABTheme(fab);
        searchPanelMaterialCardView.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimary()));
        int searchPanelTextAndIconColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        searchTextInputLayout.setBoxStrokeColor(searchPanelTextAndIconColor);
        searchTextInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(searchPanelTextAndIconColor));
        searchTextInputEditText.setTextColor(searchPanelTextAndIconColor);
        previousResultImageView.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        nextResultImageView.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        closeSearchPanelImageView.setColorFilter(searchPanelTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if (typeface != null) {
            searchTextInputLayout.setTypeface(typeface);
            searchTextInputEditText.setTypeface(typeface);
        }
    }

    private void checkNewAccountAndBindView(Bundle savedInstanceState) {
        if (mNewAccountName != null) {
            if (mAccountName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;

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
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.setAdapter(sectionsPagerAdapter);
        if (savedInstanceState == null) {
            viewPager2.setCurrentItem(getIntent().getIntExtra(EXTRA_POST_LIST_POSITION, 0), false);
        }
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (posts != null && position > posts.size() - 5) {
                    fetchMorePosts(false);
                }
            }
        });

        searchPanelMaterialCardView.setOnClickListener(null);
        
        nextResultImageView.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                searchComment(fragment, true);
            }
        });

        previousResultImageView.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                searchComment(fragment, false);
            }
        });

        closeSearchPanelImageView.setOnClickListener(view -> {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.resetSearchCommentIndex();
            }

            searchPanelMaterialCardView.setVisibility(View.GONE);
        });
    }

    public boolean isNsfwSubreddit() {
        return isNsfwSubreddit;
    }

    private void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.editComment(commentAuthor, commentContentMarkdown, position);
            }
        }
    }

    private void awardGiven(String awardsHTML, int awardCount, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.awardGiven(awardsHTML, awardCount, position);
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

    public void showRemovedComment(Comment comment, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.showRemovedComment(comment, position);
            }
        }
    }

    public void saveComment(@NonNull Comment comment, int position) {
        if (comment.isSaved()) {
            comment.setSaved(false);
            SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
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
            SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
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
        if (searchPanelMaterialCardView.getVisibility() == View.GONE) {
            searchPanelMaterialCardView.setVisibility(View.VISIBLE);
            return false;
        } else {
            searchPanelMaterialCardView.setVisibility(View.GONE);
            searchTextInputEditText.setText("");
            return true;
        }
    }

    public void searchComment(ViewPostDetailFragment fragment, boolean searchNextComment) {
        if (!searchTextInputEditText.getText().toString().isEmpty()) {
            fragment.searchComment(searchTextInputEditText.getText().toString(), searchNextComment);
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
                RedditAPI api = (mAccessToken == null ? mRetrofit : mOauthRetrofit).create(RedditAPI.class);
                Call<String> call;
                String afterKey = posts.isEmpty() ? null : posts.get(posts.size() - 1).getFullName();
                switch (postType) {
                    case PostPagingSource.TYPE_SUBREDDIT:
                        if (mAccessToken == null) {
                            call = api.getSubredditBestPosts(subredditName, sortType, sortTime, afterKey);
                        } else {
                            call = api.getSubredditBestPostsOauth(subredditName, sortType,
                                   sortTime, afterKey, APIUtils.getOAuthHeader(mAccessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_USER:
                        if (mAccessToken == null) {
                            call = api.getUserPosts(username, afterKey, sortType, sortTime);
                        } else {
                            call = api.getUserPostsOauth(username, userWhere, afterKey, sortType,
                                    sortTime, APIUtils.getOAuthHeader(mAccessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_SEARCH:
                        if (subredditName == null) {
                            if (mAccessToken == null) {
                                call = api.searchPosts(query, afterKey, sortType, sortTime,
                                        trendingSource);
                            } else {
                                call = api.searchPostsOauth(query, afterKey, sortType,
                                        sortTime, trendingSource, APIUtils.getOAuthHeader(mAccessToken));
                            }
                        } else {
                            if (mAccessToken == null) {
                                call = api.searchPostsInSpecificSubreddit(subredditName, query,
                                        sortType, sortTime, afterKey);
                            } else {
                                call = api.searchPostsInSpecificSubredditOauth(subredditName, query,
                                        sortType, sortTime, afterKey,
                                        APIUtils.getOAuthHeader(mAccessToken));
                            }
                        }
                        break;
                    case PostPagingSource.TYPE_MULTI_REDDIT:
                        if (mAccessToken == null) {
                            call = api.getMultiRedditPosts(multiPath, afterKey, sortTime);
                        } else {
                            call = api.getMultiRedditPostsOauth(multiPath, afterKey,
                                    sortTime, APIUtils.getOAuthHeader(mAccessToken));
                        }
                        break;
                    case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                        //case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT
                        call = api.getSubredditBestPosts(subredditName, sortType, sortTime, afterKey);
                        break;
                    default:
                        call = api.getBestPosts(sortType, sortTime, afterKey,
                                APIUtils.getOAuthHeader(mAccessToken));
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
                                        viewPager2.setCurrentItem(currentPostsSize - 1, false);
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
            mExecutor.execute((Runnable) () -> {
                long lastItem = 0;
                if (!posts.isEmpty()) {
                    lastItem = mRedditDataRoomDatabase.readPostDao().getReadPost(posts.get(posts.size() - 1).getId()).getTime();
                }
                List<ReadPost> readPosts = mRedditDataRoomDatabase.readPostDao().getAllReadPosts(mAccountName, lastItem);
                StringBuilder ids = new StringBuilder();
                for (ReadPost readPost : readPosts) {
                    ids.append("t3_").append(readPost.getId()).append(",");
                }
                if (ids.length() > 0) {
                    ids.deleteCharAt(ids.length() - 1);
                }

                Call<String> historyPosts;
                if (mAccessToken != null && !mAccessToken.isEmpty()) {
                    historyPosts = mOauthRetrofit.create(RedditAPI.class).getInfoOauth(ids.toString(), APIUtils.getOAuthHeader(mAccessToken));
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
                                        viewPager2.setCurrentItem(currentPostsSize - 1, false);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_COMMENT_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                editComment(null,
                        data.getStringExtra(EditCommentActivity.EXTRA_EDITED_COMMENT_CONTENT),
                        data.getExtras().getInt(EditCommentActivity.EXTRA_EDITED_COMMENT_POSITION));
            }
        } else if (requestCode == GIVE_AWARD_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, R.string.give_award_success, Toast.LENGTH_SHORT).show();
                int position = data.getIntExtra(GiveAwardActivity.EXTRA_RETURN_ITEM_POSITION, 0);
                String newAwardsHTML = data.getStringExtra(GiveAwardActivity.EXTRA_RETURN_NEW_AWARDS);
                int newAwardsCount = data.getIntExtra(GiveAwardActivity.EXTRA_RETURN_NEW_AWARDS_COUNT, 0);
                awardGiven(newAwardsHTML, newAwardsCount, position);
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
            mToolbar.setTitle(sortType.getType().fullName);
        }
    }

    @Override
    public void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    @Override
    public void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
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
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
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
