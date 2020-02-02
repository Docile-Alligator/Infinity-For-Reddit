package ml.docilealligator.infinityforreddit.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Account.Account;
import ml.docilealligator.infinityforreddit.Account.AccountViewModel;
import ml.docilealligator.infinityforreddit.Adapter.AccountRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.InsertSubscribedThingsAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.SwitchAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.SwitchToAnonymousAccountAsyncTask;
import ml.docilealligator.infinityforreddit.Event.ChangeConfirmToExitEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.FetchMyInfo;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Fragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.ParseAndSaveAccountInfo;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.ReadMessage;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback {

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
    @BindView(R.id.coordinator_layout_main_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_main_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.view_pager_main_activity)
    ViewPager viewPager;
    @BindView(R.id.collapsing_toolbar_layout_main_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nested_scroll_view_main_activity)
    NestedScrollView nestedScrollView;
    @BindView(R.id.all_drawer_items_linear_layout_main_activity)
    LinearLayout allDrawerItemsLinearLayout;
    @BindView(R.id.account_label_main_activity)
    TextView accountLabelTextView;
    @BindView(R.id.profile_text_view_main_activity)
    TextView profileTextView;
    @BindView(R.id.subscriptions_text_view_main_activity)
    TextView subscriptionTextView;
    @BindView(R.id.multi_reddits_text_view_main_activity)
    TextView multiRedditsTextView;
    @BindView(R.id.inbox_text_view_main_activity)
    TextView inboxTextView;
    @BindView(R.id.post_label_main_activity)
    TextView postLabelTextView;
    @BindView(R.id.upvoted_text_view_main_activity)
    TextView upvotedTextView;
    @BindView(R.id.downvoted_text_view_main_activity)
    TextView downvotedTextView;
    @BindView(R.id.hidden_text_view_main_activity)
    TextView hiddenTextView;
    @BindView(R.id.saved_text_view_main_activity)
    TextView savedTextView;
    @BindView(R.id.gilded_text_view_main_activity)
    TextView gildedTextView;
    @BindView(R.id.divider_main_activity)
    View divider;
    @BindView(R.id.night_mode_toggle_text_view_main_activity)
    TextView nightModeToggleTextView;
    @BindView(R.id.settings_text_view_main_activity)
    TextView settingsTextView;
    @BindView(R.id.account_recycler_view_main_activity)
    RecyclerView accountRecyclerView;
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
    AccountViewModel accountViewModel;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TextView mAccountNameTextView;
    private TextView mKarmaTextView;
    private GifImageView mProfileImageView;
    private ImageView mBannerImageView;
    private ImageView mDropIconImageView;
    private RequestManager glide;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment bestSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment popularAndAllSortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;
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

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.navBarColor, typedValue, true);
            int navBarColor = typedValue.data;
            window.setNavigationBarColor(navBarColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Resources resources = getResources();

                if ((resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet))
                        && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                    boolean lightNavBar = false;
                    if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                        lightNavBar = true;
                    }
                    boolean finalLightNavBar = lightNavBar;

                    View decorView = window.getDecorView();
                    appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                        @Override
                        public void onStateChanged(AppBarLayout appBarLayout, State state) {
                            if (state == State.COLLAPSED) {
                                if (finalLightNavBar) {
                                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                }
                            } else if (state == State.EXPANDED) {
                                if (finalLightNavBar) {
                                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                }
                            }
                        }
                    });

                    int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (statusBarResourceId > 0) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                        params.topMargin = resources.getDimensionPixelSize(statusBarResourceId);
                        toolbar.setLayoutParams(params);
                    }

                    int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                    if (navBarResourceId > 0) {
                        int navBarHeight = resources.getDimensionPixelSize(navBarResourceId);
                        linearLayoutBottomAppBar.setPadding(0,
                                (int) (6 * resources.getDisplayMetrics().density), 0, navBarHeight);
                        nestedScrollView.setPadding(0, 0, 0, navBarHeight);
                    }
                }
            }
        }

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();

        bestSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bestBundle = new Bundle();
        bestBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
        bestSortTypeBottomSheetFragment.setArguments(bestBundle);

        popularAndAllSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle popularBundle = new Bundle();
        popularBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        popularAndAllSortTypeBottomSheetFragment.setArguments(popularBundle);

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
            String notificationInterval = mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1");

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
                                            Long.parseLong(notificationInterval), TimeUnit.HOURS)
                                            .setConstraints(constraints)
                                            .build();

                            workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                    ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
                        } else {
                            workManager.cancelUniqueWork(PullNotificationWorker.WORKER_TAG);
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
                                        Long.parseLong(notificationInterval), TimeUnit.HOURS)
                                        .setConstraints(constraints)
                                        .build();

                        workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
                    } else {
                        workManager.cancelUniqueWork(PullNotificationWorker.WORKER_TAG);
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
                                    Long.parseLong(notificationInterval), TimeUnit.HOURS)
                                    .setConstraints(constraints)
                                    .build();

                    workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                            ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
                } else {
                    workManager.cancelUniqueWork(PullNotificationWorker.WORKER_TAG);
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
                    Intent intent = new Intent(MainActivity.this, MultiRedditListingActivity.class);
                    startActivity(intent);
                });

                messageBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(this, ViewMessageActivity.class);
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

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        loadSubscriptions();

        glide = Glide.with(this);

        AccountRecyclerViewAdapter adapter = new AccountRecyclerViewAdapter(this, glide, mAccountName,
                new AccountRecyclerViewAdapter.ItemSelectedListener() {
                    @Override
                    public void accountSelected(Account account) {
                        new SwitchAccountAsyncTask(mRedditDataRoomDatabase, account.getUsername(), newAccount -> {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }).execute();
                    }

                    @Override
                    public void addAccountSelected() {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                    }

                    @Override
                    public void anonymousSelected() {
                        new SwitchToAnonymousAccountAsyncTask(mRedditDataRoomDatabase, false,
                                () -> {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }).execute();
                    }

                    @Override
                    public void logoutSelected() {
                        new SwitchToAnonymousAccountAsyncTask(mRedditDataRoomDatabase, true,
                                () -> {
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }).execute();
                    }
                });

        accountRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        accountRecyclerView.setNestedScrollingEnabled(false);
        accountRecyclerView.setAdapter(adapter);

        accountViewModel = new ViewModelProvider(this,
                new AccountViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName)).get(AccountViewModel.class);
        accountViewModel.getAccountsExceptCurrentAccountLiveData().observe(this, adapter::changeAccountsDataset);

        if (getIntent().hasExtra(EXTRA_POST_TYPE)) {
            String type = getIntent().getStringExtra(EXTRA_POST_TYPE);
            if (type != null && type.equals("popular")) {
                viewPager.setCurrentItem(1);
            } else {
                viewPager.setCurrentItem(2);
            }
        }

        View header = findViewById(R.id.nav_header_main_activity);
        mAccountNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
        mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
        mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
        mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);
        mDropIconImageView = header.findViewById(R.id.account_switcher_image_view_nav_header_main);

        if (mDrawerOnAccountSwitch) {
            mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_up_24px));
            accountRecyclerView.setVisibility(View.VISIBLE);
            allDrawerItemsLinearLayout.setVisibility(View.GONE);
        } else {
            mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_down_24px));
            accountRecyclerView.setVisibility(View.GONE);
            allDrawerItemsLinearLayout.setVisibility(View.VISIBLE);
        }

        header.setOnClickListener(view -> {
            if (mDrawerOnAccountSwitch) {
                mDrawerOnAccountSwitch = false;
                mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_down_24px));
                accountRecyclerView.setVisibility(View.GONE);
                allDrawerItemsLinearLayout.setVisibility(View.VISIBLE);
            } else {
                mDrawerOnAccountSwitch = true;
                mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_up_24px));
                accountRecyclerView.setVisibility(View.VISIBLE);
                allDrawerItemsLinearLayout.setVisibility(View.GONE);
            }
        });

        loadUserData();

        if (mAccessToken != null) {
            mKarmaTextView.setText(getString(R.string.karma_info, mKarma));
            mAccountNameTextView.setText(mAccountName);
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
        } else {
            mKarmaTextView.setText(R.string.press_here_to_login);
            mAccountNameTextView.setText(R.string.anonymous_account);
            accountLabelTextView.setVisibility(View.GONE);
            profileTextView.setVisibility(View.GONE);
            subscriptionTextView.setVisibility(View.GONE);
            multiRedditsTextView.setVisibility(View.GONE);
            inboxTextView.setVisibility(View.GONE);
            postLabelTextView.setVisibility(View.GONE);
            upvotedTextView.setVisibility(View.GONE);
            downvotedTextView.setVisibility(View.GONE);
            hiddenTextView.setVisibility(View.GONE);
            savedTextView.setVisibility(View.GONE);
            gildedTextView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        if (mProfileImageUrl != null && !mProfileImageUrl.equals("")) {
            glide.load(mProfileImageUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0))))
                    .into(mProfileImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                    .into(mProfileImageView);
        }

        if (mBannerImageUrl != null && !mBannerImageUrl.equals("")) {
            glide.load(mBannerImageUrl).into(mBannerImageView);
        }

        profileTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
            startActivity(intent);
            drawer.closeDrawers();
        });

        subscriptionTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubscribedThingListingActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        multiRedditsTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, MultiRedditListingActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        inboxTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewMessageActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        upvotedTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_UPVOTED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        downvotedTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_DOWNVOTED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        hiddenTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_HIDDEN);
            startActivity(intent);
            drawer.closeDrawers();
        });

        savedTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        gildedTextView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_GILDED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            nightModeToggleTextView.setText(R.string.dark_theme);
            nightModeToggleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dark_theme_24dp, 0, 0, 0);
        } else {
            nightModeToggleTextView.setText(R.string.light_theme);
            nightModeToggleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_light_theme_24dp, 0, 0, 0);
        }

        nightModeToggleTextView.setOnClickListener(view -> {
            if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "1").apply();
                nightModeToggleTextView.setText(R.string.dark_theme);
                nightModeToggleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dark_theme_24dp, 0, 0, 0);
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if(mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Default_AmoledDark, true);
                } else {
                    getTheme().applyStyle(R.style.Theme_Default_NormalDark, true);
                }
            } else {
                mSharedPreferences.edit().putString(SharedPreferencesUtils.THEME_KEY, "0").apply();
                nightModeToggleTextView.setText(R.string.light_theme);
                nightModeToggleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_light_theme_24dp, 0, 0, 0);
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Default, true);
            }
        });

        settingsTextView.setOnClickListener(view -> {
            drawer.closeDrawers();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
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
            FetchMyInfo.fetchAccountInfo(mOauthRetrofit, mAccessToken, new FetchMyInfo.FetchUserMyListener() {
                @Override
                public void onFetchMyInfoSuccess(String response) {
                    ParseAndSaveAccountInfo.parseAndSaveAccountInfo(response, mRedditDataRoomDatabase, new ParseAndSaveAccountInfo.ParseAndSaveAccountInfoListener() {
                        @Override
                        public void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                            mAccountNameTextView.setText(name);
                            if (profileImageUrl != null && !profileImageUrl.equals("")) {
                                glide.load(profileImageUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                                        .error(glide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0))))
                                        .into(mProfileImageView);
                            } else {
                                glide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                                        .into(mProfileImageView);
                            }
                            if (bannerImageUrl != null && !bannerImageUrl.equals("")) {
                                glide.load(bannerImageUrl).into(mBannerImageView);
                            }

                            mAccountName = name;
                            mProfileImageUrl = profileImageUrl;
                            mBannerImageUrl = bannerImageUrl;
                            mKarma = karma;

                            mKarmaTextView.setText(getString(R.string.karma_info, karma));

                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onParseMyInfoFail() {
                            mFetchUserInfoSuccess = false;
                        }
                    });
                }

                @Override
                public void onFetchMyInfoFail() {
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
            case R.id.action_sort_main_activity:
                if (viewPager.getCurrentItem() == 1 || viewPager.getCurrentItem() == 2) {
                    popularAndAllSortTypeBottomSheetFragment.show(getSupportFragmentManager(), popularAndAllSortTypeBottomSheetFragment.getTag());
                } else {
                    bestSortTypeBottomSheetFragment.show(getSupportFragmentManager(), bestSortTypeBottomSheetFragment.getTag());
                }
                return true;
            case R.id.action_search_main_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, false);
                startActivity(intent);
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
        return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
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

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PostFragment frontPagePostFragment;
        private PostFragment popularPostFragment;
        private PostFragment allPostFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (mAccessToken == null) {
                if (position == 0) {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "popular");
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                } else {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "all");
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                }
            }

            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else if (position == 1) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            if (mAccessToken == null) {
                return 2;
            }
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mAccessToken == null) {
                switch (position) {
                    case 0:
                        return "Popular";
                    case 1:
                        return "All";
                }
            } else {
                switch (position) {
                    case 0:
                        return "Best";
                    case 1:
                        return "Popular";
                    case 2:
                        return "All";
                }
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (mAccessToken == null) {
                switch (position) {
                    case 0:
                        popularPostFragment = (PostFragment) fragment;
                        break;
                    case 1:
                        allPostFragment = (PostFragment) fragment;
                }
            } else {
                switch (position) {
                    case 0:
                        frontPagePostFragment = (PostFragment) fragment;
                        break;
                    case 1:
                        popularPostFragment = (PostFragment) fragment;
                        break;
                    case 2:
                        allPostFragment = (PostFragment) fragment;
                }
            }
            return fragment;
        }

        boolean handleKeyDown(int keyCode) {
            if (mAccessToken == null) {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return popularPostFragment.handleKeyDown(keyCode);
                    case 1:
                        return allPostFragment.handleKeyDown(keyCode);
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return frontPagePostFragment.handleKeyDown(keyCode);
                    case 1:
                        return popularPostFragment.handleKeyDown(keyCode);
                    case 2:
                        return allPostFragment.handleKeyDown(keyCode);
                }
            }
            return false;
        }

        boolean startLazyMode() {
            if (mAccessToken == null) {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return popularPostFragment.startLazyMode();
                    case 1:
                        return allPostFragment.startLazyMode();
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return frontPagePostFragment.startLazyMode();
                    case 1:
                        return popularPostFragment.startLazyMode();
                    case 2:
                        return allPostFragment.startLazyMode();
                }
            }

            return false;
        }

        void stopLazyMode() {
            if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        popularPostFragment.stopLazyMode();
                        break;
                    case 1:
                        allPostFragment.stopLazyMode();
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        frontPagePostFragment.stopLazyMode();
                        break;
                    case 1:
                        popularPostFragment.stopLazyMode();
                        break;
                    case 2:
                        allPostFragment.stopLazyMode();
                        break;
                }
            }
        }

        void resumeLazyMode() {
            if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        popularPostFragment.resumeLazyMode(false);
                        break;
                    case 1:
                        allPostFragment.resumeLazyMode(false);
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        frontPagePostFragment.resumeLazyMode(false);
                        break;
                    case 1:
                        popularPostFragment.resumeLazyMode(false);
                        break;
                    case 2:
                        allPostFragment.resumeLazyMode(false);
                        break;
                }
            }
        }

        void pauseLazyMode() {
            if (mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        popularPostFragment.pauseLazyMode(false);
                        break;
                    case 1:
                        allPostFragment.pauseLazyMode(false);
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        frontPagePostFragment.pauseLazyMode(false);
                        break;
                    case 1:
                        popularPostFragment.pauseLazyMode(false);
                        break;
                    case 2:
                        allPostFragment.pauseLazyMode(false);
                }
            }
        }

        int getCurrentLazyModeFragmentPosition() {
            if (mAccessToken == null) {
                if (!isInLazyMode) {
                    return -1;
                } else if (popularPostFragment != null && popularPostFragment.isInLazyMode()) {
                    return 0;
                } else if (allPostFragment != null && allPostFragment.isInLazyMode()) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if (!isInLazyMode) {
                    return -1;
                } else if (frontPagePostFragment != null && frontPagePostFragment.isInLazyMode()) {
                    return 0;
                } else if (popularPostFragment != null && popularPostFragment.isInLazyMode()) {
                    return 1;
                } else if (allPostFragment != null && allPostFragment.isInLazyMode()) {
                    return 2;
                } else {
                    return -1;
                }
            }
        }

        void changeSortType(SortType sortType) {
            if (mAccessToken == null) {
                if (viewPager.getCurrentItem() == 0) {
                    popularPostFragment.changeSortType(sortType);
                } else {
                    allPostFragment.changeSortType(sortType);
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_BEST_POST, sortType.getType().name()).apply();
                        if(sortType.getTime() != null) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_BEST_POST, sortType.getTime().name()).apply();
                        }

                        frontPagePostFragment.changeSortType(sortType);
                        break;
                    case 1:
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_POPULAR_POST, sortType.getType().name()).apply();
                        if(sortType.getTime() != null) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_POPULAR_POST, sortType.getTime().name()).apply();
                        }

                        popularPostFragment.changeSortType(sortType);
                        break;
                    case 2:
                        mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_ALL_POST, sortType.getType().name()).apply();
                        if(sortType.getTime() != null) {
                            mSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_ALL_POST, sortType.getTime().name()).apply();
                        }

                        allPostFragment.changeSortType(sortType);
                }
            }
        }

        public void refresh() {
            if (mAccessToken == null) {
                if (viewPager.getCurrentItem() == 0) {
                    if (popularPostFragment != null) {
                        popularPostFragment.refresh();
                    }
                } else {
                    if (allPostFragment != null) {
                        allPostFragment.refresh();
                    }
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        if (frontPagePostFragment != null) {
                            frontPagePostFragment.refresh();
                        }
                        break;
                    case 1:
                        if (popularPostFragment != null) {
                            popularPostFragment.refresh();
                        }
                        break;
                    case 2:
                        if (allPostFragment != null) {
                            allPostFragment.refresh();
                        }
                }
            }
        }

        void changeNSFW(boolean nsfw) {
            if (frontPagePostFragment != null) {
                frontPagePostFragment.changeNSFW(nsfw);
            }
            if (popularPostFragment != null) {
                popularPostFragment.changeNSFW(nsfw);
            }
            if (allPostFragment != null) {
                allPostFragment.changeNSFW(nsfw);
            }
        }

        void changePostLayout(int postLayout) {
            if (mAccessToken == null) {
                if (viewPager.getCurrentItem() == 0) {
                    if (popularPostFragment != null) {
                        mSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST, postLayout).apply();
                        popularPostFragment.changePostLayout(postLayout);
                    }
                } else {
                    if (allPostFragment != null) {
                        mSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_ALL_POST, postLayout).apply();
                        allPostFragment.changePostLayout(postLayout);
                    }
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        if (frontPagePostFragment != null) {
                            mSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, postLayout).apply();
                            frontPagePostFragment.changePostLayout(postLayout);
                        }
                        break;
                    case 1:
                        if (popularPostFragment != null) {
                            mSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST, postLayout).apply();
                            popularPostFragment.changePostLayout(postLayout);
                        }
                        break;
                    case 2:
                        if (allPostFragment != null) {
                            mSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_ALL_POST, postLayout).apply();
                            allPostFragment.changePostLayout(postLayout);
                        }
                }
            }
        }
    }
}
