package ml.docilealligator.infinityforreddit;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class FilteredThingActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback,
        SearchPostSortTypeBottomSheetFragment.SearchSortTypeSelectionCallback, UserThingSortTypeBottomSheetFragment.UserThingSortTypeSelectionCallback {

    static final String EXTRA_NAME = "ESN";
    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_FILTER = "EF";
    static final String EXTRA_POST_TYPE = "EPT";
    static final String EXTRA_SORT_TYPE = "EST";
    static final String EXTRA_USER_WHERE = "EUW";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.appbar_layout_filtered_posts_activity) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_filtered_posts_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_filtered_posts_activity) Toolbar toolbar;

    private boolean isInLazyMode = false;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String name;
    private int postType;

    private Fragment mFragment;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;

    private SortTypeBottomSheetFragment bestSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment popularAndAllSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment subredditSortTypeBottomSheetFragment;
    private UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment;
    private SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment;

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

        setContentView(R.layout.activity_filtered_thing);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                && (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                || resources.getBoolean(R.bool.isTablet))) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            boolean lightNavBar = false;
            if((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                lightNavBar = true;
            }
            boolean finalLightNavBar = lightNavBar;

            View decorView = window.getDecorView();
            if(finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
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

            int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResourceId > 0) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                toolbar.setLayoutParams(params);
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

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        name = getIntent().getStringExtra(EXTRA_NAME);
        postType = getIntent().getIntExtra(EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
        int filter = getIntent().getIntExtra(EXTRA_FILTER, Post.TEXT_TYPE);
        String sortType = getIntent().getStringExtra(EXTRA_SORT_TYPE);

        if(savedInstanceState != null) {
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView(filter, sortType);
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
                bindView(filter, sortType, false);
            }
        } else {
            getCurrentAccountAndBindView(filter, sortType);
        }
    }

    private void getCurrentAccountAndBindView(int filter, String sortType) {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
            bindView(filter, sortType, true);
        }).execute();
    }

    private void bindView(int filter, String sortType, boolean initializeFragment) {
        switch (postType) {
            case PostDataSource.TYPE_FRONT_PAGE:
                getSupportActionBar().setTitle(name);

                bestSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                Bundle bestBundle = new Bundle();
                bestBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
                bestSortTypeBottomSheetFragment.setArguments(bestBundle);
                break;
            case PostDataSource.TYPE_SEARCH:
                getSupportActionBar().setTitle(R.string.search);

                searchPostSortTypeBottomSheetFragment = new SearchPostSortTypeBottomSheetFragment();
                Bundle searchBundle = new Bundle();
                searchPostSortTypeBottomSheetFragment.setArguments(searchBundle);
                break;
            case PostDataSource.TYPE_SUBREDDIT:
                if(name.equals("popular") || name.equals("all")) {
                    getSupportActionBar().setTitle(name.substring(0, 1).toUpperCase() + name.substring(1));

                    popularAndAllSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                    Bundle popularBundle = new Bundle();
                    popularBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
                    popularAndAllSortTypeBottomSheetFragment.setArguments(popularBundle);
                } else {
                    String subredditNamePrefixed = "r/" + name;
                    getSupportActionBar().setTitle(subredditNamePrefixed);

                    subredditSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                    Bundle bottomSheetBundle = new Bundle();
                    bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
                    subredditSortTypeBottomSheetFragment.setArguments(bottomSheetBundle);
                }
                break;
            case PostDataSource.TYPE_USER:
                String usernamePrefixed = "u/" + name;
                getSupportActionBar().setTitle(usernamePrefixed);

                userThingSortTypeBottomSheetFragment = new UserThingSortTypeBottomSheetFragment();
                break;
        }

        switch (filter) {
            case Post.NSFW_TYPE:
                toolbar.setSubtitle(R.string.nsfw);
                break;
            case Post.TEXT_TYPE:
                toolbar.setSubtitle(R.string.text);
                break;
            case Post.LINK_TYPE:
            case Post.NO_PREVIEW_LINK_TYPE:
                toolbar.setSubtitle(R.string.link);
                break;
            case Post.IMAGE_TYPE:
                toolbar.setSubtitle(R.string.image);
                break;
            case Post.VIDEO_TYPE:
                toolbar.setSubtitle(R.string.video);
                break;
            case Post.GIF_VIDEO_TYPE:
                toolbar.setSubtitle(R.string.gif);
        }

        if(initializeFragment) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_NAME, name);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, postType);
            bundle.putString(PostFragment.EXTRA_SORT_TYPE, sortType);
            bundle.putInt(PostFragment.EXTRA_FILTER, filter);
            bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            if(postType == PostDataSource.TYPE_USER) {
                bundle.putString(PostFragment.EXTRA_USER_WHERE, getIntent().getStringExtra(EXTRA_USER_WHERE));
            }
            if(postType == PostDataSource.TYPE_SEARCH) {
                bundle.putString(PostFragment.EXTRA_QUERY, getIntent().getStringExtra(EXTRA_QUERY));
            }
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtered_posts_activity, menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_filtered_thing_activity);
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sort_filtered_thing_activity:
                switch (postType) {
                    case PostDataSource.TYPE_FRONT_PAGE:
                        bestSortTypeBottomSheetFragment.show(getSupportFragmentManager(), bestSortTypeBottomSheetFragment.getTag());
                        break;
                    case PostDataSource.TYPE_SEARCH:
                        searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                        break;
                    case PostDataSource.TYPE_SUBREDDIT:
                        if(name.equals("popular") || name.equals("all")) {
                            popularAndAllSortTypeBottomSheetFragment.show(getSupportFragmentManager(), popularAndAllSortTypeBottomSheetFragment.getTag());
                        } else {
                            subredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), subredditSortTypeBottomSheetFragment.getTag());
                        }
                        break;
                    case PostDataSource.TYPE_USER:
                        userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
                }
                return true;
            case R.id.action_refresh_filtered_thing_activity:
                if(mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_filtered_thing_activity).setTitle(R.string.action_start_lazy_mode);
                }
                if(mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                }
                return true;
            case R.id.action_lazy_mode_filtered_thing_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_filtered_thing_activity);
                if(isInLazyMode) {
                    ((FragmentCommunicator) mFragment).stopLazyMode();
                    isInLazyMode = false;
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    if(((FragmentCommunicator) mFragment).startLazyMode()) {
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void searchSortTypeSelected(String sortType) {
        ((PostFragment)mFragment).changeSortType(sortType);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        ((PostFragment)mFragment).changeSortType(sortType);
    }

    @Override
    public void userThingSortTypeSelected(String sortType) {
        ((PostFragment)mFragment).changeSortType(sortType);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
