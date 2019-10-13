package ml.docilealligator.infinityforreddit.Activity;

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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Fragment.SearchPostSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.UserThingSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.Post;
import ml.docilealligator.infinityforreddit.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;

public class FilteredThingActivity extends BaseActivity implements SortTypeSelectionCallback {

    public static final String EXTRA_NAME = "ESN";
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_FILTER = "EF";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_USER_WHERE = "EUW";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.appbar_layout_filtered_posts_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_filtered_posts_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_filtered_posts_activity)
    Toolbar toolbar;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
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
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filtered_thing);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                && (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                || resources.getBoolean(R.bool.isTablet))
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            boolean lightNavBar = false;
            if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                lightNavBar = true;
            }
            boolean finalLightNavBar = lightNavBar;

            View decorView = window.getDecorView();
            if (finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
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
                params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                toolbar.setLayoutParams(params);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        name = getIntent().getStringExtra(EXTRA_NAME);
        postType = getIntent().getIntExtra(EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
        int filter = getIntent().getIntExtra(EXTRA_FILTER, Post.TEXT_TYPE);

        if (savedInstanceState != null) {
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);

            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView(filter);
            } else {
                bindView(filter, false);
            }
        } else {
            getCurrentAccountAndBindView(filter);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    private void getCurrentAccountAndBindView(int filter) {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }
            bindView(filter, true);
        }).execute();
    }

    private void bindView(int filter, boolean initializeFragment) {
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
                if (name.equals("popular") || name.equals("all")) {
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

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();

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
            case Post.GIF_TYPE:
                toolbar.setSubtitle(R.string.gif);
        }

        if (initializeFragment) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_NAME, name);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, postType);
            bundle.putInt(PostFragment.EXTRA_FILTER, filter);
            bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            if (postType == PostDataSource.TYPE_USER) {
                bundle.putString(PostFragment.EXTRA_USER_WHERE, getIntent().getStringExtra(EXTRA_USER_WHERE));
            }
            if (postType == PostDataSource.TYPE_SEARCH) {
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
                        if (name.equals("popular") || name.equals("all")) {
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
                if (mMenu != null) {
                    mMenu.findItem(R.id.action_lazy_mode_filtered_thing_activity).setTitle(R.string.action_start_lazy_mode);
                }
                if (mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                }
                return true;
            case R.id.action_lazy_mode_filtered_thing_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_filtered_thing_activity);
                if (isInLazyMode) {
                    ((FragmentCommunicator) mFragment).stopLazyMode();
                    isInLazyMode = false;
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    if (((FragmentCommunicator) mFragment).startLazyMode()) {
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
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
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
    public void sortTypeSelected(SortType sortType) {
        ((PostFragment) mFragment).changeSortType(sortType);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
