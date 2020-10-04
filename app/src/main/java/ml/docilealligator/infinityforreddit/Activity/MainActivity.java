package ml.docilealligator.infinityforreddit.Activity;

import android.app.Activity;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import java.util.List;
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
import ml.docilealligator.infinityforreddit.BottomSheetFragment.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.ChangeConfirmToExitEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeDisableSwipingBetweenTabsEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeRequireAuthToAccountSectionEvent;
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
        ActivityToolbarInterface, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback, RandomBottomSheetFragment.RandomOptionSelectionCallback {

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
    @BindView(R.id.bottom_app_bar_main_activity)
    BottomAppBar bottomAppBar;
    @BindView(R.id.linear_layout_bottom_app_bar_main_activity)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.option_1_bottom_app_bar_main_activity)
    ImageView option1BottomAppBar;
    @BindView(R.id.option_2_bottom_app_bar_main_activity)
    ImageView option2BottomAppBar;
    @BindView(R.id.option_3_bottom_app_bar_main_activity)
    ImageView option3BottomAppBar;
    @BindView(R.id.option_4_bottom_app_bar_main_activity)
    ImageView option4BottomAppBar;
    @BindView(R.id.fab_main_activity)
    FloatingActionButton fab;
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
    SharedPreferences bottomAppBarSharedPreference;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;
    private FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment;
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
    private boolean mDisableSwipingBetweenTabs;
    private boolean mShowFavoriteSubscribedSubreddits;
    private boolean mShowSubscribedSubreddits;

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
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);
                    linearLayoutBottomAppBar.setPadding(0,
                            linearLayoutBottomAppBar.getPaddingTop(), 0, navBarHeight);
                    navDrawerRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
        fabMoreOptionsBottomSheetFragment = new FABMoreOptionsBottomSheetFragment();

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
                    if (adapter.closeAccountSectionWithoutChangeIconResource(true)) {
                        adapter.notifyItemChanged(0);
                    }
                }
            }
        });
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);
        mConfirmToExit = mSharedPreferences.getBoolean(SharedPreferencesUtils.CONFIRM_TO_EXIT, false);
        mLockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);
        mDisableSwipingBetweenTabs = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false);

        fragmentManager = getSupportFragmentManager();

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
        option1BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option2BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option3BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option4BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        navigationView.setBackgroundColor(backgroundColor);
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        bottomAppBar.setBackgroundTint(ColorStateList.valueOf(mCustomThemeWrapper.getBottomAppBarBackgroundColor()));
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
            default:
                return R.drawable.ic_account_circle_24dp;
        }
    }

    private void bindView() {
        if (isDestroyed()) {
            return;
        }

        if (mAccessToken == null) {
            bottomAppBar.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        } else {
            if (showBottomAppBar) {
                int optionCount = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_COUNT, 4);
                int option1 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);
                int option2 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_MULTIREDDITS);

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
                    int option3 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_3, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX);
                    int option4 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_4, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_PROFILE);

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

            int fabOption = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB, SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
            switch (fabOption) {
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_REFRESH:
                    fab.setImageResource(R.drawable.ic_refresh_24dp);
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                    fab.setImageResource(R.drawable.ic_sort_24dp);
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                    fab.setImageResource(R.drawable.ic_post_layout_24dp);
                    break;
                case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH:
                    fab.setImageResource(R.drawable.ic_search_black_24dp);
                    break;
                default:
                    fab.setImageResource(R.drawable.ic_add_day_night_24dp);
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
                        postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                        break;
                    }
                    case SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_FAB_SEARCH: {
                        Intent intent = new Intent(this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    }
                    default:
                        postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                        break;
                }
            });
            fab.setOnLongClickListener(view -> {
                fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
                return true;
            });
            fab.setVisibility(View.VISIBLE);
        }

        adapter = new NavigationDrawerRecyclerViewAdapter(this, mSharedPreferences,
                mNsfwAndSpoilerSharedPreferences, mCustomThemeWrapper, mAccountName,
                mProfileImageUrl, mBannerImageUrl, mKarma,
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
                            mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, true).apply();
                            sectionsPagerAdapter.changeNSFW(true);
                        }
                        break;
                    case R.string.disable_nsfw:
                        if (sectionsPagerAdapter != null) {
                            mNsfwAndSpoilerSharedPreferences.edit().putBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false).apply();
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

        int tabCount = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_COUNT, 3);
        mShowFavoriteSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_FAVORITE_SUBSCRIBED_SUBREDDITS, false);
        mShowSubscribedSubreddits = mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_SUBSCRIBED_SUBREDDITS, false);
        sectionsPagerAdapter = new SectionsPagerAdapter(fragmentManager, getLifecycle(), tabCount,
                mShowFavoriteSubscribedSubreddits, mShowSubscribedSubreddits);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(1);
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
        if (mMainActivityTabsSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_SHOW_TAB_NAMES, true)) {
            if (mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits) {
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
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
                    if (position >= tabCount && (mShowFavoriteSubscribedSubreddits || mShowSubscribedSubreddits) && sectionsPagerAdapter != null) {
                        List<SubscribedSubredditData> favoriteSubscribedSubreddits = sectionsPagerAdapter.favoriteSubscribedSubreddits;
                        List<SubscribedSubredditData> subscribedSubreddits = sectionsPagerAdapter.subscribedSubreddits;
                        if (position - tabCount < favoriteSubscribedSubreddits.size()) {
                            tab.setText(favoriteSubscribedSubreddits.get(position - tabCount).getName());
                        } else if (position - tabCount - favoriteSubscribedSubreddits.size() < subscribedSubreddits.size()) {
                            tab.setText(subscribedSubreddits.get(position - tabCount - favoriteSubscribedSubreddits.size()).getName());
                        }
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
                    fab.show();
                }
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });

        loadSubscriptions();

        subscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName))
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

    private void changeSortType() {
        int currentPostType = sectionsPagerAdapter.getCurrentPostType();
        Bundle bundle = new Bundle();
        if (currentPostType != PostDataSource.TYPE_FRONT_PAGE) {
            bundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        } else {
            bundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
        }
        sortTypeBottomSheetFragment.setArguments(bundle);
        sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_main_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_sort_main_activity:
                changeSortType();
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
    public void onBackPressed() {;
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
                bottomAppBar.performShow();
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
                bottomAppBar.performHide();
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

    @Subscribe
    public void onChangeDisableSwipingBetweenTabsEvent(ChangeDisableSwipingBetweenTabsEvent changeDisableSwipingBetweenTabsEvent) {
        mDisableSwipingBetweenTabs = changeDisableSwipingBetweenTabsEvent.disableSwipingBetweenTabs;
        viewPager2.setUserInputEnabled(!mDisableSwipingBetweenTabs);
    }

    @Subscribe
    public void onChangeRequireAuthToAccountSectionEvent(ChangeRequireAuthToAccountSectionEvent changeRequireAuthToAccountSectionEvent) {
        adapter.setRequireAuthToAccountSection(changeRequireAuthToAccountSectionEvent.requireAuthToAccountSection);
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
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_SUBREDDIT: {
                EditText thingEditText = (EditText) getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, null);
                thingEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.go_to_user)
                        .setView(thingEditText)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                            }
                            Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                            subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                            startActivity(subredditIntent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setOnDismissListener(dialogInterface -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                            }
                        })
                        .show();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_USER: {
                EditText thingEditText = (EditText) getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, null);
                thingEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.go_to_user)
                        .setView(thingEditText)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                            }
                            Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                            userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                            startActivity(userIntent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setOnDismissListener(dialogInterface -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                            }
                        })
                        .show();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_RANDOM: {
                RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false));
                randomBottomSheetFragment.setArguments(bundle);
                randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
                break;
            }
        }
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {
        int tabCount;
        boolean showFavoriteSubscribedSubreddits;
        boolean showSubscribedSubreddits;
        List<SubscribedSubredditData> favoriteSubscribedSubreddits;
        List<SubscribedSubredditData> subscribedSubreddits;

        SectionsPagerAdapter(FragmentManager fm, Lifecycle lifecycle, int tabCount, boolean showFavoriteSubscribedSubreddits, boolean showSubscribedSubreddits) {
            super(fm, lifecycle);
            this.tabCount = tabCount;
            favoriteSubscribedSubreddits = new ArrayList<>();
            subscribedSubreddits = new ArrayList<>();
            this.showFavoriteSubscribedSubreddits = showFavoriteSubscribedSubreddits;
            this.showSubscribedSubreddits = showSubscribedSubreddits;
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
                if (showFavoriteSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount < favoriteSubscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = favoriteSubscribedSubreddits.get(position - tabCount).getName();
                        return generatePostFragment(postType, name);
                    }
                }
                if (showSubscribedSubreddits) {
                    if (position >= tabCount && position - tabCount - favoriteSubscribedSubreddits.size() < subscribedSubreddits.size()) {
                        int postType = SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_SUBREDDIT;
                        String name = subscribedSubreddits.get(position - tabCount - favoriteSubscribedSubreddits.size()).getName();
                        return generatePostFragment(postType, name);
                    }
                }
                int postType = mMainActivityTabsSharedPreferences.getInt((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_POST_TYPE, SharedPreferencesUtils.MAIN_PAGE_TAB_POST_TYPE_ALL);
                String name = mMainActivityTabsSharedPreferences.getString((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.MAIN_PAGE_TAB_3_NAME, "");
                return generatePostFragment(postType, name);
            }
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
            return tabCount + favoriteSubscribedSubreddits.size() + subscribedSubreddits.size();
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

        boolean startLazyMode() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.startLazyMode();
            }

            return false;
        }

        void stopLazyMode() {
            for (int i = 0; i < getItemCount(); i++) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + i);
                if (fragment instanceof PostFragment && ((PostFragment) fragment).isInLazyMode()) {
                    ((PostFragment) fragment).stopLazyMode();
                }
            }
        }

        int getCurrentPostType() {
            PostFragment currentFragment = getCurrentFragment();
            if (currentFragment != null) {
                return currentFragment.getPostType();
            }
            return PostDataSource.TYPE_SUBREDDIT;
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
    }
}
