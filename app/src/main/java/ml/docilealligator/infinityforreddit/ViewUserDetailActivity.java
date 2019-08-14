package ml.docilealligator.infinityforreddit;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;
import javax.inject.Named;

import SubscribedUserDatabase.SubscribedUserDao;
import User.UserDao;
import User.UserData;
import User.UserViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class ViewUserDetailActivity extends AppCompatActivity implements UserThingSortTypeBottomSheetFragment.UserThingSortTypeSelectionCallback {

    public static final String EXTRA_USER_NAME_KEY = "EUNK";

    private static final String FETCH_USER_INFO_STATE = "FSIS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";

    @BindView(R.id.coordinator_layout_view_user_detail_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_view_user_detail_activity) ViewPager viewPager;
    @BindView(R.id.appbar_layout_view_user_detail) AppBarLayout appBarLayout;
    @BindView(R.id.tab_layout_view_user_detail_activity) TabLayout tabLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_user_detail_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.banner_image_view_view_user_detail_activity) GifImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_user_detail_activity) GifImageView iconGifImageView;
    @BindView(R.id.user_name_text_view_view_user_detail_activity) TextView userNameTextView;
    @BindView(R.id.subscribe_user_chip_view_user_detail_activity) Chip subscribeUserChip;
    @BindView(R.id.karma_text_view_view_user_detail_activity) TextView karmaTextView;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private SubscribedUserDao subscribedUserDao;
    private RequestManager glide;
    private UserViewModel userViewModel;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;

    private UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String username;
    private boolean subscriptionReady = false;
    private boolean mFetchUserInfoSuccess = false;
    private boolean isInLazyMode = false;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private boolean showToast = false;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_detail);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        username = getIntent().getExtras().getString(EXTRA_USER_NAME_KEY);

        if (savedInstanceState == null) {
            getCurrentAccountAndInitializeViewPager();
        } else {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeViewPager();
            } else {
                initializeViewPager();
            }
        }

        fetchUserInfo();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        Resources resources = getResources();

        //Get status bar height
        int statusBarHeight = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        }

        String title = "u/" + username;
        userNameTextView.setText(title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ViewGroup.MarginLayoutParams toolbarLayoutParams = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        toolbarLayoutParams.topMargin = statusBarHeight;

        expandedTabTextColor = resources.getColor(R.color.tabLayoutWithExpandedCollapsingToolbarTextColor);
        expandedTabBackgroundColor = resources.getColor(R.color.tabLayoutWithExpandedCollapsingToolbarTabBackground);
        expandedTabIndicatorColor = resources.getColor(R.color.tabLayoutWithExpandedCollapsingToolbarTabIndicator);

        collapsedTabTextColor = resources.getColor(R.color.tabLayoutWithCollapsedCollapsingToolbarTextColor);
        collapsedTabBackgroundColor = resources.getColor(R.color.tabLayoutWithCollapsedCollapsingToolbarTabBackground);
        collapsedTabIndicatorColor = resources.getColor(R.color.tabLayoutWithCollapsedCollapsingToolbarTabIndicator);

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
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                        tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    } else if (state == State.EXPANDED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                        tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    }
                }
            });

            int statusBarResourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResourceId > 0) {
                int navBarHeight = resources.getDimensionPixelSize(statusBarResourceId);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.topMargin = navBarHeight;
                toolbar.setLayoutParams(params);

                showToast = true;
            }
        } else {
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                void onStateChanged(AppBarLayout appBarLayout, State state) {
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

        subscribedUserDao = mRedditDataRoomDatabase.subscribedUserDao();
        glide = Glide.with(this);

        userViewModel = ViewModelProviders.of(this, new UserViewModel.Factory(getApplication(), mRedditDataRoomDatabase, username))
                .get(UserViewModel.class);
        userViewModel.getUserLiveData().observe(this, userData -> {
            if (userData != null) {
                if (userData.getBanner().equals("")) {
                    bannerImageView.setOnClickListener(view -> {
                        //Do nothing since the user has no banner image
                    });
                } else {
                    glide.load(userData.getBanner()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, userData.getBanner());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, username + "-banner");
                        startActivity(intent);
                    });
                }

                if (userData.getIconUrl().equals("")) {
                    glide.load(getDrawable(R.drawable.subreddit_default_icon))
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing since the user has no icon image
                    });
                } else {
                    glide.load(userData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0))))
                            .into(iconGifImageView);

                    iconGifImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, userData.getIconUrl());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, username + "-icon");
                        startActivity(intent);
                    });
                }

                if (userData.isCanBeFollowed()) {
                    subscribeUserChip.setVisibility(View.VISIBLE);
                    subscribeUserChip.setOnClickListener(view -> {
                        if (subscriptionReady) {
                            subscriptionReady = false;
                            if (subscribeUserChip.getText().equals(resources.getString(R.string.follow))) {
                                UserFollowing.followUser(mOauthRetrofit, mRetrofit, mAccessToken,
                                        username, mAccountName, subscribedUserDao, new UserFollowing.UserFollowingListener() {
                                            @Override
                                            public void onUserFollowingSuccess() {
                                                subscribeUserChip.setText(R.string.unfollow);
                                                subscribeUserChip.setChipBackgroundColor(resources.getColorStateList(R.color.colorAccent));
                                                makeSnackbar(R.string.followed, false);
                                                subscriptionReady = true;
                                            }

                                            @Override
                                            public void onUserFollowingFail() {
                                                makeSnackbar(R.string.follow_failed, false);
                                                subscriptionReady = true;
                                            }
                                        });
                            } else {
                                UserFollowing.unfollowUser(mOauthRetrofit, mRetrofit, mAccessToken,
                                        username, mAccountName, subscribedUserDao, new UserFollowing.UserFollowingListener() {
                                            @Override
                                            public void onUserFollowingSuccess() {
                                                subscribeUserChip.setText(R.string.follow);
                                                subscribeUserChip.setChipBackgroundColor(resources.getColorStateList(R.color.backgroundColorPrimaryDark));
                                                makeSnackbar(R.string.unfollowed, false);
                                                subscriptionReady = true;
                                            }

                                            @Override
                                            public void onUserFollowingFail() {
                                                makeSnackbar(R.string.unfollow_failed, false);
                                                subscriptionReady = true;
                                            }
                                        });
                            }
                        }
                    });

                    new CheckIsFollowingUserAsyncTask(subscribedUserDao, username, mAccountName, new CheckIsFollowingUserAsyncTask.CheckIsFollowingUserListener() {
                        @Override
                        public void isSubscribed() {
                            subscribeUserChip.setText(R.string.unfollow);
                            subscribeUserChip.setChipBackgroundColor(resources.getColorStateList(R.color.colorAccent));
                            subscriptionReady = true;
                        }

                        @Override
                        public void isNotSubscribed() {
                            subscribeUserChip.setText(R.string.follow);
                            subscribeUserChip.setChipBackgroundColor(resources.getColorStateList(R.color.backgroundColorPrimaryDark));
                            subscriptionReady = true;
                        }
                    }).execute();
                } else {
                    subscribeUserChip.setVisibility(View.GONE);
                }

                String userFullName = "u/" + userData.getName();
                userNameTextView.setText(userFullName);
                if (!title.equals(userFullName)) {
                    getSupportActionBar().setTitle(userFullName);
                }
                String karma = getString(R.string.karma_info, userData.getKarma());
                karmaTextView.setText(karma);
            }
        });

        userThingSortTypeBottomSheetFragment = new UserThingSortTypeBottomSheetFragment();
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
        tabLayout.setupWithViewPager(viewPager);
    }

    private void fetchUserInfo() {
        if (!mFetchUserInfoSuccess) {
            FetchUserData.fetchUserData(mRetrofit, username, new FetchUserData.FetchUserDataListener() {
                @Override
                public void onFetchUserDataSuccess(UserData userData) {
                    new InsertUserDataAsyncTask(mRedditDataRoomDatabase.userDao(), userData,
                            () -> mFetchUserInfoSuccess = true).execute();
                }

                @Override
                public void onFetchUserDataFailed() {
                    makeSnackbar(R.string.cannot_fetch_user_info, true);
                    mFetchUserInfoSuccess = false;
                }
            });
        }
    }

    void deleteComment(String fullName) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete_this_comment)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteThing.delete(mOauthRetrofit, fullName, mAccessToken, new DeleteThing.DeleteThingListener() {
                    @Override
                    public void deleteSuccess() {
                        Toast.makeText(ViewUserDetailActivity.this, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deleteFailed() {
                        Toast.makeText(ViewUserDetailActivity.this, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_user_detail_activity, menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_user_detail_activity);
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
            case R.id.action_sort_view_user_detail_activity:
                userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
                return true;
            case R.id.action_search_view_user_detail_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, username);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_IS_USER, true);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, false);
                startActivity(intent);
                return true;
            case R.id.action_refresh_view_user_detail_activity:
                sectionsPagerAdapter.refresh();
                return true;
            case R.id.action_lazy_mode_view_user_detail_activity:
                /*MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_user_detail_activity);
                if(isInLazyMode) {
                    isInLazyMode = false;
                    ((FragmentCommunicator) mFragment).stopLazyMode();
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    isInLazyMode = true;
                    ((FragmentCommunicator) mFragment).startLazyMode();
                    lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                    appBarLayout.setExpanded(false);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                    collapsingToolbarLayout.setLayoutParams(params);
                }
                return true;*/
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
    }

    private void makeSnackbar(int resId, boolean retry) {
        if(showToast) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        } else {
            if (retry) {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).setAction(R.string.retry,
                        view -> fetchUserInfo()).show();
            } else {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void userThingSortTypeSelected(String sortType) {
        sectionsPagerAdapter.changeSortType(sortType);
    }

    private static class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

        interface InsertUserDataAsyncTaskListener {
            void insertSuccess();
        }

        private UserDao userDao;
        private UserData subredditData;
        private InsertUserDataAsyncTaskListener insertUserDataAsyncTaskListener;

        InsertUserDataAsyncTask(UserDao userDao, UserData userData,
                                InsertUserDataAsyncTaskListener insertUserDataAsyncTaskListener) {
            this.userDao = userDao;
            this.subredditData = userData;
            this.insertUserDataAsyncTaskListener = insertUserDataAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            userDao.insert(subredditData);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            insertUserDataAsyncTaskListener.insertSuccess();
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PostFragment postFragment;
        private CommentsListingFragment commentsListingFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, username);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostDataSource.USER_WHERE_SUBMITTED);
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_NEW);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            }
            CommentsListingFragment fragment = new CommentsListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CommentsListingFragment.EXTRA_USERNAME_KEY, username);
            bundle.putString(CommentsListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            bundle.putString(CommentsListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
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
                    return "Comments";
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
                    commentsListingFragment = (CommentsListingFragment) fragment;
            }
            return fragment;
        }

        public void refresh() {
            if (viewPager.getCurrentItem() == 0) {
                if(postFragment != null) {
                    postFragment.refresh();
                }
            } else {
                if(commentsListingFragment != null) {
                    commentsListingFragment.refresh();
                }
            }
        }

        public void changeSortType(String sortType) {
            if(viewPager.getCurrentItem() == 0) {
                if(postFragment != null) {
                    postFragment.changeSortType(sortType);
                }
            } else {
                if(commentsListingFragment != null) {
                    commentsListingFragment.changeSortType(sortType);
                }
            }
        }
    }
}
