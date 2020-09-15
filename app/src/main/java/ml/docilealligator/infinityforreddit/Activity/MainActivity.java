package ml.docilealligator.infinityforreddit.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Account.AccountViewModel;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.NavigationDrawerRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.InsertSubscribedThingsAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.SwitchAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.SwitchToAnonymousAccountAsyncTask;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.ChangeConfirmToExitEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.FetchMyInfo;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.Message.ReadMessage;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface {

    static final String EXTRA_POST_TYPE = "EPT";
    static final String EXTRA_MESSSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String FETCH_SUBSCRIPTIONS_STATE = "FSS";
    private static final String DRAWER_ON_ACCOUNT_SWITCH_STATE = "DOASS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String ACCOUNT_PROFILE_IMAGE_URL_STATE = "APIUS";
    private static final String ACCOUNT_BANNER_IMAGE_URL_STATE = "ABIUS";
    private static final String ACCOUNT_KARMA_STATE = "AKS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";

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
    Toolbar toolbar;
    @BindView(R.id.nav_drawer_recycler_view_main_activity)
    RecyclerView navDrawerRecyclerView;
    @BindView(R.id.tab_layout_main_activity)
    TabLayout tabLayout;
    @BindView(R.id.bottom_navigation_main_activity)
    BottomAppBar bottomNavigationView;
    @BindView(R.id.linear_layout_bottom_app_bar_main_activity)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.subscriptions_bottom_app_bar_main_activity)
    ImageView subscriptionsBottomAppBar;
    @BindView(R.id.multi_reddit_bottom_app_bar_main_activity)
    ImageView multiRedditBottomAppBar;
    @BindView(R.id.message_bottom_app_bar_main_activity)
    ImageView messageBottomAppBar;
    @BindView(R.id.profile_bottom_app_bar_main_activity)
    ImageView profileBottomAppBar;
    @BindView(R.id.fab_main_activity)
    FloatingActionButton fab;
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
    CustomThemeWrapper mCustomThemeWrapper;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;
    private NavigationDrawerRecyclerViewAdapter adapter;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private int mKarma;
    private boolean mFetchUserInfoSuccess = false;
    private boolean mFetchSubscriptionsSuccess = false;
    private boolean mDrawerOnAccountSwitch = false;
    private String mMessageFullname;
    private String mNewAccountName;
    private Menu mMenu;
    private boolean isInLazyMode = false;
    private boolean showBottomAppBar;
    private boolean mConfirmToExit;
    private boolean mLockBottomAppBar;

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    drawer.setFitsSystemWindows(false);
                    drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    linearLayoutBottomAppBar.setPadding(0,
                            (int) (6 * getResources().getDisplayMetrics().density), 0, navBarHeight);
                    navDrawerRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();

        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);
        mConfirmToExit = mSharedPreferences.getBoolean(SharedPreferencesUtils.CONFIRM_TO_EXIT, false);
        mLockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);

        if (savedInstanceState != null) {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mFetchSubscriptionsSuccess = savedInstanceState.getBoolean(FETCH_SUBSCRIPTIONS_STATE);
            mDrawerOnAccountSwitch = savedInstanceState.getBoolean(DRAWER_ON_ACCOUNT_SWITCH_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            mProfileImageUrl = savedInstanceState.getString(ACCOUNT_PROFILE_IMAGE_URL_STATE);
            mBannerImageUrl = savedInstanceState.getString(ACCOUNT_BANNER_IMAGE_URL_STATE);
            mKarma = savedInstanceState.getInt(ACCOUNT_KARMA_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            getCurrentAccountAndBindView();
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
        drawer.setStatusBarBackgroundColor(mCustomThemeWrapper.getColorPrimaryDark());
        int bottomAppBarIconColor = mCustomThemeWrapper.getBottomAppBarIconColor();
        subscriptionsBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        multiRedditBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        messageBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        profileBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        navigationView.setBackgroundColor(backgroundColor);
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        bottomNavigationView.setBackgroundTint(ColorStateList.valueOf(mCustomThemeWrapper.getBottomAppBarBackgroundColor()));
        applyFABTheme(fab);
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
            long notificationInterval = Long.parseLong(mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));
            TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

            WorkManager workManager = WorkManager.getInstance(this);

            if (mNewAccountName != null) {
                if (account == null || !account.getUsername().equals(mNewAccountName)) {
                    new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
                        EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                        Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                        mNewAccountName = null;
                        if (newAccount == null) {
                            mNullAccessToken = true;
                        } else {
                            mAccessToken = newAccount.getAccessToken();
                            mAccountName = newAccount.getUsername();
                            mProfileImageUrl = newAccount.getProfileImageUrl();
                            mBannerImageUrl = newAccount.getBannerImageUrl();
                            mKarma = newAccount.getKarma();
                        }

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

                        bindView();
                    }).execute();
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    mProfileImageUrl = account.getProfileImageUrl();
                    mBannerImageUrl = account.getBannerImageUrl();
                    mKarma = account.getKarma();

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

                    bindView();
                }
            } else {
                if (account == null) {
                    mNullAccessToken = true;
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    mProfileImageUrl = account.getProfileImageUrl();
                    mBannerImageUrl = account.getBannerImageUrl();
                    mKarma = account.getKarma();
                }

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

                bindView();
            }
        }).execute();
    }

    private void bindView() {
        if (isDestroyed()) {
            return;
        }

        if (mAccessToken == null) {
            bottomNavigationView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        } else {
            if (showBottomAppBar) {
                bottomNavigationView.setVisibility(View.VISIBLE);
                subscriptionsBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                    startActivity(intent);
                });

                multiRedditBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                    intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                    startActivity(intent);
                });

                messageBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(this, InboxActivity.class);
                    startActivity(intent);
                });

                profileBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(this, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                    startActivity(intent);
                });
            } else {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                lp.setAnchorId(View.NO_ID);
                lp.gravity = Gravity.END | Gravity.BOTTOM;
                fab.setLayoutParams(lp);
            }

            fab.setOnClickListener(view -> postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag()));
            fab.setVisibility(View.VISIBLE);
        }

        boolean nsfwEnabled = mSharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_KEY, false);
        adapter = new NavigationDrawerRecyclerViewAdapter(this, mCustomThemeWrapper, mAccountName,
                mProfileImageUrl, mBannerImageUrl, mKarma, nsfwEnabled,
                new NavigationDrawerRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onMenuClick(int stringId) {
                Intent intent = null;
                switch (stringId) {
                    case R.string.profile:
                        intent = new Intent(MainActivity.this, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                        break;
                    case R.string.subscriptions:
                        intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                        break;
                    case R.string.multi_reddit:
                        intent = new Intent(MainActivity.this, SubscribedThingListingActivity.class);
                        intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                        break;
                    case R.string.inbox:
                        intent = new Intent(MainActivity.this, InboxActivity.class);
                        break;
                    case R.string.upvoted:
                        intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                        intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_UPVOTED);
                        break;
                    case R.string.downvoted:
                        intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                        intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_DOWNVOTED);
                        break;
                    case R.string.hidden:
                        intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                        intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_HIDDEN);
                        break;
                    case R.string.saved:
                        intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
                        break;
                    case R.string.gilded:
                        intent = new Intent(MainActivity.this, AccountPostsActivity.class);
                        intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_GILDED);
                        break;
                    case R.string.light_theme:
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "0").apply();
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                        mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                        break;
                    case R.string.dark_theme:
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "1").apply();
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                            mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                        } else {
                            mCustomThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                        }
                        break;
                    case R.string.enable_nsfw:
                        if (sectionsPagerAdapter != null) {
                            mSharedPreferences.edit().putBoolean(SharedPreferencesUtils.NSFW_KEY, true).apply();
                            sectionsPagerAdapter.changeNSFW(true);
                        }
                        break;
                    case R.string.disable_nsfw:
                        if (sectionsPagerAdapter != null) {
                            mSharedPreferences.edit().putBoolean(SharedPreferencesUtils.NSFW_KEY, false).apply();
                            sectionsPagerAdapter.changeNSFW(false);
                        }
                        break;
                    case R.string.settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        break;
                    case R.string.add_account:
                        Intent addAccountIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(addAccountIntent, LOGIN_ACTIVITY_REQUEST_CODE);
                        break;
                    case R.string.anonymous_account:
                        new SwitchToAnonymousAccountAsyncTask(mRedditDataRoomDatabase, false,
                                () -> {
                                    Intent anonymousIntent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(anonymousIntent);
                                    finish();
                                }).execute();
                        break;
                    case R.string.log_out:
                        new SwitchToAnonymousAccountAsyncTask(mRedditDataRoomDatabase, true,
                                () -> {
                                    Intent logOutIntent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(logOutIntent);
                                    finish();
                                }).execute();

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
                new SwitchAccountAsyncTask(mRedditDataRoomDatabase, accountName, newAccount -> {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }).execute();
            }
        });
        navDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navDrawerRecyclerView.setAdapter(adapter);

        fragmentManager = getSupportFragmentManager();
        sectionsPagerAdapter = new SectionsPagerAdapter(fragmentManager, getLifecycle());
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.requestDisallowInterceptTouchEvent(true);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if (mAccessToken == null) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.popular);
                        break;
                    case 1:
                        tab.setText(R.string.all);
                        break;
                }
            } else {
                switch (position) {
                    case 0:
                        tab.setText(mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home)));
                        break;
                    case 1:
                        tab.setText(mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular)));
                        break;
                    case 2:
                        tab.setText(mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all)));
                        break;
                }
            }
        }).attach();
        //tabLayout.setupWithViewPager(viewPager2);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (mAccessToken != null) {
                    if (showBottomAppBar) {
                        bottomNavigationView.performShow();
                    }
                    fab.show();
                }
                /*if (isInLazyMode) {
                    if (position == sectionsPagerAdapter.getCurrentLazyModeFragmentPosition()) {
                        sectionsPagerAdapter.resumeLazyMode();
                    } else {
                        sectionsPagerAdapter.pauseLazyMode();
                    }
                }*/
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });
        /*viewPager2.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mAccessToken != null) {
                    if (showBottomAppBar) {
                        bottomNavigationView.performShow();
                    }
                    fab.show();
                }
                if (isInLazyMode) {
                    if (position == sectionsPagerAdapter.getCurrentLazyModeFragmentPosition()) {
                        sectionsPagerAdapter.resumeLazyMode();
                    } else {
                        sectionsPagerAdapter.pauseLazyMode();
                    }
                }
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });*/

        loadSubscriptions();

        subscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName))
                .get(SubscribedSubredditViewModel.class);
        subscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this,
                subscribedSubredditData -> adapter.setSubscribedSubreddits(subscribedSubredditData));

        accountViewModel = new ViewModelProvider(this,
                new AccountViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName)).get(AccountViewModel.class);
        accountViewModel.getAccountsExceptCurrentAccountLiveData().observe(this, adapter::changeAccountsDataset);

        if (getIntent().hasExtra(EXTRA_POST_TYPE)) {
            String type = getIntent().getStringExtra(EXTRA_POST_TYPE);
            if (type != null && type.equals("popular")) {
                viewPager2.setCurrentItem(1);
            } else {
                viewPager2.setCurrentItem(2);
            }
        }

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
                            new InsertSubscribedThingsAsyncTask(
                                    mRedditDataRoomDatabase,
                                    mAccountName,
                                    subscribedSubredditData,
                                    subscribedUserData,
                                    subredditData,
                                    () -> mFetchSubscriptionsSuccess = true).execute();
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
            FetchMyInfo.fetchAccountInfo(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                    new FetchMyInfo.FetchMyInfoListener() {
                        @Override
                        public void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                            mAccountName = name;
                            mProfileImageUrl = profileImageUrl;
                            mBannerImageUrl = bannerImageUrl;
                            mKarma = karma;
                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onFetchMyInfoFailed(boolean parseFailed) {
                            mFetchUserInfoSuccess = false;
                        }
            });
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
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);

        if (isInLazyMode) {
            lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
            collapsingToolbarLayout.setLayoutParams(params);
        } else {
            lazyModeItem.setTitle(R.string.action_start_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            collapsingToolbarLayout.setLayoutParams(params);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_main_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_sort_main_activity:
                int currentPostType = sectionsPagerAdapter.getCurrentPostType();
                Bundle bundle = new Bundle();
                if (currentPostType != PostDataSource.TYPE_FRONT_PAGE) {
                    bundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
                } else {
                    bundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
                }
                sortTypeBottomSheetFragment.setArguments(bundle);
                sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
                return true;
            case R.id.action_refresh_main_activity:
                if (mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_main_activity).setTitle(R.string.action_start_lazy_mode);
                }
                sectionsPagerAdapter.refresh();
                mFetchUserInfoSuccess = false;
                loadUserData();
                return true;
            case R.id.action_lazy_mode_main_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);
                if (isInLazyMode) {
                    sectionsPagerAdapter.stopLazyMode();
                    isInLazyMode = false;
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    if (sectionsPagerAdapter.startLazyMode()) {
                        isInLazyMode = true;
                        lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                        collapsingToolbarLayout.setLayoutParams(params);
                    }
                }
                return true;
            case R.id.action_change_post_layout_main_activity:
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mConfirmToExit) {
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.exit_app)
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> finish())
                        .setNegativeButton(R.string.no, null)
                        .show();
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
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(ACCOUNT_PROFILE_IMAGE_URL_STATE, mProfileImageUrl);
        outState.putString(ACCOUNT_BANNER_IMAGE_URL_STATE, mBannerImageUrl);
        outState.putInt(ACCOUNT_KARMA_STATE, mKarma);
        outState.putString(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
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
        }
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    public void postScrollUp() {
        if (mAccessToken != null) {
            if (showBottomAppBar && !mLockBottomAppBar) {
                bottomNavigationView.performShow();
            }
            if (!(showBottomAppBar && mLockBottomAppBar)) {
                fab.show();
            }
        }
    }

    public void postScrollDown() {
        if (mAccessToken != null) {
            if (!(showBottomAppBar && mLockBottomAppBar)) {
                fab.hide();
            }
            if (showBottomAppBar && !mLockBottomAppBar) {
                bottomNavigationView.performHide();
            }
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
        recreate();
    }

    @Subscribe
    public void onChangeConfirmToExitEvent(ChangeConfirmToExitEvent changeConfirmToExitEvent) {
        mConfirmToExit = changeConfirmToExitEvent.confirmToExit;
    }

    @Subscribe
    public void onChangeLockBottomAppBar(ChangeLockBottomAppBarEvent changeLockBottomAppBarEvent) {
        mLockBottomAppBar = changeLockBottomAppBarEvent.lockBottomAppBar;
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

    private class SectionsPagerAdapter extends FragmentStateAdapter{
        /*private PostFragment tab1;
        private PostFragment tab2;
        private PostFragment tab3;*/

        SectionsPagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (mAccessToken == null) {
                if (position == 0) {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "popular");
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    fragment.setArguments(bundle);
                    return fragment;
                } else {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "all");
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    fragment.setArguments(bundle);
                    return fragment;
                }
            }

            if (position == 0) {
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_NAME, "");
                return generatePostFragment(postType, name);
            } else if (position == 1) {
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_POPULAR);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_NAME, "");
                return generatePostFragment(postType, name);
            } else {
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
                return generatePostFragment(postType, name);
            }
        }

        private Fragment generatePostFragment(int postType, String name) {

            if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_HOME) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_MULTIREDDIT) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_MULTI_REDDIT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else if (postType == SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_USER) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, name);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_SUBMITTED);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getItemCount() {
            if (mAccessToken == null) {
                return 2;
            }
            return 3;
        }

        /*@Override
        public CharSequence getPageTitle(int position) {
            if (mAccessToken == null) {
                switch (position) {
                    case 0:
                        return getString(R.string.popular);
                    case 1:
                        return getString(R.string.all);
                }
            } else {
                switch (position) {
                    case 0:
                        return mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_1_TITLE, getString(R.string.home));
                    case 1:
                        return mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_2_TITLE, getString(R.string.popular));
                    case 2:
                        return mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_TITLE, getString(R.string.all));
                }
            }
            return null;
        }*/

        /*@NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (mAccessToken == null) {
                switch (position) {
                    case 0:
                        tab2 = (PostFragment) fragment;
                        break;
                    case 1:
                        tab3 = (PostFragment) fragment;
                }
            } else {
                switch (position) {
                    case 0:
                        tab1 = (PostFragment) fragment;
                        break;
                    case 1:
                        tab2 = (PostFragment) fragment;
                        break;
                    case 2:
                        tab3 = (PostFragment) fragment;
                }
            }
            displaySortTypeInToolbar();
            return fragment;
        }*/

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
            /*if (mAccessToken == null) {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        return tab2.handleKeyDown(keyCode);
                    case 1:
                        return tab3.handleKeyDown(keyCode);
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        return tab1.handleKeyDown(keyCode);
                    case 1:
                        return tab2.handleKeyDown(keyCode);
                    case 2:
                        return tab3.handleKeyDown(keyCode);
                }
            }*/
            return false;
        }

        boolean startLazyMode() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.startLazyMode();
            }
            /*if (mAccessToken == null) {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        return tab2.startLazyMode();
                    case 1:
                        return tab3.startLazyMode();
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        return tab1.startLazyMode();
                    case 1:
                        return tab2.startLazyMode();
                    case 2:
                        return tab3.startLazyMode();
                }
            }*/

            return false;
        }

        void stopLazyMode() {
            for (int i = 0; i < getItemCount(); i++) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + i);
                if (fragment instanceof PostFragment && ((PostFragment) fragment).isInLazyMode()) {
                    ((PostFragment) fragment).stopLazyMode();
                }
            }
            /*if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab2.stopLazyMode();
                        break;
                    case 1:
                        tab3.stopLazyMode();
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab1.stopLazyMode();
                        break;
                    case 1:
                        tab2.stopLazyMode();
                        break;
                    case 2:
                        tab3.stopLazyMode();
                        break;
                }
            }*/
        }

        void resumeLazyMode() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.resumeLazyMode(false);
            }
            /*if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab2.resumeLazyMode(false);
                        break;
                    case 1:
                        tab3.resumeLazyMode(false);
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab1.resumeLazyMode(false);
                        break;
                    case 1:
                        tab2.resumeLazyMode(false);
                        break;
                    case 2:
                        tab3.resumeLazyMode(false);
                        break;
                }
            }*/
        }

        void pauseLazyMode() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.pauseLazyMode(false);
            }
            /*if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab2.pauseLazyMode(false);
                        break;
                    case 1:
                        tab3.pauseLazyMode(false);
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        tab1.pauseLazyMode(false);
                        break;
                    case 1:
                        tab2.pauseLazyMode(false);
                        break;
                    case 2:
                        tab3.pauseLazyMode(false);
                }
            }*/
        }

        /*int getCurrentLazyModeFragmentPosition() {
            if (mAccessToken == null) {
                if (!isInLazyMode) {
                    return -1;
                } else if (tab2 != null && tab2.isInLazyMode()) {
                    return 0;
                } else if (tab3 != null && tab3.isInLazyMode()) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if (!isInLazyMode) {
                    return -1;
                } else if (tab1 != null && tab1.isInLazyMode()) {
                    return 0;
                } else if (tab2 != null && tab2.isInLazyMode()) {
                    return 1;
                } else if (tab3 != null && tab3.isInLazyMode()) {
                    return 2;
                } else {
                    return -1;
                }
            }
        }*/

        int getCurrentPostType() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.getPostType();
            }
            /*if (mAccessToken == null) {
                if (viewPager2.getCurrentItem() == 0) {
                    return tab2.getPostType();
                } else {
                    return tab3.getPostType();
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 1:
                        return tab2.getPostType();
                    case 2:
                        return tab3.getPostType();
                    default:
                        return tab1.getPostType();
                }
            }*/
            return PostDataSource.TYPE_SUBREDDIT;
        }

        void changeSortType(SortType sortType) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changeSortType(sortType);
            }
            /*if (mAccessToken == null) {
                if (viewPager2.getCurrentItem() == 0) {
                    tab2.changeSortType(sortType);
                } else {
                    tab3.changeSortType(sortType);
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        tab1.changeSortType(sortType);
                        break;
                    case 1:
                        tab2.changeSortType(sortType);
                        break;
                    case 2:
                        tab3.changeSortType(sortType);
                }
            }*/
            displaySortTypeInToolbar();
        }

        public void refresh() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.refresh();
            }
            /*if (mAccessToken == null) {
                if (viewPager2.getCurrentItem() == 0) {
                    if (tab2 != null) {
                        tab2.refresh();
                    }
                } else {
                    if (tab3 != null) {
                        tab3.refresh();
                    }
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        if (tab1 != null) {
                            tab1.refresh();
                        }
                        break;
                    case 1:
                        if (tab2 != null) {
                            tab2.refresh();
                        }
                        break;
                    case 2:
                        if (tab3 != null) {
                            tab3.refresh();
                        }
                }
            }*/
        }

        void changeNSFW(boolean nsfw) {
            for (int i = 0; i < getItemCount(); i++) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + i);
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).changeNSFW(nsfw);
                }
            }
            /*if (tab1 != null) {
                tab1.changeNSFW(nsfw);
            }
            if (tab2 != null) {
                tab2.changeNSFW(nsfw);
            }
            if (tab3 != null) {
                tab3.changeNSFW(nsfw);
            }*/
        }

        void changePostLayout(int postLayout) {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.changePostLayout(postLayout);
            }
            /*if (mAccessToken == null) {
                if (viewPager2.getCurrentItem() == 0) {
                    if (tab2 != null) {
                        mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST, postLayout).apply();
                        tab2.changePostLayout(postLayout);
                    }
                } else {
                    if (tab3 != null) {
                        mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_ALL_POST, postLayout).apply();
                        tab3.changePostLayout(postLayout);
                    }
                }
            } else {
                switch (viewPager2.getCurrentItem()) {
                    case 0:
                        if (tab1 != null) {
                            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, postLayout).apply();
                            tab1.changePostLayout(postLayout);
                        }
                        break;
                    case 1:
                        if (tab2 != null) {
                            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST, postLayout).apply();
                            tab2.changePostLayout(postLayout);
                        }
                        break;
                    case 2:
                        if (tab3 != null) {
                            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_ALL_POST, postLayout).apply();
                            tab3.changePostLayout(postLayout);
                        }
                }
            }*/
        }

        void goBackToTop() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                currentFragment.goBackToTop();
            }
            /*if (viewPager2.getCurrentItem() == 0) {
                if (mAccessToken != null && tab1 != null) {
                    tab1.goBackToTop();
                } else if (tab2 != null) {
                    tab2.goBackToTop();
                }
            } else if (viewPager2.getCurrentItem() == 1) {
                if (mAccessToken != null && tab2 != null) {
                    tab2.goBackToTop();
                } else if (tab3 != null) {
                    tab3.goBackToTop();
                }
            } else {
                tab3.goBackToTop();
            }*/
        }

        void displaySortTypeInToolbar() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                SortType sortType = currentFragment.getSortType();
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            }
            /*switch (viewPager2.getCurrentItem()) {
                case 0:
                    if (mAccessToken != null) {
                        if (tab1 != null) {
                            SortType sortType = tab1.getSortType();
                            Utils.displaySortTypeInToolbar(sortType, toolbar);
                        }
                    } else {
                        if (tab2 != null) {
                            SortType sortType = tab2.getSortType();
                            Utils.displaySortTypeInToolbar(sortType, toolbar);
                        }
                    }
                    break;
                case 1:
                    if (mAccessToken != null) {
                        if (tab2 != null) {
                            SortType sortType = tab2.getSortType();
                            Utils.displaySortTypeInToolbar(sortType, toolbar);
                        }
                    } else {
                        if (tab3 != null) {
                            SortType sortType = tab3.getSortType();
                            Utils.displaySortTypeInToolbar(sortType, toolbar);
                        }
                    }
                    break;
                case 2:
                    if (tab3 != null) {
                        SortType sortType = tab3.getSortType();
                        Utils.displaySortTypeInToolbar(sortType, toolbar);
                    }
                    break;
            }*/
        }
    }
}
