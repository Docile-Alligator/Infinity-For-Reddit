package ml.docilealligator.infinityforreddit;

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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountPostsActivity extends AppCompatActivity implements UserThingSortTypeBottomSheetFragment.UserThingSortTypeSelectionCallback {

    static final String EXTRA_USER_WHERE = "EUW";

    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.collapsing_toolbar_layout_account_posts_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar_layout_account_posts_activity) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_account_posts_activity) Toolbar toolbar;

    private boolean isInLazyMode = false;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String mUserWhere;

    private Fragment mFragment;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;

    private UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_posts);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

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

        mUserWhere = getIntent().getExtras().getString(EXTRA_USER_WHERE);
        if(mUserWhere.equals(PostDataSource.USER_WHERE_UPVOTED)) {
            toolbar.setTitle(R.string.upvoted);
        } else if(mUserWhere.equals(PostDataSource.USER_WHERE_DOWNVOTED)) {
            toolbar.setTitle(R.string.downvoted);
        } else if(mUserWhere.equals(PostDataSource.USER_WHERE_SAVED)) {
            toolbar.setTitle(R.string.saved);
        } else if(mUserWhere.equals(PostDataSource.USER_WHERE_HIDDEN)) {
            toolbar.setTitle(R.string.hidden);
        } else if(mUserWhere.equals(PostDataSource.USER_WHERE_GILDED)){
            if(mMenu != null) {
                mMenu.findItem(R.id.action_sort_account_posts_activity).setVisible(true);
            }
            toolbar.setTitle(R.string.gilded);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        if(savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeFragment();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_account_posts_activity, mFragment).commit();
            }
        } else {
            getCurrentAccountAndInitializeFragment();
        }

        userThingSortTypeBottomSheetFragment = new UserThingSortTypeBottomSheetFragment();
    }

    private void getCurrentAccountAndInitializeFragment() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            initializeFragment();
        }).execute();
    }

    private void initializeFragment() {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_USER);
        bundle.putString(PostFragment.EXTRA_USER_NAME, mAccountName);
        bundle.putString(PostFragment.EXTRA_USER_WHERE, mUserWhere);
        bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_NEW);
        bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
        bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_account_posts_activity, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_posts_activity, menu);
        mMenu = menu;
        if(mUserWhere != null && mUserWhere.equals(PostDataSource.USER_WHERE_GILDED)) {
            menu.findItem(R.id.action_sort_account_posts_activity).setVisible(true);
        }
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_account_posts_activity);
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
            case R.id.action_sort_account_posts_activity:
                userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
                return true;
            case R.id.action_refresh_account_posts_activity:
                if(mFragment != null) {
                    ((PostFragment) mFragment).refresh();
                }
                return true;
            case R.id.action_lazy_mode_account_posts_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_account_posts_activity);
                if(isInLazyMode) {
                    ((FragmentCommunicator) mFragment).stopLazyMode();
                    isInLazyMode = false;
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    ((FragmentCommunicator) mFragment).startLazyMode();
                    isInLazyMode = true;
                    lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                    collapsingToolbarLayout.setLayoutParams(params);
                }
                return true;
            case android.R.id.home:
                finish();
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
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
    }

    @Override
    public void userThingSortTypeSelected(String sortType) {
        if(mFragment != null) {
            ((PostFragment) mFragment).changeSortType(sortType);
        }
    }
}
