package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;
import javax.inject.Named;

import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserRoomDatabase;
import User.UserDao;
import User.UserData;
import User.UserRoomDatabase;
import User.UserViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class ViewUserDetailActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME_KEY = "EUNK";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";
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

    private Fragment mFragment;
    private SubscribedUserDao subscribedUserDao;
    private RequestManager glide;
    private UserViewModel userViewModel;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;

    private String userName;
    private boolean subscriptionReady = false;
    private boolean isInLazyMode = false;
    private int colorPrimary;
    private int white;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_detail);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        //Get status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        userName = getIntent().getExtras().getString(EXTRA_USER_NAME_KEY);
        String title = "u/" + userName;
        userNameTextView.setText(title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params.topMargin = statusBarHeight;

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        colorPrimary = getResources().getColor(R.color.colorPrimary);
        white = getResources().getColor(android.R.color.white);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            void onStateChanged(AppBarLayout appBarLayout, State state) {
                if(state == State.EXPANDED) {
                    tabLayout.setTabTextColors(colorPrimary, colorPrimary);
                    tabLayout.setSelectedTabIndicatorColor(colorPrimary);
                    tabLayout.setBackgroundColor(white);
                } else if(state == State.COLLAPSED) {
                    tabLayout.setTabTextColors(white, white);
                    tabLayout.setSelectedTabIndicatorColor(white);
                    tabLayout.setBackgroundColor(colorPrimary);
                }
            }
        });

        subscribedUserDao = SubscribedUserRoomDatabase.getDatabase(this).subscribedUserDao();
        glide = Glide.with(this);

        userViewModel = ViewModelProviders.of(this, new UserViewModel.Factory(getApplication(), userName))
                .get(UserViewModel.class);
        userViewModel.getUserLiveData().observe(this, userData -> {
            if(userData != null) {
                if(userData.getBanner().equals("")) {
                    bannerImageView.setOnClickListener(view -> {
                        //Do nothing since the user has no banner image
                    });
                } else {
                    glide.load(userData.getBanner()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, userData.getBanner());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, userName + "-banner");
                        startActivity(intent);
                    });
                }

                if(userData.getIconUrl().equals("")) {
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
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, userName + "-icon");
                        startActivity(intent);
                    });
                }

                if(userData.isCanBeFollowed()) {
                    subscribeUserChip.setVisibility(View.VISIBLE);
                    subscribeUserChip.setOnClickListener(view -> {
                        if(subscriptionReady) {
                            subscriptionReady = false;
                            if(subscribeUserChip.getText().equals(getResources().getString(R.string.follow))) {
                                UserFollowing.followUser(mOauthRetrofit, mRetrofit, sharedPreferences,
                                        userName, subscribedUserDao, new UserFollowing.UserFollowingListener() {
                                            @Override
                                            public void onUserFollowingSuccess() {
                                                subscribeUserChip.setText(R.string.unfollow);
                                                subscribeUserChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
                                                makeSnackbar(R.string.followed);
                                                subscriptionReady = true;
                                            }

                                            @Override
                                            public void onUserFollowingFail() {
                                                makeSnackbar(R.string.follow_failed);
                                                subscriptionReady = true;
                                            }
                                        });
                            } else {
                                UserFollowing.unfollowUser(mOauthRetrofit, mRetrofit, sharedPreferences,
                                        userName, subscribedUserDao, new UserFollowing.UserFollowingListener() {
                                            @Override
                                            public void onUserFollowingSuccess() {
                                                subscribeUserChip.setText(R.string.follow);
                                                subscribeUserChip.setChipBackgroundColor(getResources().getColorStateList(R.color.backgroundColorPrimaryDark));
                                                makeSnackbar(R.string.unfollowed);
                                                subscriptionReady = true;
                                            }

                                            @Override
                                            public void onUserFollowingFail() {
                                                makeSnackbar(R.string.unfollow_failed);
                                                subscriptionReady = true;
                                            }
                                        });
                            }
                        }
                    });

                    new CheckIsFollowingUserAsyncTask(subscribedUserDao, userName, new CheckIsFollowingUserAsyncTask.CheckIsFollowingUserListener() {
                        @Override
                        public void isSubscribed() {
                            subscribeUserChip.setText(R.string.unfollow);
                            subscribeUserChip.setChipBackgroundColor(getResources().getColorStateList(R.color.colorAccent));
                            subscriptionReady = true;
                        }

                        @Override
                        public void isNotSubscribed() {
                            subscribeUserChip.setText(R.string.follow);
                            subscribeUserChip.setChipBackgroundColor(getResources().getColorStateList(R.color.backgroundColorPrimaryDark));
                            subscriptionReady = true;
                        }
                    }).execute();
                } else {
                    subscribeUserChip.setVisibility(View.GONE);
                }

                String userFullName = "u/" + userData.getName();
                userNameTextView.setText(userFullName);
                if(!title.equals(userFullName)) {
                    getSupportActionBar().setTitle(userFullName);
                }
                String karma = getString(R.string.karma_info, userData.getKarma());
                karmaTextView.setText(karma);
            }
        });

        FetchUserData.fetchUserData(mRetrofit, userName, new FetchUserData.FetchUserDataListener() {
            @Override
            public void onFetchUserDataSuccess(UserData userData) {
                new InsertUserDataAsyncTask(UserRoomDatabase.getDatabase(ViewUserDetailActivity.this).userDao(), userData).execute();
            }

            @Override
            public void onFetchUserDataFailed() {
                makeSnackbar(R.string.cannot_fetch_user_info);
            }
        });

        if(savedInstanceState == null) {
            /*mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_SUBREDDIT_NAME, userName);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_USER);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_user_detail_activity, mFragment).commit();*/
        } else {
            /*mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
            if(mFragment == null) {
                mFragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_SUBREDDIT_NAME, userName);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_USER);
                mFragment.setArguments(bundle);
            }*/
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            //getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_user_detail_activity, mFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_user_detail_activity, menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_user_detail_activity);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search_view_user_detail_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, userName);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_IS_USER, true);
                startActivity(intent);
                break;
            case R.id.action_refresh_view_user_detail_activity:
                if (mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                    return true;
                }
                break;
            case R.id.action_lazy_mode_view_user_detail_activity:
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_user_detail_activity);
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
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        //getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
    }

    private void makeSnackbar(int resId) {
        Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
    }

    public abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
        // State
        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        /**
         * Notifies on state change
         * @param appBarLayout Layout
         * @param state Collapse state
         */
        abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }

    private static class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private UserDao userDao;
        private UserData subredditData;

        InsertUserDataAsyncTask(UserDao userDao, UserData userData) {
            this.userDao = userDao;
            this.subredditData = userData;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            userDao.insert(subredditData);
            return null;
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
                bundle.putString(PostFragment.EXTRA_SUBREDDIT_NAME, userName);
                fragment.setArguments(bundle);
                return fragment;
            }
            CommentsListingFragment fragment = new CommentsListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CommentsListingFragment.EXTRA_USERNAME_KEY, userName);
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
                    break;
            }
            return fragment;
        }

        public void refresh() {
            if(postFragment != null) {
                ((FragmentCommunicator) postFragment).refresh();
            }
            if(commentsListingFragment != null) {
                ((FragmentCommunicator) commentsListingFragment).refresh();
            }
        }
    }
}
