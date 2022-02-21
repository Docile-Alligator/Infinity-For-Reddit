package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.account.AccountViewModel;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.navigationdrawer.NavigationDrawerRecyclerViewMergedAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThings;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccount;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchToAnonymousMode;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.ChangeDisableSwipingBetweenTabsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeRequireAuthToAccountSectionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditViewModel;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.user.FetchUserData;
import ml.docilealligator.infinityforreddit.user.UserData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RandomBottomSheetFragment.RandomOptionSelectionCallback, MarkPostAsReadInterface, RecyclerViewContentScrollingInterface {

    static final String EXTRA_MESSSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String FETCH_SUBSCRIPTIONS_STATE = "FSS";
    private static final String DRAWER_ON_ACCOUNT_SWITCH_STATE = "DOASS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";
    private static final String INBOX_COUNT_STATE = "ICS";

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 0;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.navigation_view_main_activity)
    NavigationView navigationView;
    @BindView(R.id.coordinator_layout_main_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_main_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.view_pager_main_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.collapsing_toolbar_layout_main_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    MaterialToolbar toolbar;
    @BindView(R.id.nav_drawer_recycler_view_main_activity)
    RecyclerView navDrawerRecyclerView;
    @BindView(R.id.tab_layout_main_activity)
    TabLayout tabLayout;
    @BindView(R.id.bottom_app_bar_bottom_app_bar)
    BottomAppBar bottomAppBar;
    @BindView(R.id.linear_layout_bottom_app_bar)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.option_1_bottom_app_bar)
    ImageView option1BottomAppBar;
    @BindView(R.id.option_2_bottom_app_bar)
    ImageView option2BottomAppBar;
    @BindView(R.id.option_3_bottom_app_bar)
    ImageView option3BottomAppBar;
    @BindView(R.id.option_4_bottom_app_bar)
    ImageView option4BottomAppBar;
    @BindView(R.id.fab_main_activity)
    FloatingActionButton fab;
    MultiRedditViewModel multiRedditViewModel;
    SubscribedSubredditViewModel subscribedSubredditViewModel;
    AccountViewModel accountViewModel;
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
    @Named("main_activity_tabs")
    SharedPreferences mMainActivityTabsSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences mBottomAppBarSharedPreference;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("navigation_drawer")
    SharedPreferences mNavigationDrawerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private NavigationDrawerRecyclerViewMergedAdapter adapter;
    private Call<String> subredditAutocompleteCall;
    private String mAccessToken;
    private String mAccountName;
    private boolean mFetchUserInfoSuccess = false;
    private boolean mFetchSubscriptionsSuccess = false;
    private boolean mDrawerOnAccountSwitch = false;
    private String mMessageFullname;
    private String mNewAccountName;
    private boolean showBottomAppBar;
    private int mBackButtonAction;
    private boolean mLockBottomAppBar;
    private boolean mDisableSwipingBetweenTabs;
    private boolean mShowFavoriteMultiReddits;
    private boolean mShowMultiReddits;
    private boolean mShowFavoriteSubscribedSubreddits;
    private boolean mShowSubscribedSubreddits;
    private int fabOption;
    private int inboxCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setTheme(R.style.AppTheme_NoActionBarWithTransparentStatusBar);

        setHasDrawerLayout();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                drawer.setStatusBarBackgroundColor(Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    drawer.setFitsSystemWindows(false);
                    getWindow().setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);
                    linearLayoutBottomAppBar.setPadding(0,
                            linearLayoutBottomAppBar.getPaddingTop(), 0, navBarHeight);
                    navDrawerRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            } else {
                drawer.setStatusBarBackgroundColor(mCustomThemeWrapper.getColorPrimaryDark());
            }
        }

        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (adapter != null) {
                    adapter.closeAccountSectionWithoutChangeIconResource(true);
                }
            }
        });
        toggle.syncState();

        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);
        mBackButtonAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION, "0"));
        mLockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);
        mDisableSwipingBetweenTabs = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false);

        fragmentManager = getSupportFragmentManager();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mFetchSubscriptionsSuccess = savedInstanceState.getBoolean(FETCH_SUBSCRIPTIONS_STATE);
            mDrawerOnAccountSwitch = savedInstanceState.getBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);
            inboxCount = savedInstanceState.getInt(INBOX_COUNT_STATE);
            initializeNotificationAndBindView(true);
        } else {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            initializeNotificationAndBindView(false);
        }
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
        int backgroundColor = mCustomThemeWrapper.getBackgroundColor();
        drawer.setBackgroundColor(backgroundColor);
        int bottomAppBarIconColor = mCustomThemeWrapper.getBottomAppBarIconColor();
        option1BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option2BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option3BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option4BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        navigationView.setBackgroundColor(backgroundColor);
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        bottomAppBar.setBackgroundTint(ColorStateList.valueOf(mCustomThemeWrapper.getBottomAppBarBackgroundColor()));
        applyFABTheme(fab);
    }

    private void initializeNotificationAndBindView(boolean doNotInitializeNotificationIfNoNewAccount) {
        boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
        long notificationInterval = Long.parseLong(mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));
        TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

        WorkManager workManager = WorkManager.getInstance(this);

        if (mNewAccountName != null) {
            if (mAccountName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;
                            if (newAccount != null) {
                                mAccessToken = newAccount.getAccessToken();
                                mAccountName = newAccount.getAccountName();
                            }

                            setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                            bindView();
                        });
            } else {
                setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                bindView();
            }
        } else {
            if (doNotInitializeNotificationIfNoNewAccount) {
                setNotification(workManager, notificationInterval, timeUnit, enableNotification);
            }

            bindView();
        }
    }

    private void setNotification(WorkManager workManager, long notificationInterval, TimeUnit timeUnit,
                                 boolean enableNotification) {
        if (enableNotification) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest pullNotificationRequest =
                    new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                            notificationInterval, timeUnit)
                            .setConstraints(constraints)
                            .build();

            workManager.enqueueUniquePeriodicWork(PullNotificationWorker.UNIQUE_WORKER_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
        } else {
            workManager.cancelUniqueWork(PullNotificationWorker.UNIQUE_WORKER_NAME);
        }
    }

    private void bottomAppBarOptionAction(int option) {
        switch (option) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX: {
                Intent intent = new Intent(this, InboxActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE: {
                Intent intent = new Intent(this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE: {
                changeSortType();
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT: {
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH: {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                goToSubreddit();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                goToUser();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM:
                randomThing();
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SAVED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GILDED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
            default:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;

        }
    }

    private int getBottomAppBarOptionDrawableResource(int option) {
        switch (option) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                return R.drawable.ic_subscritptions_bottom_app_bar_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                return R.drawable.ic_add_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH:
                return R.drawable.ic_refresh_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                return R.drawable.ic_sort_toolbar_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                return R.drawable.ic_post_layout_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH:
                return R.drawable.ic_search_black_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                return R.drawable.ic_subreddit_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                return R.drawable.ic_user_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM:
                return R.drawable.ic_random_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                return R.drawable.ic_hide_read_posts_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                return R.drawable.ic_filter_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED:
                return R.drawable.ic_arrow_upward_black_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                return R.drawable.ic_arrow_downward_black_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN:
                return R.drawable.ic_outline_lock_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED:
                return R.drawable.ic_outline_bookmarks_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GILDED:
                return R.drawable.ic_star_border_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
                return R.drawable.ic_keyboard_double_arrow_up_24;
            default:
                return R.drawable.ic_account_circle_24dp;
        }
    }

    private void bindView() {
        if (isDestroyed()) {
            return;
        }

        if (showBottomAppBar) {
            int optionCount = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
            int option1 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);
            int option2 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS);

            bottomAppBar.setVisibility(View.VISIBLE);

            if (optionCount == 2) {
                linearLayoutBottomAppBar.setWeightSum(3);
                option1BottomAppBar.setVisibility(View.GONE);
                option3BottomAppBar.setVisibility(View.GONE);

                option2BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option1));
                option4BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option2));

                option2BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option1);
                });

                option4BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option2);
                });
            } else {
                int option3 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, mAccessToken == null ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX);
                int option4 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, mAccessToken == null ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE);

                option1BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option1));
                option2BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option2));
                option3BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option3));
                option4BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option4));

                option1BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option1);
                });

                option2BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option2);
                });

                option3BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option3);
                });

                option4BottomAppBar.setOnClickListener(view -> {
                    bottomAppBarOptionAction(option4);
                });
            }
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            lp.setAnchorId(View.NO_ID);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            fab.setLayoutParams(lp);
        }

        fabOption = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB,
                SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH:
                fab.setImageResource(R.drawable.ic_refresh_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                fab.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                fab.setImageResource(R.drawable.ic_post_layout_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH:
                fab.setImageResource(R.drawable.ic_search_black_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                fab.setImageResource(R.drawable.ic_subreddit_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                fab.setImageResource(R.drawable.ic_user_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_RANDOM:
                fab.setImageResource(R.drawable.ic_random_24dp);
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (mAccessToken == null) {
                    fab.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    fab.setImageResource(R.drawable.ic_hide_read_posts_24dp);
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                fab.setImageResource(R.drawable.ic_filter_24dp);
                break;
            default:
                if (mAccessToken == null) {
                    fab.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    fab.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }
        fab.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {
                    changeSortType();
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                    postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH: {
                    Intent intent = new Intent(this, SearchActivity.class);
                    startActivity(intent);
                    break;
                }
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                    goToSubreddit();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                    goToUser();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_RANDOM:
                    randomThing();
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.hideReadPosts();
                    }
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.filterPosts();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        fab.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment= new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, mAccessToken == null);
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });
        fab.setVisibility(View.VISIBLE);

        adapter = new NavigationDrawerRecyclerViewMergedAdapter(this, mSharedPreferences,
                mNsfwAndSpoilerSharedPreferences, mNavigationDrawerSharedPreferences, mCustomThemeWrapper, mAccountName,
                new NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener() {
                    @Override
                    public void onMenuClick(int stringId) {
                        Intent intent = null;
                        if (stringId == R.string.profile) {
                            intent = new Intent(MainActivity.this, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                        } else if (stringId == R.string.subscriptions) {
                            if (mAccessToken != null) {
                                intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                            }
                        } else if (stringId == R.string.multi_reddit) {
                            intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                            intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                        } else if (stringId == R.string.rpan) {
                            intent = new Intent(MainActivity.this, RPANActivity.class);
                        } else if (stringId == R.string.trending) {
                            intent = new Intent(MainActivity.this, TrendingActivity.class);
                        } else if (stringId == R.string.upvoted) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                        } else if (stringId == R.string.downvoted) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                        } else if (stringId == R.string.hidden) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                        } else if (stringId == R.string.account_saved_thing_activity_label) {
                            intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
                        } else if (stringId == R.string.gilded) {
                            intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                        } else if (stringId == R.string.light_theme) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "0").apply();
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                            mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                        } else if (stringId == R.string.dark_theme) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "1").apply();
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                                mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                            } else {
                                mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                            }
                        } else if (stringId == R.string.enable_nsfw) {
                            if (sectionsPagerAdapter != null) {
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, true).apply();
                                sectionsPagerAdapter.changeNSFW(true);
                            }
                        } else if (stringId == R.string.disable_nsfw) {
                            if (sectionsPagerAdapter != null) {
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false).apply();
                                sectionsPagerAdapter.changeNSFW(false);
                            }
                        } else if (stringId == R.string.settings) {
                            intent = new Intent(MainActivity.this, SettingsActivity.class);
                        } else if (stringId == R.string.add_account) {
                            Intent addAccountIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivityForResult(addAccountIntent, LOGIN_ACTIVITY_REQUEST_CODE);
                        } else if (stringId == R.string.anonymous_account) {
                            SwitchToAnonymousMode.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                    mExecutor, new Handler(), false, () -> {
                                        Intent anonymousIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(anonymousIntent);
                                        finish();
                                    });
                        } else if (stringId == R.string.log_out) {
                            SwitchToAnonymousMode.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                    mExecutor, new Handler(), true,
                                    () -> {
                                        Intent logOutIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(logOutIntent);
                                        finish();
                                    });
                        }
                        if (intent != null) {
                            startActivity(intent);
                        }
                        drawer.closeDrawers();
                    }

                    @Override
                    public void onSubscribedSubredditClick(String subredditName) {
                        Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                        startActivity(intent);
                    }

                    @Override
                    public void onAccountClick(String accountName) {
                        SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                mExecutor, new Handler(), accountName, newAccount -> {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
        adapter.setInboxCount(inboxCount);
        navDrawerRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this));
        navDrawerRecyclerView.setAdapter(adapter.getConcatAdapter());

        int tabCount = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        mShowFavoriteMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false);
        mShowMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false);
        mShowFavoriteSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false);
        mShowSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, tabCount, mShowFavoriteMultiReddits,
                mShowMultiReddits, mShowFavoriteSubscribedSubreddits, mShowSubscribedSubreddits);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(1);
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
        if (mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true)) {
            if (mShowFavoriteMultiReddits || mShowMultiReddits || mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits) {
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
            new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                switch (position) {
                    case 0:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home)));
                        break;
                    case 1:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular)));
                        break;
                    case 2:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all)));
                        break;
                }
                if (position >= tabCount && (mShowFavoriteMultiReddits || mShowMultiReddits ||
                        mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits)
                        && sectionsPagerAdapter != null) {
                    if (position - tabCount < sectionsPagerAdapter.favoriteMultiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.favoriteMultiReddits.get(position - tabCount).getName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size() < sectionsPagerAdapter.multiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.multiReddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()).getName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size()
                            - sectionsPagerAdapter.multiReddits.size() < sectionsPagerAdapter.favoriteSubscribedSubreddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.favoriteSubscribedSubreddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()
                                - sectionsPagerAdapter.multiReddits.size()).getName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size()
                            - sectionsPagerAdapter.multiReddits.size()
                            - sectionsPagerAdapter.favoriteSubscribedSubreddits.size() < sectionsPagerAdapter.subscribedSubreddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.subscribedSubreddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()
                                - sectionsPagerAdapter.multiReddits.size()
                                - sectionsPagerAdapter.favoriteSubscribedSubreddits.size()).getName());
                    }
                }
            }).attach();
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (mAccessToken != null) {
                    if (showBottomAppBar) {
                        bottomAppBar.performShow();
                    }
                }
                fab.show();
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });

        fixViewPager2Sensitivity(viewPager2);

        loadSubscriptions();

        multiRedditViewModel = new ViewModelProvider(this, new MultiRedditViewModel.Factory(getApplication(),
                mRedditDataRoomDatabase, mAccountName == null ? "-" : mAccountName))
                .get(MultiRedditViewModel.class);

        multiRedditViewModel.getAllFavoriteMultiReddits().observe(this, multiReddits -> {
            if (mShowFavoriteMultiReddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setFavoriteMultiReddits(multiReddits);
            }
        });

        multiRedditViewModel.getAllMultiReddits().observe(this, multiReddits -> {
            if (mShowMultiReddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setMultiReddits(multiReddits);
            }
        });

        subscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName == null ? "-" : mAccountName))
                .get(SubscribedSubredditViewModel.class);
        subscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this,
                subscribedSubredditData -> {
                    adapter.setSubscribedSubreddits(subscribedSubredditData);
                    if (mShowSubscribedSubreddits && sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.setSubscribedSubreddits(subscribedSubredditData);
                    }
                });
        subscribedSubredditViewModel.getAllFavoriteSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            adapter.setFavoriteSubscribedSubreddits(subscribedSubredditData);
            if (mShowFavoriteSubscribedSubreddits && sectionsPagerAdapter != null) {
                sectionsPagerAdapter.setFavoriteSubscribedSubreddits(subscribedSubredditData);
            }
        });

        accountViewModel = new ViewModelProvider(this,
                new AccountViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName)).get(AccountViewModel.class);
        accountViewModel.getAccountsExceptCurrentAccountLiveData().observe(this, adapter::changeAccountsDataset);
        accountViewModel.getCurrentAccountLiveData().observe(this, account -> {
            if (account != null) {
                adapter.updateAccountInfo(account.getProfileImageUrl(), account.getBannerImageUrl(),
                        account.getKarma());
            }
        });

        loadUserData();

        if (mAccessToken != null) {
            if (mMessageFullname != null) {
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
        }
    }

    private void loadSubscriptions() {
        if (mAccessToken != null && !mFetchSubscriptionsSuccess) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, mAccessToken, mAccountName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            InsertSubscribedThings.insertSubscribedThings(
                                    mExecutor,
                                    new Handler(),
                                    mRedditDataRoomDatabase,
                                    mAccountName,
                                    subscribedSubredditData,
                                    subscribedUserData,
                                    subredditData,
                                    () -> mFetchSubscriptionsSuccess = true);
                        }

                        @Override
                        public void onFetchSubscribedThingFail() {
                            mFetchSubscriptionsSuccess = false;
                        }
                    });
        }
    }

    private void loadUserData() {
        if (!mFetchUserInfoSuccess) {
            FetchUserData.fetchUserData(mRedditDataRoomDatabase, mOauthRetrofit, mAccessToken,
                    mAccountName, new FetchUserData.FetchUserDataListener() {
                @Override
                public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                    MainActivity.this.inboxCount = inboxCount;
                    mAccountName = userData.getName();
                    mFetchUserInfoSuccess = true;
                    if (adapter != null) {
                        adapter.setInboxCount(inboxCount);
                    }
                }

                @Override
                public void onFetchUserDataFailed() {
                    mFetchUserInfoSuccess = false;
                }
            });
            /*FetchMyInfo.fetchAccountInfo(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                    new FetchMyInfo.FetchMyInfoListener() {
                        @Override
                        public void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                            mAccountName = name;
                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onFetchMyInfoFailed(boolean parseFailed) {
                            mFetchUserInfoSuccess = false;
                        }
                    });*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    private void changeSortType() {
        int currentPostType = sectionsPagerAdapter.getCurrentPostType();
        Bundle bundle = new Bundle();
        bundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, currentPostType != PostPagingSource.TYPE_FRONT_PAGE);
        SortTypeBottomSheetFragment sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        sortTypeBottomSheetFragment.setArguments(bundle);
        sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_main_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_sort_main_activity) {
            changeSortType();
            return true;
        } else if (itemId == R.id.action_refresh_main_activity) {
            sectionsPagerAdapter.refresh();
            mFetchUserInfoSuccess = false;
            loadUserData();
            return true;
        } else if (itemId == R.id.action_change_post_layout_main_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_CONFIRM_EXIT) {
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.exit_app)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> finish())
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_OPEN_NAVIGATION_DRAWER) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(FETCH_SUBSCRIPTIONS_STATE, mFetchSubscriptionsSuccess);
        outState.putBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE, mDrawerOnAccountSwitch);
        outState.putString(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
        outState.putInt(INBOX_COUNT_STATE, inboxCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        sectionsPagerAdapter.changeSortType(sortType);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void postTypeSelected(int postType) {
        Intent intent;
        switch (postType) {
            case PostTypeBottomSheetFragment.TYPE_TEXT:
                intent = new Intent(MainActivity.this, PostTextActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(MainActivity.this, PostLinkActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(MainActivity.this, PostImageActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(MainActivity.this, PostVideoActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_GALLERY:
                intent = new Intent(MainActivity.this, PostGalleryActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_POLL:
                intent = new Intent(MainActivity.this, PostPollActivity.class);
                startActivity(intent);
        }
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    @Override
    public void contentScrollUp() {
        if (showBottomAppBar && !mLockBottomAppBar) {
            bottomAppBar.performShow();
        }
        if (!(showBottomAppBar && mLockBottomAppBar)) {
            fab.show();
        }
    }

    @Override
    public void contentScrollDown() {
        if (!(showBottomAppBar && mLockBottomAppBar)) {
            fab.hide();
        }
        if (showBottomAppBar && !mLockBottomAppBar) {
            bottomAppBar.performHide();
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
        if (adapter != null) {
            adapter.setNSFWEnabled(changeNSFWEvent.nsfw);
        }
    }

    @Subscribe
    public void onRecreateActivityEvent(RecreateActivityEvent recreateActivityEvent) {
        ActivityCompat.recreate(this);
    }

    @Subscribe
    public void onChangeLockBottomAppBar(ChangeLockBottomAppBarEvent changeLockBottomAppBarEvent) {
        mLockBottomAppBar = changeLockBottomAppBarEvent.lockBottomAppBar;
    }

    @Subscribe
    public void onChangeDisableSwipingBetweenTabsEvent(ChangeDisableSwipingBetweenTabsEvent changeDisableSwipingBetweenTabsEvent) {
        mDisableSwipingBetweenTabs = changeDisableSwipingBetweenTabsEvent.disableSwipingBetweenTabs;
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
    }

    @Subscribe
    public void onChangeRequireAuthToAccountSectionEvent(ChangeRequireAuthToAccountSectionEvent changeRequireAuthToAccountSectionEvent) {
        if (adapter != null) {
            adapter.setRequireAuthToAccountSection(changeRequireAuthToAccountSectionEvent.requireAuthToAccountSection);
        }
    }

    @Subscribe
    public void onChangeShowAvatarOnTheRightInTheNavigationDrawerEvent(ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent event) {
        if (adapter != null) {
            adapter.setShowAvatarOnTheRightInTheNavigationDrawer(event.showAvatarOnTheRightInTheNavigationDrawer);
            int previousPosition = -1;
            if (navDrawerRecyclerView.getLayoutManager() != null) {
                previousPosition = ((LinearLayoutManagerBugFixed) navDrawerRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            }

            RecyclerView.LayoutManager layoutManager = navDrawerRecyclerView.getLayoutManager();
            navDrawerRecyclerView.setAdapter(null);
            navDrawerRecyclerView.setLayoutManager(null);
            navDrawerRecyclerView.setAdapter(adapter.getConcatAdapter());
            navDrawerRecyclerView.setLayoutManager(layoutManager);

            if (previousPosition > 0) {
                navDrawerRecyclerView.scrollToPosition(previousPosition);
            }
        }
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.displaySortTypeInToolbar();
        }
    }

    @Override
    public void fabOptionSelected(int option) {
        switch (option) {
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SUBMIT_POST:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_REFRESH:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_SORT_TYPE:
                changeSortType();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_POST_LAYOUT:
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_SUBREDDIT: {
                goToSubreddit();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_USER: {
                goToUser();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_RANDOM: {
                randomThing();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_HIDE_READ_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_FILTER_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            }
        }
    }

    private void goToSubreddit() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            if (imm != null) {
                imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
            }
            Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        thingEditText.requestFocus();
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (imm != null) {
                    imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                }
                Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                startActivity(subredditIntent);
                return true;
            }
            return false;
        });

        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);
        thingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (subredditAutocompleteCall != null) {
                    subredditAutocompleteCall.cancel();
                }
                subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                        editable.toString(), nsfw);
                subredditAutocompleteCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                @Override
                                public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                    adapter.setSubreddits(subredditData);
                                }

                                @Override
                                public void onParseSubredditListingDataFail() {

                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                    }
                });
            }
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_subreddit)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                    Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                    subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                    startActivity(subredditIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .setOnDismissListener(dialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    private void goToUser() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        thingEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (imm != null) {
                    imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                }
                Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                startActivity(userIntent);
                return true;
            }
            return false;
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_user)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                    Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                    userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                    startActivity(userIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .setOnDismissListener(dialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    private void randomThing() {
        RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false));
        randomBottomSheetFragment.setArguments(bundle);
        randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    @Override
    public void markPostAsRead(Post post) {
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountName, post.getId());
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {
        int tabCount;
        boolean showFavoriteMultiReddits;
        boolean showMultiReddits;
        boolean showFavoriteSubscribedSubreddits;
        boolean showSubscribedSubreddits;
        List<MultiReddit> favoriteMultiReddits;
        List<MultiReddit> multiReddits;
        List<SubscribedSubredditData> favoriteSubscribedSubreddits;
        List<SubscribedSubredditData> subscribedSubreddits;

        SectionsPagerAdapter(FragmentActivity fa, int tabCount, boolean showFavoriteMultiReddits,
                             boolean showMultiReddits, boolean showFavoriteSubscribedSubreddits,
                             boolean showSubscribedSubreddits) {
            super(fa);
            this.tabCount = tabCount;
            favoriteMultiReddits = new ArrayList<>();
            multiReddits = new ArrayList<>();
            favoriteSubscribedSubreddits = new ArrayList<>();
            subscribedSubreddits = new ArrayList<>();
            this.showFavoriteMultiReddits = showFavoriteMultiReddits;
            this.showMultiReddits = showMultiReddits;
            this.showFavoriteSubscribedSubreddits = showFavoriteSubscribedSubreddits;
            this.showSubscribedSubreddits = showSubscribedSubreddits;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
                return generatePostFragment(postType, name);
            } else {
                if (showFavoriteMultiReddits) {
                    if (position >= tabCount && position - tabCount < favoriteMultiReddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT;
                        String name = favoriteMultiReddits.get(position - tabCount).getPath();
                        return generatePostFragment(postType, name);
                    }
                }

                if (showMultiReddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size() < multiReddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT;
                        String name = multiReddits.get(position - tabCount - favoriteMultiReddits.size()).getPath();
                        return generatePostFragment(postType, name);
                    }
                }

                if (showFavoriteSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size()
                            - multiReddits.size() < favoriteSubscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = favoriteSubscribedSubreddits.get(position - tabCount
                                - favoriteMultiReddits.size()
                                - multiReddits.size()).getName();
                        return generatePostFragment(postType, name);
                    }
                }
                if (showSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount - favoriteMultiReddits.size()
                            - multiReddits.size() - favoriteSubscribedSubreddits.size() < subscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = subscribedSubreddits.get(position - tabCount - favoriteMultiReddits.size()
                                - multiReddits.size() - favoriteSubscribedSubreddits.size()).getName();
                        return generatePostFragment(postType, name);
                    }
                }

                int postType;
                String name;
                if (position == 1) {
                     postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
                     name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
                } else {
                    postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
                    name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
                }
                return generatePostFragment(postType, name);
            }
        }

        public void setFavoriteMultiReddits(List<MultiReddit> favoriteMultiReddits) {
            this.favoriteMultiReddits = favoriteMultiReddits;
            notifyDataSetChanged();
        }

        public void setMultiReddits(List<MultiReddit> multiReddits) {
            this.multiReddits = multiReddits;
            notifyDataSetChanged();
        }

        public void setFavoriteSubscribedSubreddits(List<SubscribedSubredditData> favoriteSubscribedSubreddits) {
            this.favoriteSubscribedSubreddits = favoriteSubscribedSubreddits;
            notifyDataSetChanged();
        }

        public void setSubscribedSubreddits(List<SubscribedSubredditData> subscribedSubreddits) {
            this.subscribedSubreddits = subscribedSubreddits;
            notifyDataSetChanged();
        }

        private Fragment generatePostFragment(int postType, String name) {
            if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, mAccessToken == null ? PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE : PostPagingSource.TYPE_FRONT_PAGE);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, mAccessToken == null ? PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT : PostPagingSource.TYPE_MULTI_REDDIT);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, name);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SUBMITTED);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SAVED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_GILDED) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, mAccountName);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                bundle.putBoolean(PostFragment.EXTRA_DISABLE_READ_POSTS, true);

                if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SAVED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SAVED);
                } else {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                }

                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getItemCount() {
            return tabCount + favoriteMultiReddits.size() + multiReddits.size() +
                    favoriteSubscribedSubreddits.size() + subscribedSubreddits.size();
        }

        @Nullable
        private PostFragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
            if (fragment instanceof PostFragment) {
                return (PostFragment) fragment;
            }
            return null;
        }

        boolean handleKeyDown(int keyCode) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.handleKeyDown(keyCode);
            }
            return false;
        }

        int getCurrentPostType() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.getPostType();
            }
            return PostPagingSource.TYPE_SUBREDDIT;
        }

        void changeSortType(SortType sortType) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changeSortType(sortType);
            }
            displaySortTypeInToolbar();
        }

        public void refresh() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.refresh();
            }
        }

        void changeNSFW(boolean nsfw) {
            for (int i = 0; i < getItemCount(); i++) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + i);
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).changeNSFW(nsfw);
                }
            }
        }

        void changePostLayout(int postLayout) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                SortType sortType = currentFragment.getSortType();
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            }
        }

        void hideReadPosts() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.hideReadPosts();
            }
        }

        void filterPosts() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.filterPosts();
            }
        }
    }
}