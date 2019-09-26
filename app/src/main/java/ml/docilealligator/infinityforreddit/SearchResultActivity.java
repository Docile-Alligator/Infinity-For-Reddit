package ml.docilealligator.infinityforreddit;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class SearchResultActivity extends AppCompatActivity implements SearchPostSortTypeBottomSheetFragment.SearchSortTypeSelectionCallback,
        SearchUserAndSubredditSortTypeBottomSheetFragment.SearchUserAndSubredditSortTypeSelectionCallback {
    static final String EXTRA_QUERY = "QK";
    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String mQuery;
    private String mSubredditName;

    @BindView(R.id.appbar_layout_search_result_activity) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_search_result_activity) Toolbar toolbar;
    @BindView(R.id.tab_layout_search_result_activity) TabLayout tabLayout;
    @BindView(R.id.view_pager_search_result_activity) ViewPager viewPager;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment;
    private SearchUserAndSubredditSortTypeBottomSheetFragment searchUserAndSubredditSortTypeBottomSheetFragment;

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

        setContentView(R.layout.activity_search_result);

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
                    params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                    toolbar.setLayoutParams(params);
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

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getCurrentAccountAndInitializeViewPager();
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeViewPager();
            } else {
                initializeViewPager();
            }
        }

        searchPostSortTypeBottomSheetFragment = new SearchPostSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        searchPostSortTypeBottomSheetFragment.setArguments(bundle);

        searchUserAndSubredditSortTypeBottomSheetFragment = new SearchUserAndSubredditSortTypeBottomSheetFragment();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getStringExtra(EXTRA_QUERY);

        mSubredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);

        if(query != null) {
            mQuery = query;
            setTitle(query);
        }
    }

    private void getCurrentAccountAndInitializeViewPager() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            initializeViewPager();
        }).execute();
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_result_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sort_search_result_activity:
                switch (viewPager.getCurrentItem()) {
                    case 0: {
                        searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                        break;
                    }
                    case 1:
                    case 2:
                        Bundle bundle = new Bundle();
                        bundle.putInt(SearchUserAndSubredditSortTypeBottomSheetFragment.EXTRA_FRAGMENT_POSITION, viewPager.getCurrentItem());
                        searchUserAndSubredditSortTypeBottomSheetFragment.setArguments(bundle);
                        searchUserAndSubredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchUserAndSubredditSortTypeBottomSheetFragment.getTag());
                        break;
                }
                return true;
            case R.id.action_search_search_result_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, false);
                intent.putExtra(SearchActivity.EXTRA_QUERY, mQuery);
                finish();
                startActivity(intent);
                return true;
            case R.id.action_refresh_search_result_activity:
                sectionsPagerAdapter.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void searchSortTypeSelected(String sortType) {
        sectionsPagerAdapter.changeSortType(sortType, 0);
    }

    @Override
    public void searchUserAndSubredditSortTypeSelected(String sortType, int fragmentPosition) {
        sectionsPagerAdapter.changeSortType(sortType, fragmentPosition);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PostFragment postFragment;
        private SubredditListingFragment subredditListingFragment;
        private UserListingFragment userListingFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    PostFragment mFragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SEARCH);
                    bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_RELEVANCE);
                    bundle.putString(PostFragment.EXTRA_NAME, mSubredditName);
                    bundle.putString(PostFragment.EXTRA_QUERY, mQuery);
                    bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                    bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                case 1: {
                    SubredditListingFragment mFragment = new SubredditListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(SubredditListingFragment.EXTRA_QUERY, mQuery);
                    bundle.putBoolean(SubredditListingFragment.EXTRA_IS_POSTING, false);
                    bundle.putString(SubredditListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(SubredditListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                default: {
                    UserListingFragment mFragment = new UserListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(UserListingFragment.EXTRA_QUERY, mQuery);
                    bundle.putString(UserListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(UserListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Posts";
                case 1:
                    return "Subreddits";
                case 2:
                    return "Users";
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
                    break;
                case 1:
                    subredditListingFragment = (SubredditListingFragment) fragment;
                    break;
                case 2:
                    userListingFragment = (UserListingFragment) fragment;
                    break;
            }
            return fragment;
        }

        void changeSortType(String sortType, int fragmentPosition) {
            switch (fragmentPosition) {
                case 0:
                    postFragment.changeSortType(sortType);
                    break;
                case 1:
                    subredditListingFragment.changeSortType(sortType);
                    break;
                case 2:
                    userListingFragment.changeSortType(sortType);
            }
        }

        public void refresh() {
            switch (viewPager.getCurrentItem()) {
                case 0:
                    ((FragmentCommunicator) postFragment).refresh();
                    break;
                case 1:
                    ((FragmentCommunicator) subredditListingFragment).refresh();
                    break;
                case 2:
                    ((FragmentCommunicator) userListingFragment).refresh();
                    break;
            }
        }

        public void changeNSFW(boolean nsfw) {
            if(postFragment != null) {
                postFragment.changeNSFW(nsfw);
            }
        }
    }
}
