package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.thing.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.post.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountViewModel;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.navigationdrawer.NavigationDrawerRecyclerViewMergedAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.AccountManagement;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThings;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.NavigationWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityMainBinding;
import ml.docilealligator.infinityforreddit.events.ChangeDisableSwipingBetweenTabsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideFabInPostFeedEvent;
import ml.docilealligator.infinityforreddit.events.ChangeHideKarmaEvent;
import ml.docilealligator.infinityforreddit.events.ChangeInboxCountEvent;
import ml.docilealligator.infinityforreddit.events.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.ChangeRequireAuthToAccountSectionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeShowAvatarOnTheRightInTheNavigationDrawerEvent;
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent;
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

    static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String FETCH_SUBSCRIPTIONS_STATE = "FSS";
    private static final String DRAWER_ON_ACCOUNT_SWITCH_STATE = "DOASS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";
    private static final String INBOX_COUNT_STATE = "ICS";

    MultiRedditViewModel multiRedditViewModel;
    SubscribedSubredditViewModel subscribedSubredditViewModel;
    AccountViewModel accountViewModel;
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
    @Named("security")
    SharedPreferences mSecuritySharedPreferences;
    @Inject
    @Named("internal")
    SharedPreferences mInternalSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private NavigationDrawerRecyclerViewMergedAdapter adapter;
    private NavigationWrapper navigationWrapper;
    private Runnable autoCompleteRunnable;
    private Call<String> subredditAutocompleteCall;
    private boolean mFetchUserInfoSuccess = false;
    private boolean mFetchSubscriptionsSuccess = false;
    private boolean mDrawerOnAccountSwitch = false;
    private String mMessageFullname;
    private String mNewAccountName;
    private boolean hideFab;
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
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        setTheme(R.style.AppTheme_NoActionBarWithTransparentStatusBar);

        setHasDrawerLayout();

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAB_IN_POST_FEED, false);
        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);

        navigationWrapper = new NavigationWrapper(findViewById(R.id.bottom_app_bar_bottom_app_bar), findViewById(R.id.linear_layout_bottom_app_bar),
                findViewById(R.id.option_1_bottom_app_bar), findViewById(R.id.option_2_bottom_app_bar),
                findViewById(R.id.option_3_bottom_app_bar), findViewById(R.id.option_4_bottom_app_bar),
                findViewById(R.id.fab_main_activity),
                findViewById(R.id.navigation_rail), showBottomAppBar);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.includedAppBar.appbarLayoutMainActivity);
            }

            if (isImmersiveInterface()) {
                binding.drawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    binding.drawerLayout.setFitsSystemWindows(false);
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.includedAppBar.toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    if (navigationWrapper.navigationRailView == null) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
                        params.bottomMargin += navBarHeight;
                        navigationWrapper.floatingActionButton.setLayoutParams(params);
                    }
                    if (navigationWrapper.bottomAppBar != null) {
                        navigationWrapper.linearLayoutBottomAppBar.setPadding(navigationWrapper.linearLayoutBottomAppBar.getPaddingLeft(),
                                navigationWrapper.linearLayoutBottomAppBar.getPaddingTop(), navigationWrapper.linearLayoutBottomAppBar.getPaddingRight(), navBarHeight);
                    }
                    binding.navDrawerRecyclerViewMainActivity.setPadding(0, 0, 0, navBarHeight);
                }
            } else {
                binding.drawerLayout.setStatusBarBackgroundColor(mCustomThemeWrapper.getColorPrimaryDark());
            }
        }

        setSupportActionBar(binding.includedAppBar.toolbar);
        setToolbarGoToTop(binding.includedAppBar.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.includedAppBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.drawerLayout.addDrawerListener(toggle);
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (adapter != null) {
                    adapter.closeAccountManagement(true);
                }
            }
        });
        toggle.syncState();

        mViewPager2 = binding.includedAppBar.viewPagerMainActivity;

        mBackButtonAction = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION, "0"));
        mLockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);
        mDisableSwipingBetweenTabs = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mFetchSubscriptionsSuccess = savedInstanceState.getBoolean(FETCH_SUBSCRIPTIONS_STATE);
            mDrawerOnAccountSwitch = savedInstanceState.getBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);
            inboxCount = savedInstanceState.getInt(INBOX_COUNT_STATE);
        } else {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }

        /*if (!mInternalSharedPreferences.getBoolean(SharedPreferencesUtils.DO_NOT_SHOW_REDDIT_API_INFO_V2_AGAIN, false)) {
            ImportantInfoBottomSheetFragment fragment = new ImportantInfoBottomSheetFragment();
            fragment.setCancelable(false);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        }*/

        initializeNotificationAndBindView();
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
        int backgroundColor = mCustomThemeWrapper.getBackgroundColor();
        binding.drawerLayout.setBackgroundColor(backgroundColor);
        navigationWrapper.applyCustomTheme(mCustomThemeWrapper.getBottomAppBarIconColor(), mCustomThemeWrapper.getBottomAppBarBackgroundColor());
        binding.navigationViewMainActivity.setBackgroundColor(backgroundColor);
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.includedAppBar.appbarLayoutMainActivity, binding.includedAppBar.collapsingToolbarLayoutMainActivity, binding.includedAppBar.toolbar);
        applyTabLayoutTheme(binding.includedAppBar.tabLayoutMainActivity);
        applyFABTheme(navigationWrapper.floatingActionButton);
    }

    private void initializeNotificationAndBindView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> mInternalSharedPreferences.edit().putBoolean(SharedPreferencesUtils.HAS_REQUESTED_NOTIFICATION_PERMISSION, true).apply());

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (!mInternalSharedPreferences.getBoolean(SharedPreferencesUtils.HAS_REQUESTED_NOTIFICATION_PERMISSION, false)) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }

        boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
        long notificationInterval = Long.parseLong(mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));
        TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

        WorkManager workManager = WorkManager.getInstance(this);

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

                            setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                            bindView();
                        });
            } else {
                setNotification(workManager, notificationInterval, timeUnit, enableNotification);

                bindView();
            }
        } else {
            setNotification(workManager, notificationInterval, timeUnit, enableNotification);

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
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, accountName);
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
                Intent intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
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
                return R.drawable.ic_subscriptions_bottom_app_bar_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                return R.drawable.ic_add_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH:
                return R.drawable.ic_refresh_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                return R.drawable.ic_sort_toolbar_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                return R.drawable.ic_post_layout_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH:
                return R.drawable.ic_search_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                return R.drawable.ic_subreddit_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                return R.drawable.ic_user_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM:
                return R.drawable.ic_random_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                return R.drawable.ic_hide_read_posts_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                return R.drawable.ic_filter_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED:
                return R.drawable.ic_arrow_upward_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                return R.drawable.ic_arrow_downward_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN:
                return R.drawable.ic_lock_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED:
                return R.drawable.ic_bookmarks_day_night_24dp;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
                return R.drawable.ic_keyboard_double_arrow_up_day_night_24dp;
            default:
                return R.drawable.ic_account_circle_day_night_24dp;
        }
    }

    private void bindView() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (showBottomAppBar) {
            int optionCount = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
            int option1 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);
            int option2 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS);

            if (optionCount == 2) {
                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1), getBottomAppBarOptionDrawableResource(option2));

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    setBottomAppBarContentDescription(navigationWrapper.option2BottomAppBar, option1);
                    setBottomAppBarContentDescription(navigationWrapper.option4BottomAppBar, option2);
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        }
                        return false;
                    });
                }
            } else {
                int option3 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX);
                int option4 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE : SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE);

                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1),
                        getBottomAppBarOptionDrawableResource(option2), getBottomAppBarOptionDrawableResource(option3),
                        getBottomAppBarOptionDrawableResource(option4));

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option1BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    navigationWrapper.option3BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option3);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option4);
                    });

                    setBottomAppBarContentDescription(navigationWrapper.option1BottomAppBar, option1);
                    setBottomAppBarContentDescription(navigationWrapper.option2BottomAppBar, option2);
                    setBottomAppBarContentDescription(navigationWrapper.option3BottomAppBar, option3);
                    setBottomAppBarContentDescription(navigationWrapper.option4BottomAppBar, option4);
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_3) {
                            bottomAppBarOptionAction(option3);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_4) {
                            bottomAppBarOptionAction(option4);
                            return true;
                        }
                        return false;
                    });
                }
            }
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
            lp.setAnchorId(View.NO_ID);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            navigationWrapper.floatingActionButton.setLayoutParams(lp);
        }

        fabOption = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB,
                SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_refresh_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_refresh));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_change_sort_type));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_post_layout_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_change_post_layout));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_search_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_search));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_subreddit_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_go_to_subreddit));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_user_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_go_to_user));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_RANDOM:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_random_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_random));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                    navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_filter_posts));
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_hide_read_posts_day_night_24dp);
                    navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_hide_read_posts));
                }
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_filter_posts));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_keyboard_double_arrow_up_day_night_24dp);
                navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_go_to_top));
                break;
            default:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                    navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_filter_posts));
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_add_day_night_24dp);
                    navigationWrapper.floatingActionButton.setContentDescription(getString(R.string.content_description_submit_post));
                }
                break;
        }
        navigationWrapper.floatingActionButton.setOnClickListener(view -> {
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
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.goBackToTop();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        navigationWrapper.floatingActionButton.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment= new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, accountName.equals(Account.ANONYMOUS_ACCOUNT));
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });
        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);

        adapter = new NavigationDrawerRecyclerViewMergedAdapter(this, mSharedPreferences,
                mNsfwAndSpoilerSharedPreferences, mNavigationDrawerSharedPreferences, mSecuritySharedPreferences,
                mCustomThemeWrapper, accountName, new NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener() {
                    @Override
                    public void onMenuClick(int stringId) {
                        Intent intent = null;
                        if (stringId == R.string.profile) {
                            intent = new Intent(MainActivity.this, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, accountName);
                        } else if (stringId == R.string.subscriptions) {
                            intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                        } else if (stringId == R.string.multi_reddit) {
                            intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                            intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                        } else if (stringId == R.string.history) {
                            intent = new Intent(MainActivity.this, HistoryActivity.class);
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
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, true).apply();
                                sectionsPagerAdapter.changeNSFW(true);
                            }
                        } else if (stringId == R.string.disable_nsfw) {
                            if (sectionsPagerAdapter != null) {
                                mNsfwAndSpoilerSharedPreferences.edit().putBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false).apply();
                                sectionsPagerAdapter.changeNSFW(false);
                            }
                        } else if (stringId == R.string.settings) {
                            intent = new Intent(MainActivity.this, SettingsActivity.class);
                        } else if (stringId == R.string.add_account) {
                            intent = new Intent(MainActivity.this, LoginActivity.class);
                        } else if (stringId == R.string.anonymous_account) {
                            AccountManagement.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                    mExecutor, new Handler(), false, () -> {
                                        Intent anonymousIntent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(anonymousIntent);
                                        finish();
                                    });
                        } else if (stringId == R.string.log_out) {
                            AccountManagement.switchToAnonymousMode(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
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
                        binding.drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onSubscribedSubredditClick(String subredditName) {
                        Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                        startActivity(intent);
                    }

                    @Override
                    public void onAccountClick(@NonNull String accountName) {
                        AccountManagement.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                                mExecutor, new Handler(), accountName, newAccount -> {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

            @Override
            public void onAccountLongClick(@NonNull String accountName) {
                new MaterialAlertDialogBuilder(MainActivity.this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.log_out)
                        .setMessage(accountName)
                        .setPositiveButton(R.string.yes,
                                (dialogInterface, i) -> AccountManagement.removeAccount(mRedditDataRoomDatabase, mExecutor, accountName))
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
        adapter.setInboxCount(inboxCount);
        binding.navDrawerRecyclerViewMainActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this));
        binding.navDrawerRecyclerViewMainActivity.setAdapter(adapter.getConcatAdapter());

        int tabCount = mMainActivityTabsSharedPreferences.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        mShowFavoriteMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_MULTIREDDITS, false);
        mShowMultiReddits = mMainActivityTabsSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_MULTIREDDITS, false);
        mShowFavoriteSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false);
        mShowSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, tabCount, mShowFavoriteMultiReddits,
                mShowMultiReddits, mShowFavoriteSubscribedSubreddits, mShowSubscribedSubreddits);
        binding.includedAppBar.viewPagerMainActivity.setAdapter(sectionsPagerAdapter);
        binding.includedAppBar.viewPagerMainActivity.setUserInputEnabled(!mDisableSwipingBetweenTabs);
        if (mMainActivityTabsSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true)) {
            if (mShowFavoriteMultiReddits || mShowMultiReddits || mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits) {
                binding.includedAppBar.tabLayoutMainActivity.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                binding.includedAppBar.tabLayoutMainActivity.setTabMode(TabLayout.MODE_FIXED);
            }
            new TabLayoutMediator(binding.includedAppBar.tabLayoutMainActivity, binding.includedAppBar.viewPagerMainActivity, (tab, position) -> {
                switch (position) {
                    case 0:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home)));
                        break;
                    case 1:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular)));
                        break;
                    case 2:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all)));
                        break;
                }
                if (position >= tabCount && (mShowFavoriteMultiReddits || mShowMultiReddits ||
                        mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits)
                        && sectionsPagerAdapter != null) {
                    if (position - tabCount < sectionsPagerAdapter.favoriteMultiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.favoriteMultiReddits.get(position - tabCount).getDisplayName());
                    } else if (position - tabCount - sectionsPagerAdapter.favoriteMultiReddits.size() < sectionsPagerAdapter.multiReddits.size()) {
                        Utils.setTitleWithCustomFontToTab(typeface, tab, sectionsPagerAdapter.multiReddits.get(position - tabCount
                                - sectionsPagerAdapter.favoriteMultiReddits.size()).getDisplayName());
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
            binding.includedAppBar.tabLayoutMainActivity.setVisibility(View.GONE);
        }

        binding.includedAppBar.viewPagerMainActivity.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (showBottomAppBar) {
                    navigationWrapper.showNavigation();
                }
                if (!hideFab) {
                    navigationWrapper.showFab();
                }
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });

        fixViewPager2Sensitivity(binding.includedAppBar.viewPagerMainActivity);

        loadSubscriptions();

        multiRedditViewModel = new ViewModelProvider(this, new MultiRedditViewModel.Factory(
                mRedditDataRoomDatabase, accountName))
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
                new SubscribedSubredditViewModel.Factory(mRedditDataRoomDatabase, accountName))
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
                new AccountViewModel.Factory(mRedditDataRoomDatabase)).get(AccountViewModel.class);
        accountViewModel.getAccountsExceptCurrentAccountLiveData().observe(this, adapter::changeAccountsDataset);
        accountViewModel.getCurrentAccountLiveData().observe(this, account -> {
            if (account != null) {
                adapter.updateAccountInfo(account.getProfileImageUrl(), account.getBannerImageUrl(),
                        account.getKarma());
            }
        });

        loadUserData();

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            if (mMessageFullname != null) {
                ReadMessage.readMessage(mOauthRetrofit, accessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
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

    public void setBottomAppBarContentDescription(View view, int option) {
        switch (option) {
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                view.setContentDescription(getString(R.string.content_description_subscriptions));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX:
                view.setContentDescription(getString(R.string.content_description_inbox));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE:
                view.setContentDescription(getString(R.string.content_description_profile));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                view.setContentDescription(getString(R.string.content_description_multireddits));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                view.setContentDescription(getString(R.string.content_description_submit_post));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_REFRESH:
                view.setContentDescription(getString(R.string.content_description_refresh));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                view.setContentDescription(getString(R.string.content_description_change_sort_type));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                view.setContentDescription(getString(R.string.content_description_change_post_layout));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SEARCH:
                view.setContentDescription(getString(R.string.content_description_search));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT :
                view.setContentDescription(getString(R.string.content_description_go_to_subreddit));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_USER :
                view.setContentDescription(getString(R.string.content_description_go_to_user));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_RANDOM :
                view.setContentDescription(getString(R.string.content_description_random));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS :
                view.setContentDescription(getString(R.string.content_description_hide_read_posts));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_FILTER_POSTS :
                view.setContentDescription(getString(R.string.content_description_filter_posts));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_UPVOTED :
                view.setContentDescription(getString(R.string.content_description_upvoted));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_DOWNVOTED :
                view.setContentDescription(getString(R.string.content_description_downvoted));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_HIDDEN :
                view.setContentDescription(getString(R.string.content_description_hidden));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SAVED :
                view.setContentDescription(getString(R.string.content_description_saved));
                break;
            case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_GO_TO_TOP :
                view.setContentDescription(getString(R.string.content_description_go_to_top));
                break;
        }
    }

    private void loadSubscriptions() {
        if (System.currentTimeMillis() - mCurrentAccountSharedPreferences.getLong(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME, 0L) < 24 * 60 * 60 * 1000) {
            return;
        }

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT) && !mFetchSubscriptionsSuccess) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, accessToken, accountName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            mCurrentAccountSharedPreferences.edit().putLong(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME, System.currentTimeMillis()).apply();
                            InsertSubscribedThings.insertSubscribedThings(
                                    mExecutor,
                                    new Handler(),
                                    mRedditDataRoomDatabase,
                                    accountName,
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
            FetchUserData.fetchUserData(mRedditDataRoomDatabase, mOauthRetrofit, accessToken,
                    accountName, new FetchUserData.FetchUserDataListener() {
                @Override
                public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                    MainActivity.this.inboxCount = inboxCount;
                    accountName = userData.getName();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    private void changeSortType() {
        int currentPostType = sectionsPagerAdapter.getCurrentPostType();
        PostFragment postFragment = sectionsPagerAdapter.getCurrentFragment();
        if (postFragment != null) {
            SortTypeBottomSheetFragment sortTypeBottomSheetFragment = SortTypeBottomSheetFragment.getNewInstance(currentPostType != PostPagingSource.TYPE_FRONT_PAGE, postFragment.getSortType());
            sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
        }
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
    public void onBackPressed() {
        if (binding.drawerLayout.isOpen()) {
            binding.drawerLayout.close();
        } else {
            if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_CONFIRM_EXIT) {
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.exit_app)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> finish())
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else if (mBackButtonAction == SharedPreferencesUtils.MAIN_PAGE_BACK_BUTTON_ACTION_OPEN_NAVIGATION_DRAWER) {
                binding.drawerLayout.open();
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
            navigationWrapper.showNavigation();
        }
        if (!(showBottomAppBar && mLockBottomAppBar) && !hideFab) {
            navigationWrapper.showFab();
        }
    }

    @Override
    public void contentScrollDown() {
        if (!(showBottomAppBar && mLockBottomAppBar) && !hideFab) {
            navigationWrapper.hideFab();
        }
        if (showBottomAppBar && !mLockBottomAppBar) {
            navigationWrapper.hideNavigation();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
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
        binding.includedAppBar.viewPagerMainActivity.setUserInputEnabled(!mDisableSwipingBetweenTabs);
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
            if (binding.navDrawerRecyclerViewMainActivity.getLayoutManager() != null) {
                previousPosition = ((LinearLayoutManagerBugFixed) binding.navDrawerRecyclerViewMainActivity.getLayoutManager()).findFirstVisibleItemPosition();
            }

            RecyclerView.LayoutManager layoutManager = binding.navDrawerRecyclerViewMainActivity.getLayoutManager();
            binding.navDrawerRecyclerViewMainActivity.setAdapter(null);
            binding.navDrawerRecyclerViewMainActivity.setLayoutManager(null);
            binding.navDrawerRecyclerViewMainActivity.setAdapter(adapter.getConcatAdapter());
            binding.navDrawerRecyclerViewMainActivity.setLayoutManager(layoutManager);

            if (previousPosition > 0) {
                binding.navDrawerRecyclerViewMainActivity.scrollToPosition(previousPosition);
            }
        }
    }

    @Subscribe
    public void onChangeInboxCountEvent(ChangeInboxCountEvent event) {
        if (adapter != null) {
            adapter.setInboxCount(event.inboxCount);
        }
    }

    @Subscribe
    public void onChangeHideKarmaEvent(ChangeHideKarmaEvent event) {
        if (adapter != null) {
            adapter.setHideKarma(event.hideKarma);
        }
    }

    @Subscribe
    public void onChangeHideFabInPostFeed(ChangeHideFabInPostFeedEvent event) {
        hideFab = event.hideFabInPostFeed;
        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void onNewUserLoggedInEvent(NewUserLoggedInEvent event) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
            case FABMoreOptionsBottomSheetFragment.FAB_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
        }
    }

    private void goToSubreddit() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text,
                binding.includedAppBar.coordinatorLayoutMainActivity, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            Utils.hideKeyboard(this);
            Intent intent = new Intent(MainActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        thingEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                startActivity(subredditIntent);
                return true;
            }
            return false;
        });

        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        Handler handler = new Handler();
        thingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (subredditAutocompleteCall != null && subredditAutocompleteCall.isExecuted()) {
                    subredditAutocompleteCall.cancel();
                }
                if (autoCompleteRunnable != null) {
                    handler.removeCallbacks(autoCompleteRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String currentQuery = editable.toString().trim();
                if (!currentQuery.isEmpty()) {
                    autoCompleteRunnable = () -> {
                        subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(accessToken),
                                currentQuery, nsfw);
                        subredditAutocompleteCall.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                subredditAutocompleteCall = null;
                                if (response.isSuccessful() && !call.isCanceled()) {
                                    ParseSubredditData.parseSubredditListingData(mExecutor, handler,
                                            response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
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
                                subredditAutocompleteCall = null;
                            }
                        });
                    };

                    handler.postDelayed(autoCompleteRunnable, 500);
                }
            }
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_subreddit)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                    subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                    startActivity(subredditIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void goToUser() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, binding.includedAppBar.coordinatorLayoutMainActivity, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        thingEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
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
                    Utils.hideKeyboard(this);
                    Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                    userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                    startActivity(userIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void randomThing() {
        RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false));
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
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, accountName, post.getId());
    }

    public void doNotShowRedditAPIInfoAgain() {
        mInternalSharedPreferences.edit().putBoolean(SharedPreferencesUtils.DO_NOT_SHOW_REDDIT_API_INFO_V2_AGAIN, true).apply();
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
                int postType = mMainActivityTabsSharedPreferences.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
                String name = mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
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
                     postType = mMainActivityTabsSharedPreferences.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
                     name = mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
                } else {
                    postType = mMainActivityTabsSharedPreferences.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
                    name = mMainActivityTabsSharedPreferences.getString((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
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
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE : PostPagingSource.TYPE_FRONT_PAGE);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, name);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT : PostPagingSource.TYPE_MULTI_REDDIT);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, name);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SUBMITTED);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN
                    || postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SAVED) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, accountName);
                bundle.putBoolean(PostFragment.EXTRA_DISABLE_READ_POSTS, true);

                if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_UPVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_DOWNVOTED) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HIDDEN) {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                } else {
                    bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SAVED);
                }

                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
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
            if (fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + binding.includedAppBar.viewPagerMainActivity.getCurrentItem());
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
                Utils.displaySortTypeInToolbar(sortType, binding.includedAppBar.toolbar);
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