package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SearchPostSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SearchUserAndSubredditSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.SubredditListingFragment;
import ml.docilealligator.infinityforreddit.Fragment.UserListingFragment;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;

public class SearchResultActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface {
    static final String EXTRA_QUERY = "QK";
    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    @BindView(R.id.coordinator_layout_search_result_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_search_result_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_search_result_activity)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_search_result_activity)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_search_result_activity)
    ViewPager viewPager;
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
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String mQuery;
    private String mSubredditName;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment;
    private SortTimeBottomSheetFragment sortTimeBottomSheetFragment;
    private SearchUserAndSubredditSortTypeBottomSheetFragment searchUserAndSubredditSortTypeBottomSheetFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_result);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(toolbar);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        if (savedInstanceState == null) {
            getCurrentAccountAndInitializeViewPager();
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeViewPager();
            } else {
                initializeViewPager();
            }
        }

        searchPostSortTypeBottomSheetFragment = new SearchPostSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        searchPostSortTypeBottomSheetFragment.setArguments(bundle);

        sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();

        searchUserAndSubredditSortTypeBottomSheetFragment = new SearchUserAndSubredditSortTypeBottomSheetFragment();

        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getStringExtra(EXTRA_QUERY);

        mSubredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);

        if (query != null) {
            mQuery = query;
            setTitle(query);
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
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
    }

    private void getCurrentAccountAndInitializeViewPager() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                sectionsPagerAdapter.displaySortTypeInToolbar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_result_activity, menu);
        applyMenuItemTheme(menu);
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
                if (mSubredditName != null && !mSubredditName.equals("")) {
                    intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, mSubredditName);
                }
                intent.putExtra(SearchActivity.EXTRA_QUERY, mQuery);
                finish();
                startActivity(intent);
                return true;
            case R.id.action_refresh_search_result_activity:
                sectionsPagerAdapter.refresh();
                return true;
            case R.id.action_change_post_layout_search_result_activity:
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
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
    public void searchUserAndSubredditSortTypeSelected(SortType sortType, int fragmentPosition) {
        sectionsPagerAdapter.changeSortType(sortType, fragmentPosition);
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
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
            displaySortTypeInToolbar();
            return fragment;
        }

        public boolean handleKeyDown(int keyCode) {
            return viewPager.getCurrentItem() == 0 && postFragment.handleKeyDown(keyCode);
        }

        void changeSortType(SortType sortType) {
            mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_POST, sortType.getType().name()).apply();
            if(sortType.getTime() != null) {
                mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_SEARCH_POST, sortType.getTime().name()).apply();
            }

            postFragment.changeSortType(sortType);
            displaySortTypeInToolbar();
        }

        void changeSortType(SortType sortType, int fragmentPosition) {
            switch (fragmentPosition) {
                case 1:
                    mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT, sortType.getType().name()).apply();
                    subredditListingFragment.changeSortType(sortType);
                    break;
                case 2:
                    mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_SEARCH_USER, sortType.getType().name()).apply();
                    userListingFragment.changeSortType(sortType);
            }
            displaySortTypeInToolbar();
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

        void changeNSFW(boolean nsfw) {
            if (postFragment != null) {
                postFragment.changeNSFW(nsfw);
            }
        }

        void changePostLayout(int postLayout) {
            if (postFragment != null) {
                mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST, postLayout).apply();
                ((FragmentCommunicator) postFragment).changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            if (viewPager.getCurrentItem() == 0) {
                postFragment.goBackToTop();
            } else if (viewPager.getCurrentItem() == 1) {
                subredditListingFragment.goBackToTop();
            } else {
                userListingFragment.goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            switch (viewPager.getCurrentItem()) {
                case 0:
                    if (postFragment != null) {
                        SortType sortType = postFragment.getSortType();
                        Utils.displaySortTypeInToolbar(sortType, toolbar);
                    }
                    break;
                case 1:
                    if (subredditListingFragment != null) {
                        SortType sortType = subredditListingFragment.getSortType();
                        Utils.displaySortTypeInToolbar(sortType, toolbar);
                    }
                    break;
                case 2:
                    if (userListingFragment != null) {
                        SortType sortType = userListingFragment.getSortType();
                        Utils.displaySortTypeInToolbar(sortType, toolbar);
                    }
                    break;
            }
        }
    }
}
