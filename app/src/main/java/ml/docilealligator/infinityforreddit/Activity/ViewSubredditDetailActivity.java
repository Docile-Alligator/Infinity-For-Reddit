package ml.docilealligator.infinityforreddit.Activity;

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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.AsyncTask.CheckIsSubscribedToSubredditAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.InsertSubredditDataAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.SwitchAccountAsyncTask;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Fragment.SidebarFragment;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Message.ReadMessage;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditViewModel;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditSubscription;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class ViewSubredditDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface {

    public static final String EXTRA_SUBREDDIT_NAME_KEY = "ESN";
    public static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_VIEW_SIDEBAR = "EVSB";

    private static final String FETCH_SUBREDDIT_INFO_STATE = "FSIS";
    private static final String CURRENT_ONLINE_SUBSCRIBERS_STATE = "COSS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";

    @BindView(R.id.coordinator_layout_view_subreddit_detail_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_view_subreddit_detail_activity)
    ViewPager viewPager;
    @BindView(R.id.appbar_layout_view_subreddit_detail_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_subreddit_detail_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_linear_layout_view_subreddit_detail_activity)
    LinearLayout linearLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_view_subreddit_detail_activity)
    TabLayout tabLayout;
    @BindView(R.id.banner_image_view_view_subreddit_detail_activity)
    GifImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_subreddit_detail_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subscribe_subreddit_chip_view_subreddit_detail_activity)
    Chip subscribeSubredditChip;
    @BindView(R.id.subreddit_name_text_view_view_subreddit_detail_activity)
    TextView subredditNameTextView;
    @BindView(R.id.subscriber_count_text_view_view_subreddit_detail_activity)
    TextView nSubscribersTextView;
    @BindView(R.id.online_subscriber_count_text_view_view_subreddit_detail_activity)
    TextView nOnlineSubscribersTextView;
    @BindView(R.id.since_text_view_view_subreddit_detail_activity)
    TextView sinceTextView;
    @BindView(R.id.creation_time_text_view_view_subreddit_detail_activity)
    TextView creationTimeTextView;
    @BindView(R.id.description_text_view_view_subreddit_detail_activity)
    TextView descriptionTextView;
    @BindView(R.id.bottom_navigation_view_subreddit_detail_activity)
    BottomAppBar bottomNavigationView;
    @BindView(R.id.linear_layout_bottom_app_bar_view_subreddit_detail_activity)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.subscriptions_bottom_app_bar_view_subreddit_detail_activity)
    ImageView subscriptionsBottomAppBar;
    @BindView(R.id.go_back_to_main_page_bottom_app_bar_view_subreddit_detail_activity)
    ImageView goBackToMainPageBottomAppBar;
    @BindView(R.id.message_bottom_app_bar_view_subreddit_detail_activity)
    ImageView messageBottomAppBar;
    @BindView(R.id.profile_bottom_app_bar_view_subreddit_detail_activity)
    ImageView profileBottomAppBar;
    @BindView(R.id.fab_view_subreddit_detail_activity)
    FloatingActionButton fab;
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
    CustomThemeWrapper mCustomThemeWrapper;
    public SubredditViewModel mSubredditViewModel;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String subredditName;
    private boolean mFetchSubredditInfoSuccess = false;
    private int mNCurrentOnlineSubscribers = 0;
    private boolean subscriptionReady = false;
    private boolean isInLazyMode = false;
    private boolean showToast = false;
    private boolean showBottomAppBar;
    private boolean lockBottomAppBar;
    private String mMessageFullname;
    private String mNewAccountName;
    private RequestManager glide;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private int unsubscribedColor;
    private int subscribedColor;
    private SlidrInterface mSlidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        setTransparentStatusBarAfterToolbarCollapsed();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_subreddit_detail);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                showToast = true;

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin = navBarHeight;
                    fab.setLayoutParams(params);
                    linearLayoutBottomAppBar.setPadding(0,
                            (int) (6 * getResources().getDisplayMetrics().density), 0, navBarHeight);

                    showToast = true;
                }
            }

            View decorView = window.getDecorView();
            if (isChangeStatusBarIconColor()) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                        if (state == State.COLLAPSED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarCollapsed());
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarExpanded());
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            } else {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if (state == State.COLLAPSED) {
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            }
        } else {
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.EXPANDED) {
                        tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    } else if (state == State.COLLAPSED) {
                        tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    }
                }
            });
        }

        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);
        lockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);

        subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME_KEY);

        if (savedInstanceState == null) {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            getCurrentAccountAndBindView();
        } else {
            mFetchSubredditInfoSuccess = savedInstanceState.getBoolean(FETCH_SUBREDDIT_INFO_STATE);
            mNCurrentOnlineSubscribers = savedInstanceState.getInt(CURRENT_ONLINE_SUBSCRIBERS_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }

            if (mFetchSubredditInfoSuccess) {
                nOnlineSubscribersTextView.setText(getString(R.string.online_subscribers_number_detail, mNCurrentOnlineSubscribers));
            }
        }

        fetchSubredditData();

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();

        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        sortTypeBottomSheetFragment.setArguments(bottomSheetBundle);

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();

        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        String title = "r/" + subredditName;
        subredditNameTextView.setText(title);

        toolbar.setTitle(title);
        adjustToolbar(toolbar);
        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        glide = Glide.with(this);
        Locale locale = getResources().getConfiguration().locale;

        mSubredditViewModel = new ViewModelProvider(this,
                new SubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(this, subredditData -> {
            if (subredditData != null) {
                if (subredditData.getBannerUrl().equals("")) {
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it has no image
                    });
                } else {
                    glide.load(subredditData.getBannerUrl()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.IMAGE_URL_KEY, subredditData.getBannerUrl());
                        intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, subredditName + "-banner.jpg");
                        startActivity(intent);
                    });
                }

                if (subredditData.getIconUrl().equals("")) {
                    glide.load(getDrawable(R.drawable.subreddit_default_icon))
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it is a default icon
                    });
                } else {
                    glide.load(subredditData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0))))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.IMAGE_URL_KEY, subredditData.getIconUrl());
                        intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, subredditName + "-icon.jpg");
                        startActivity(intent);
                    });
                }

                String subredditFullName = "r/" + subredditData.getName();
                if (!title.equals(subredditFullName)) {
                    getSupportActionBar().setTitle(subredditFullName);
                }
                subredditNameTextView.setText(subredditFullName);
                String nSubscribers = getString(R.string.subscribers_number_detail, subredditData.getNSubscribers());
                nSubscribersTextView.setText(nSubscribers);
                creationTimeTextView.setText(new SimpleDateFormat("MMM d, yyyy",
                        locale).format(subredditData.getCreatedUTC()));
                if (subredditData.getDescription().equals("")) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                    descriptionTextView.setText(subredditData.getDescription());
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        collapsingToolbarLayout.setContentScrimColor(mCustomThemeWrapper.getColorPrimary());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        expandedTabTextColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTextColor();
        expandedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabIndicator();
        expandedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabBackground();
        collapsedTabTextColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTextColor();
        collapsedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabIndicator();
        collapsedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabBackground();
        linearLayout.setBackgroundColor(expandedTabBackgroundColor);
        subredditNameTextView.setTextColor(mCustomThemeWrapper.getSubreddit());
        subscribeSubredditChip.setTextColor(mCustomThemeWrapper.getChipTextColor());
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        nSubscribersTextView.setTextColor(primaryTextColor);
        nOnlineSubscribersTextView.setTextColor(primaryTextColor);
        sinceTextView.setTextColor(primaryTextColor);
        creationTimeTextView.setTextColor(primaryTextColor);
        descriptionTextView.setTextColor(primaryTextColor);
        bottomNavigationView.setBackgroundTint(ColorStateList.valueOf(mCustomThemeWrapper.getBottomAppBarBackgroundColor()));
        int bottomAppBarIconColor = mCustomThemeWrapper.getBottomAppBarIconColor();
        subscriptionsBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        goBackToMainPageBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        messageBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        profileBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
        unsubscribedColor = mCustomThemeWrapper.getUnsubscribed();
        subscribedColor = mCustomThemeWrapper.getSubscribed();
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
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
                        }

                        bindView();
                    }).execute();
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    bindView();
                }
            } else {
                if (account == null) {
                    mNullAccessToken = true;
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                }

                bindView();
            }
        }).execute();
    }

    private void fetchSubredditData() {
        if (!mFetchSubredditInfoSuccess) {
            FetchSubredditData.fetchSubredditData(mRetrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
                @Override
                public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                    mNCurrentOnlineSubscribers = nCurrentOnlineSubscribers;
                    nOnlineSubscribersTextView.setText(getString(R.string.online_subscribers_number_detail, nCurrentOnlineSubscribers));
                    new InsertSubredditDataAsyncTask(mRedditDataRoomDatabase, subredditData, () -> mFetchSubredditInfoSuccess = true).execute();
                }

                @Override
                public void onFetchSubredditDataFail() {
                    makeSnackbar(R.string.cannot_fetch_subreddit_info, true);
                    mFetchSubredditInfoSuccess = false;
                }
            });
        }
    }

    private void bindView() {
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

            if (showBottomAppBar) {
                bottomNavigationView.setVisibility(View.VISIBLE);
                subscriptionsBottomAppBar.setOnClickListener(view -> {
                    Intent intent = new Intent(ViewSubredditDetailActivity.this, SubscribedThingListingActivity.class);
                    startActivity(intent);
                });

                subscriptionsBottomAppBar.setOnLongClickListener(view -> {
                    Intent intent = new Intent(ViewSubredditDetailActivity.this, SubscribedThingListingActivity.class);
                    intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                    startActivity(intent);
                    return true;
                });

                goBackToMainPageBottomAppBar.setOnClickListener(view -> EventBus.getDefault().post(new GoBackToMainPageEvent()));

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
        } else {
            bottomNavigationView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }

        subscribeSubredditChip.setOnClickListener(view -> {
            if (mAccessToken == null) {
                Toast.makeText(ViewSubredditDetailActivity.this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return;
            }

            if (subscriptionReady) {
                subscriptionReady = false;
                if (subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                    SubredditSubscription.subscribeToSubreddit(mOauthRetrofit, mRetrofit, mAccessToken,
                            subredditName, mAccountName, mRedditDataRoomDatabase,
                            new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.unsubscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                                    makeSnackbar(R.string.subscribed, false);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.subscribe_failed, false);
                                    subscriptionReady = true;
                                }
                            });
                } else {
                    SubredditSubscription.unsubscribeToSubreddit(mOauthRetrofit, mAccessToken,
                            subredditName, mAccountName, mRedditDataRoomDatabase,
                            new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.subscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                                    makeSnackbar(R.string.unsubscribed, false);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.unsubscribe_failed, false);
                                    subscriptionReady = true;
                                }
                            });
                }
            }
        });

        new CheckIsSubscribedToSubredditAsyncTask(mRedditDataRoomDatabase, subredditName, mAccountName,
                new CheckIsSubscribedToSubredditAsyncTask.CheckIsSubscribedToSubredditListener() {
                    @Override
                    public void isSubscribed() {
                        subscribeSubredditChip.setText(R.string.unsubscribe);
                        subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                        subscriptionReady = true;
                    }

                    @Override
                    public void isNotSubscribed() {
                        subscribeSubredditChip.setText(R.string.subscribe);
                        subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                        subscriptionReady = true;
                    }
                }).execute();

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);


        boolean viewSidebar = getIntent().getBooleanExtra(EXTRA_VIEW_SIDEBAR, false);
        if (viewSidebar) {
            viewPager.setCurrentItem(1, false);
        }

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mAccessToken != null) {
                    if (showBottomAppBar) {
                        bottomNavigationView.performShow();
                    }
                    fab.show();
                }
                if (isInLazyMode) {
                    if (viewPager.getCurrentItem() == 0) {
                        sectionsPagerAdapter.resumeLazyMode();
                    } else {
                        sectionsPagerAdapter.pauseLazyMode();
                    }
                }

                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_subreddit_detail_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
        if (isInLazyMode) {
            lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            collapsingToolbarLayout.setLayoutParams(params);
        } else {
            lazyModeItem.setTitle(R.string.action_start_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            collapsingToolbarLayout.setLayoutParams(params);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sort_view_subreddit_detail_activity:
                sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
                return true;
            case R.id.action_search_view_subreddit_detail_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                return true;
            case R.id.action_refresh_view_subreddit_detail_activity:
                if (mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity).setTitle(R.string.action_start_lazy_mode);
                }
                sectionsPagerAdapter.refresh();
                mFetchSubredditInfoSuccess = false;
                fetchSubredditData();
                return true;
            case R.id.action_lazy_mode_view_subreddit_detail_activity:
                if (sectionsPagerAdapter != null) {
                    MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
                    if (isInLazyMode) {
                        isInLazyMode = false;
                        sectionsPagerAdapter.stopLazyMode();
                        lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                        collapsingToolbarLayout.setLayoutParams(params);
                    } else {
                        isInLazyMode = true;
                        if (sectionsPagerAdapter.startLazyMode()) {
                            lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                            appBarLayout.setExpanded(false);
                            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                            collapsingToolbarLayout.setLayoutParams(params);
                        } else {
                            isInLazyMode = false;
                        }
                    }
                }
                return true;
            case R.id.action_change_post_layout_view_subreddit_detail_activity:
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                return true;
            case R.id.action_share_view_subreddit_detail_activity:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.reddit.com/r/" + subredditName);
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                } else {
                    Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_SUBREDDIT_INFO_STATE, mFetchSubredditInfoSuccess);
        outState.putInt(CURRENT_ONLINE_SUBSCRIBERS_STATE, mNCurrentOnlineSubscribers);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void makeSnackbar(int resId, boolean retry) {
        if (showToast) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        } else {
            if (retry) {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).setAction(R.string.retry,
                        view -> fetchSubredditData()).show();
            } else {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changeSortType(sortType);
        }
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
                intent = new Intent(this, PostTextActivity.class);
                intent.putExtra(PostTextActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(this, PostLinkActivity.class);
                intent.putExtra(PostLinkActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(this, PostImageActivity.class);
                intent.putExtra(PostImageActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(this, PostVideoActivity.class);
                intent.putExtra(PostVideoActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
        }
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, postLayout).apply();
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    public void contentScrollUp() {
        if (mAccessToken != null) {
            if (showBottomAppBar && !lockBottomAppBar) {
                bottomNavigationView.performShow();
            }
            if (!(showBottomAppBar && lockBottomAppBar)) {
                fab.show();
            }
        }
    }

    public void contentScrollDown() {
        if (mAccessToken != null) {
            if (!(showBottomAppBar && lockBottomAppBar)) {
                fab.hide();
            }
            if (showBottomAppBar && !lockBottomAppBar) {
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
    public void goBackToMainPageEvent(GoBackToMainPageEvent event) {
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

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PostFragment postFragment;
        private SidebarFragment sidebarFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, subredditName);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            }
            SidebarFragment fragment = new SidebarFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SidebarFragment.EXTRA_SUBREDDIT_NAME, subredditName);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Posts";
                case 1:
                    return "Sidebar";
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    postFragment = (PostFragment) fragment;
                    displaySortTypeInToolbar();
                    break;
                case 1:
                    sidebarFragment = (SidebarFragment) fragment;
            }
            return fragment;
        }

        public boolean handleKeyDown(int keyCode) {
            return viewPager.getCurrentItem() == 0 && postFragment.handleKeyDown(keyCode);
        }

        public void refresh() {
            if (viewPager.getCurrentItem() == 0) {
                if (postFragment != null) {
                    postFragment.refresh();
                }
            } else {
                if (sidebarFragment != null) {
                    sidebarFragment.fetchSubredditData();
                }
            }
        }

        boolean startLazyMode() {
            if (postFragment != null) {
                return ((FragmentCommunicator) postFragment).startLazyMode();
            }
            return false;
        }

        void stopLazyMode() {
            if (postFragment != null) {
                ((FragmentCommunicator) postFragment).stopLazyMode();
            }
        }

        void resumeLazyMode() {
            if (postFragment != null) {
                ((FragmentCommunicator) postFragment).resumeLazyMode(false);
            }
        }

        void pauseLazyMode() {
            if (postFragment != null) {
                ((FragmentCommunicator) postFragment).pauseLazyMode(false);
            }
        }

        public void changeSortType(SortType sortType) {
            if (postFragment != null) {
                mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE + subredditName, sortType.getType().name()).apply();
                if (sortType.getTime() != null) {
                    mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE + subredditName, sortType.getTime().name()).apply();
                }

                postFragment.changeSortType(sortType);
                displaySortTypeInToolbar();
            }
        }

        public void changeNSFW(boolean nsfw) {
            if (postFragment != null) {
                postFragment.changeNSFW(nsfw);
            }
        }

        void changePostLayout(int postLayout) {
            if (postFragment != null) {
                mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, postLayout).apply();
                ((FragmentCommunicator) postFragment).changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            if (viewPager.getCurrentItem() == 0) {
                if (postFragment != null) {
                    postFragment.goBackToTop();
                }
            } else {
                if (sidebarFragment != null) {
                    sidebarFragment.goBackToTop();
                }
            }
        }

        void displaySortTypeInToolbar() {
            if (viewPager.getCurrentItem() == 0) {
                if (postFragment != null) {
                    SortType sortType = postFragment.getSortType();
                    Utils.displaySortTypeInToolbar(sortType, toolbar);
                }
            }
        }
    }

    private void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    private void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
        }
    }
}
