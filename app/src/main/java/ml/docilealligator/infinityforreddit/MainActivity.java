package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import Account.Account;
import Account.AccountViewModel;
import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedUserDatabase.SubscribedUserData;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback {

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

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.coordinator_layout_main_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_main_activity) AppBarLayout appBarLayout;
    @BindView(R.id.view_pager_main_activity) ViewPager viewPager;
    @BindView(R.id.collapsing_toolbar_layout_main_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.nested_scroll_view_main_activity) NestedScrollView nestedScrollView;
    @BindView(R.id.all_drawer_items_linear_layout_main_activity) LinearLayout allDrawerItemsLinearLayout;
    @BindView(R.id.account_label_main_activity) TextView accountLabelTextView;
    @BindView(R.id.profile_linear_layout_main_activity) LinearLayout profileLinearLayout;
    @BindView(R.id.subscriptions_linear_layout_main_activity) LinearLayout subscriptionLinearLayout;
    @BindView(R.id.inbox_linear_layout_main_activity) LinearLayout inboxLinearLayout;
    @BindView(R.id.post_label_main_activity) TextView postLabelTextView;
    @BindView(R.id.upvoted_linear_layout_main_activity) LinearLayout upvotedLinearLayout;
    @BindView(R.id.downvoted_linear_layout_main_activity) LinearLayout downvotedLinearLayout;
    @BindView(R.id.hidden_linear_layout_main_activity) LinearLayout hiddenLinearLayout;
    @BindView(R.id.saved_linear_layout_main_activity) LinearLayout savedLinearLayout;
    @BindView(R.id.gilded_linear_layout_main_activity) LinearLayout gildedLinearLayout;
    @BindView(R.id.divider_main_activity) View divider;
    @BindView(R.id.settings_linear_layout_main_activity) LinearLayout settingsLinearLayout;
    @BindView(R.id.account_recycler_view_main_activity) RecyclerView accountRecyclerView;
    @BindView(R.id.tab_layout_main_activity) TabLayout tabLayout;
    @BindView(R.id.fab_main_activity) FloatingActionButton fab;

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

    AccountViewModel accountViewModel;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBarWithTransparentStatusBar);

        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Resources resources = getResources();

            if(resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                Window window = getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                boolean lightNavBar = false;
                if((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                    lightNavBar = true;
                }
                boolean finalLightNavBar = lightNavBar;

                View decorView = window.getDecorView();
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if(state == State.COLLAPSED) {
                            if(finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        } else if(state == State.EXPANDED) {
                            if(finalLightNavBar) {
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
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin = navBarHeight;
                    fab.setLayoutParams(params);

                    nestedScrollView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int themeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (themeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case 2:
                if(systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
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

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        if(savedInstanceState != null) {
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

            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            getCurrentAccountAndBindView();
        }

        fab.setOnClickListener(view -> postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag()));
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            boolean enableNotification = mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
            String notificationInterval = mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1");

            WorkManager workManager = WorkManager.getInstance(this);

            if(mNewAccountName != null) {
                if(account == null || !account.getUsername().equals(mNewAccountName)) {
                    new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
                        EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                        Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                        mNewAccountName = null;
                        if(newAccount == null) {
                            mNullAccessToken = true;

                            if(mMenu != null) {
                                mMenu.findItem(R.id.action_subscriptions_main_activity).setVisible(false);
                            }
                        } else {
                            mAccessToken = newAccount.getAccessToken();
                            mAccountName = newAccount.getUsername();
                            mProfileImageUrl = newAccount.getProfileImageUrl();
                            mBannerImageUrl = newAccount.getBannerImageUrl();
                            mKarma = newAccount.getKarma();

                            if(mMenu != null) {
                                mMenu.findItem(R.id.action_subscriptions_main_activity).setVisible(true);
                            }
                        }

                        if(enableNotification) {
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

                    if(mMenu != null) {
                        mMenu.findItem(R.id.action_subscriptions_main_activity).setVisible(true);
                    }

                    if(enableNotification) {
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
                if(account == null) {
                    mNullAccessToken = true;

                    if(mMenu != null) {
                        mMenu.findItem(R.id.action_subscriptions_main_activity).setVisible(false);
                    }
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    mProfileImageUrl = account.getProfileImageUrl();
                    mBannerImageUrl = account.getBannerImageUrl();
                    mKarma = account.getKarma();

                    if(mMenu != null) {
                        mMenu.findItem(R.id.action_subscriptions_main_activity).setVisible(true);
                    }
                }

                if(enableNotification) {
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
        if(isDestroyed()) {
            return;
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                fab.show();
                if(isInLazyMode) {
                    if(position == sectionsPagerAdapter.getCurrentLazyModeFragmentPosition()) {
                        sectionsPagerAdapter.resumeLazyMode();
                    } else {
                        sectionsPagerAdapter.pauseLazyMode();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
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

        if(getIntent().hasExtra(EXTRA_POST_TYPE)) {
            String type = getIntent().getStringExtra(EXTRA_POST_TYPE);
            if(type != null && type.equals("popular")) {
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

        if(mDrawerOnAccountSwitch) {
            mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_up_24px));
            accountRecyclerView.setVisibility(View.VISIBLE);
            allDrawerItemsLinearLayout.setVisibility(View.GONE);
        } else {
            mDropIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_arrow_drop_down_24px));
            accountRecyclerView.setVisibility(View.GONE);
            allDrawerItemsLinearLayout.setVisibility(View.VISIBLE);
        }

        header.setOnClickListener(view -> {
            if(mDrawerOnAccountSwitch) {
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

        if(mAccessToken != null) {
            mKarmaTextView.setText(getString(R.string.karma_info, mKarma));
            mAccountNameTextView.setText(mAccountName);
            if(mMessageFullname != null) {
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
            profileLinearLayout.setVisibility(View.GONE);
            subscriptionLinearLayout.setVisibility(View.GONE);
            inboxLinearLayout.setVisibility(View.GONE);
            postLabelTextView.setVisibility(View.GONE);
            upvotedLinearLayout.setVisibility(View.GONE);
            downvotedLinearLayout.setVisibility(View.GONE);
            hiddenLinearLayout.setVisibility(View.GONE);
            savedLinearLayout.setVisibility(View.GONE);
            gildedLinearLayout.setVisibility(View.GONE);
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

        profileLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
            startActivity(intent);
            drawer.closeDrawers();
        });

        subscriptionLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubscribedThingListingActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        inboxLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewMessageActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        upvotedLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_UPVOTED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        downvotedLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_DOWNVOTED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        hiddenLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_HIDDEN);
            startActivity(intent);
            drawer.closeDrawers();
        });

        savedLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountSavedThingActivity.class);
            startActivity(intent);
            drawer.closeDrawers();
        });

        gildedLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountPostsActivity.class);
            intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_GILDED);
            startActivity(intent);
            drawer.closeDrawers();
        });

        settingsLinearLayout.setOnClickListener(view -> {
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
        if(requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
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
        MenuItem subscriptionsItem = mMenu.findItem(R.id.action_subscriptions_main_activity);

        if(mAccessToken != null) {
            subscriptionsItem.setVisible(true);
        } else {
            subscriptionsItem.setVisible(false);
        }

        if(isInLazyMode) {
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
            case R.id.action_subscriptions_main_activity:
                Intent subscriptionsIntent = new Intent(this, SubscribedThingListingActivity.class);
                startActivity(subscriptionsIntent);
                return true;
            case R.id.action_sort_main_activity:
                if(viewPager.getCurrentItem() == 1 ||viewPager.getCurrentItem() == 2) {
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
                if(mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_main_activity).setTitle(R.string.action_start_lazy_mode);
                }
                sectionsPagerAdapter.refresh();
                mFetchUserInfoSuccess = false;
                loadUserData();
                return true;
            case R.id.action_lazy_mode_main_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);
                if(isInLazyMode) {
                    sectionsPagerAdapter.stopLazyMode();
                    isInLazyMode = false;
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    if(sectionsPagerAdapter.startLazyMode()) {
                        isInLazyMode = true;
                        lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                        collapsingToolbarLayout.setLayoutParams(params);
                    }
                }
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
            super.onBackPressed();
        }
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
    public void sortTypeSelected(String sortType) {
        sectionsPagerAdapter.changeSortType(sortType);
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

    public void postScrollUp() {
        fab.show();
    }

    public void postScrollDown() {
        fab.hide();
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if(!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
    }

    @Subscribe
    public void onChangeFontSizeEvent(ChangeFontSizeEvent changeFontSizeEvent) {
        recreate();
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
            if(mAccessToken == null) {
                if(position == 0) {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "popular");
                    bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                } else {
                    PostFragment fragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, "all");
                    bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
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
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_BEST);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else if(position == 1) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount()
        {
            if(mAccessToken == null) {
                return 2;
            }
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(mAccessToken == null) {
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
            if(mAccessToken == null) {
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

        boolean startLazyMode() {
            if(mAccessToken == null) {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return ((FragmentCommunicator) popularPostFragment).startLazyMode();
                    case 1:
                        return ((FragmentCommunicator) allPostFragment).startLazyMode();
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        return ((FragmentCommunicator) frontPagePostFragment).startLazyMode();
                    case 1:
                        return ((FragmentCommunicator) popularPostFragment).startLazyMode();
                    case 2:
                        return ((FragmentCommunicator) allPostFragment).startLazyMode();
                }
            }

            return false;
        }

        void stopLazyMode() {
            if(mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) popularPostFragment).stopLazyMode();
                        break;
                    case 1:
                        ((FragmentCommunicator) allPostFragment).stopLazyMode();
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) frontPagePostFragment).stopLazyMode();
                        break;
                    case 1:
                        ((FragmentCommunicator) popularPostFragment).stopLazyMode();
                        break;
                    case 2:
                        ((FragmentCommunicator) allPostFragment).stopLazyMode();
                        break;
                }
            }
        }

        void resumeLazyMode() {
            if(mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) popularPostFragment).resumeLazyMode(false);
                        break;
                    case 1:
                        ((FragmentCommunicator) allPostFragment).resumeLazyMode(false);
                        break;
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) frontPagePostFragment).resumeLazyMode(false);
                        break;
                    case 1:
                        ((FragmentCommunicator) popularPostFragment).resumeLazyMode(false);
                        break;
                    case 2:
                        ((FragmentCommunicator) allPostFragment).resumeLazyMode(false);
                        break;
                }
            }
        }

        void pauseLazyMode() {
            if(mAccessToken == null) {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) popularPostFragment).pauseLazyMode(false);
                        break;
                    case 1:
                        ((FragmentCommunicator) allPostFragment).pauseLazyMode(false);
                }
            } else {
                switch (getCurrentLazyModeFragmentPosition()) {
                    case 0:
                        ((FragmentCommunicator) frontPagePostFragment).pauseLazyMode(false);
                        break;
                    case 1:
                        ((FragmentCommunicator) popularPostFragment).pauseLazyMode(false);
                        break;
                    case 2:
                        ((FragmentCommunicator) allPostFragment).pauseLazyMode(false);
                }
            }
        }

        int getCurrentLazyModeFragmentPosition() {
            if(mAccessToken == null) {
                if(!isInLazyMode) {
                    return -1;
                } else if(popularPostFragment != null && ((FragmentCommunicator) popularPostFragment).isInLazyMode()) {
                    return 0;
                } else if(allPostFragment != null && ((FragmentCommunicator) allPostFragment).isInLazyMode()) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                if(!isInLazyMode) {
                    return -1;
                } else if(frontPagePostFragment != null && ((FragmentCommunicator) frontPagePostFragment).isInLazyMode()) {
                    return 0;
                } else if(popularPostFragment != null && ((FragmentCommunicator) popularPostFragment).isInLazyMode()) {
                    return 1;
                } else if(allPostFragment != null && ((FragmentCommunicator) allPostFragment).isInLazyMode()) {
                    return 2;
                } else {
                    return -1;
                }
            }
        }

        void changeSortType(String sortType) {
            if(mAccessToken == null) {
                if(viewPager.getCurrentItem() == 0) {
                    popularPostFragment.changeSortType(sortType);
                } else {
                    allPostFragment.changeSortType(sortType);
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        frontPagePostFragment.changeSortType(sortType);
                        break;
                    case 1:
                        popularPostFragment.changeSortType(sortType);
                        break;
                    case 2:
                        allPostFragment.changeSortType(sortType);
                }
            }
        }

        public void refresh() {
            if(mAccessToken == null) {
                if(viewPager.getCurrentItem() == 0) {
                    if(popularPostFragment != null) {
                        ((FragmentCommunicator) popularPostFragment).refresh();
                    }
                } else {
                    if(allPostFragment != null) {
                        ((FragmentCommunicator) allPostFragment).refresh();
                    }
                }
            } else {
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        if(frontPagePostFragment != null) {
                            ((FragmentCommunicator) frontPagePostFragment).refresh();
                        }
                        break;
                    case 1:
                        if(popularPostFragment != null) {
                            ((FragmentCommunicator) popularPostFragment).refresh();
                        }
                        break;
                    case 2:
                        if(allPostFragment != null) {
                            ((FragmentCommunicator) allPostFragment).refresh();
                        }
                }
            }
        }

        void changeNSFW(boolean nsfw) {
            if(frontPagePostFragment != null) {
                frontPagePostFragment.changeNSFW(nsfw);
            }
            if(popularPostFragment != null) {
                popularPostFragment.changeNSFW(nsfw);
            }
            if(allPostFragment != null) {
                allPostFragment.changeNSFW(nsfw);
            }
        }
    }
}
