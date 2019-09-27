package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditDao;
import SubredditDatabase.SubredditData;
import SubredditDatabase.SubredditViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class ViewSubredditDetailActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback {

    static final String EXTRA_SUBREDDIT_NAME_KEY = "ESN";
    static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";

    private static final String FETCH_SUBREDDIT_INFO_STATE = "FSIS";
    private static final String CURRENT_ONLINE_SUBSCRIBERS_STATE = "COSS";
    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";

    @BindView(R.id.coordinator_layout_view_subreddit_detail_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_view_subreddit_detail) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_subreddit_detail_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.banner_image_view_view_subreddit_detail_activity) GifImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_subreddit_detail_activity) GifImageView iconGifImageView;
    @BindView(R.id.subscribe_subreddit_chip_view_subreddit_detail_activity) Chip subscribeSubredditChip;
    @BindView(R.id.subreddit_name_text_view_view_subreddit_detail_activity) TextView subredditNameTextView;
    @BindView(R.id.subscriber_count_text_view_view_subreddit_detail_activity) TextView nSubscribersTextView;
    @BindView(R.id.online_subscriber_count_text_view_view_subreddit_detail_activity) TextView nOnlineSubscribersTextView;
    @BindView(R.id.description_text_view_view_subreddit_detail_activity) TextView descriptionTextView;
    @BindView(R.id.fab_view_subreddit_detail_activity) FloatingActionButton fab;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String subredditName;
    private boolean mFetchSubredditInfoSuccess = false;
    private int mNCurrentOnlineSubscribers = 0;
    private boolean subscriptionReady = false;
    private boolean isInLazyMode = false;
    private boolean showToast = false;
    private String mMessageFullname;
    private String mNewAccountName;

    private RequestManager glide;
    private Fragment mFragment;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;

    private SubredditViewModel mSubredditViewModel;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        setContentView(R.layout.activity_view_subreddit_detail);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

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
                        if (state == State.COLLAPSED) {
                            if(finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        } else if (state == State.EXPANDED) {
                            if(finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        }
                    }
                });

                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin = resources.getDimensionPixelSize(navBarResourceId);
                    fab.setLayoutParams(params);

                    showToast = true;
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

        subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME_KEY);

        if(savedInstanceState == null) {
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

            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView(false);
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_subreddit_detail_activity, mFragment).commit();
            }

            if(mFetchSubredditInfoSuccess) {
                nOnlineSubscribersTextView.setText(getString(R.string.online_subscribers_number_detail, mNCurrentOnlineSubscribers));
            }
        }

        fetchSubredditData();

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();

        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        sortTypeBottomSheetFragment.setArguments(bottomSheetBundle);

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        //Get status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        String title = "r/" + subredditName;
        subredditNameTextView.setText(title);

        toolbar.setTitle(title);
        ViewGroup.MarginLayoutParams toolbarParams = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        toolbarParams.topMargin = statusBarHeight;
        toolbar.setLayoutParams(toolbarParams);
        setSupportActionBar(toolbar);

        glide = Glide.with(this);

        mSubredditViewModel = new ViewModelProvider(this,
                new SubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(this, subredditData -> {
            if(subredditData != null) {
                if(subredditData.getBannerUrl().equals("")) {
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it has no image
                    });
                } else {
                    glide.load(subredditData.getBannerUrl()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getBannerUrl());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-banner");
                        startActivity(intent);
                    });
                }

                if(subredditData.getIconUrl().equals("")) {
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
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, subredditData.getIconUrl());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName + "-icon");
                        startActivity(intent);
                    });
                }

                String subredditFullName = "r/" + subredditData.getName();
                if(!title.equals(subredditFullName)) {
                    getSupportActionBar().setTitle(subredditFullName);
                }
                subredditNameTextView.setText(subredditFullName);
                String nSubscribers = getString(R.string.subscribers_number_detail, subredditData.getNSubscribers());
                nSubscribersTextView.setText(nSubscribers);
                if(subredditData.getDescription().equals("")) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                    descriptionTextView.setText(subredditData.getDescription());
                }
            }
        });

        fab.setOnClickListener(view -> {
            if(mAccessToken == null) {
                Toast.makeText(ViewSubredditDetailActivity.this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return;
            }

            postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
        });
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(mNewAccountName != null) {
                if(account == null || !account.getUsername().equals(mNewAccountName)) {
                    new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
                        EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                        Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                        mNewAccountName = null;
                        if(newAccount == null) {
                            mNullAccessToken = true;
                        } else {
                            mAccessToken = newAccount.getAccessToken();
                            mAccountName = newAccount.getUsername();
                        }

                        bindView(true);
                    }).execute();
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    bindView(true);
                }
            } else {
                if(account == null) {
                    mNullAccessToken = true;
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                }

                bindView(true);
            }
        }).execute();
    }

    private void fetchSubredditData() {
        if(!mFetchSubredditInfoSuccess) {
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

    private void bindView(boolean initializeFragment) {
        if(mAccessToken != null && mMessageFullname != null) {
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

        subscribeSubredditChip.setOnClickListener(view -> {
            if(mAccessToken == null) {
                Toast.makeText(ViewSubredditDetailActivity.this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return;
            }

            if(subscriptionReady) {
                subscriptionReady = false;
                if(subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                    SubredditSubscription.subscribeToSubreddit(mOauthRetrofit, mRetrofit, mAccessToken,
                            subredditName, mAccountName, mRedditDataRoomDatabase,
                            new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.unsubscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
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
                                    subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.backgroundColorPrimaryDark));
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
                        subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
                        subscriptionReady = true;
                    }

                    @Override
                    public void isNotSubscribed() {
                        subscribeSubredditChip.setText(R.string.subscribe);
                        subscribeSubredditChip.setChipBackgroundColor(getResources().getColorStateList(R.color.backgroundColorPrimaryDark));
                        subscriptionReady = true;
                    }
                }).execute();

        if(initializeFragment) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_NAME, subredditName);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
            bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_BEST);
            bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
            bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_subreddit_detail_activity, mFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_subreddit_detail_activity, menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
        if(isInLazyMode) {
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
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_IS_USER, false);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, false);
                startActivity(intent);
                break;
            case R.id.action_refresh_view_subreddit_detail_activity:
                if(mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity).setTitle(R.string.action_start_lazy_mode);
                }
                if(mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                    mFetchSubredditInfoSuccess = false;
                    fetchSubredditData();
                }
                break;
            case R.id.action_lazy_mode_view_subreddit_detail_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
                if(isInLazyMode) {
                    isInLazyMode = false;
                    ((FragmentCommunicator) mFragment).stopLazyMode();
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    isInLazyMode = true;
                    if(((FragmentCommunicator) mFragment).startLazyMode()) {
                        lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                        appBarLayout.setExpanded(false);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                        collapsingToolbarLayout.setLayoutParams(params);
                    } else {
                        isInLazyMode = false;
                    }
                }
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
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void makeSnackbar(int resId, boolean retry) {
        if(showToast) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        } else {
            if(retry) {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).setAction(R.string.retry,
                        view -> fetchSubredditData()).show();
            } else {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void sortTypeSelected(String sortType) {
        ((PostFragment) mFragment).changeSortType(sortType);
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
        ((FragmentCommunicator) mFragment).changeNSFW(changeNSFWEvent.nsfw);
    }


    private static class InsertSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {

        interface InsertSubredditDataAsyncTaskListener {
            void insertSuccess();
        }

        private SubredditDao mSubredditDao;
        private SubredditData subredditData;
        private InsertSubredditDataAsyncTaskListener insertSubredditDataAsyncTaskListener;

        InsertSubredditDataAsyncTask(RedditDataRoomDatabase db, SubredditData subredditData,
                                     InsertSubredditDataAsyncTaskListener insertSubredditDataAsyncTaskListener) {
            mSubredditDao = db.subredditDao();
            this.subredditData = subredditData;
            this.insertSubredditDataAsyncTaskListener = insertSubredditDataAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mSubredditDao.insert(subredditData);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            insertSubredditDataAsyncTaskListener.insertSuccess();
        }
    }
}
