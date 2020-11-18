package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.asynctasks.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultiRedditAsyncTask;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThingsAsyncTask;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.events.RefreshMultiRedditsEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.fragments.FollowedUsersListingFragment;
import ml.docilealligator.infinityforreddit.fragments.MultiRedditListingFragment;
import ml.docilealligator.infinityforreddit.fragments.SubscribedSubredditsListingFragment;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.multireddit.DeleteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.FetchMyMultiReddits;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SubscribedThingListingActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SHOW_MULTIREDDITS = "ESM";
    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";
    private static final String INSERT_MULTIREDDIT_STATE = "IMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";

    @BindView(R.id.coordinator_layout_subscribed_thing_listing_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_subscribed_thing_listing_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_subscribed_thing_listing_activity)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_subscribed_thing_listing_activity)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_subscribed_thing_listing_activity)
    ViewPager viewPager;
    @BindView(R.id.fab_subscribed_thing_listing_activity)
    public FloatingActionButton fab;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private SlidrInterface mSlidrInterface;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private boolean mInsertSuccess = false;
    private boolean mInsertMultiredditSuccess = false;
    private boolean showMultiReddits = false;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subscribed_thing_listing);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin = navBarHeight;
                    fab.setLayoutParams(params);
                }
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        if (savedInstanceState != null) {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
            mInsertMultiredditSuccess = savedInstanceState.getBoolean(INSERT_MULTIREDDIT_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndInitializeViewPager();
            } else {
                initializeViewPagerAndLoadSubscriptions();
            }
        } else {
            showMultiReddits = getIntent().getBooleanExtra(EXTRA_SHOW_MULTIREDDITS, false);
            getCurrentAccountAndInitializeViewPager();
        }
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
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
    }

    private void getCurrentAccountAndInitializeViewPager() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            initializeViewPagerAndLoadSubscriptions();
        }).execute();
    }

    private void initializeViewPagerAndLoadSubscriptions() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateMultiRedditActivity.class);
            startActivity(intent);
        });
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                    fab.hide();
                } else {
                    lockSwipeRightToGoBack();
                    if (position == 1) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                }
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        if (showMultiReddits) {
            viewPager.setCurrentItem(2);
        }

        loadSubscriptions(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
        outState.putBoolean(INSERT_MULTIREDDIT_STATE, mInsertMultiredditSuccess);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void loadSubscriptions(boolean forceLoad) {
        if (!(!forceLoad && mInsertSuccess)) {
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
                                    () -> {
                                        mInsertSuccess = true;
                                        sectionsPagerAdapter.stopRefreshProgressbar();
                                    }).execute();
                        }

                        @Override
                        public void onFetchSubscribedThingFail() {
                            mInsertSuccess = false;
                            sectionsPagerAdapter.stopRefreshProgressbar();
                            Toast.makeText(SubscribedThingListingActivity.this,
                                    R.string.error_loading_subscriptions, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if (!(!forceLoad && mInsertMultiredditSuccess)) {
            loadMultiReddits();
        }
    }

    private void loadMultiReddits() {
        FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, mAccessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
            @Override
            public void success(ArrayList<MultiReddit> multiReddits) {
                new InsertMultiRedditAsyncTask(mRedditDataRoomDatabase, multiReddits, mAccountName, () -> {
                    mInsertMultiredditSuccess = true;
                    sectionsPagerAdapter.stopMultiRedditRefreshProgressbar();
                }).execute();
            }

            @Override
            public void failed() {
                mInsertMultiredditSuccess = false;
                sectionsPagerAdapter.stopMultiRedditRefreshProgressbar();
                Toast.makeText(SubscribedThingListingActivity.this, R.string.error_loading_multi_reddit_list, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMultiReddit(MultiReddit multiReddit) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_multi_reddit_dialog_message)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteMultiReddit.deleteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase,
                        mAccessToken, mAccountName, multiReddit.getPath(), new DeleteMultiReddit.DeleteMultiRedditListener() {
                            @Override
                            public void success() {
                                Toast.makeText(SubscribedThingListingActivity.this,
                                        R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                loadMultiReddits();
                            }

                            @Override
                            public void failed() {
                                Toast.makeText(SubscribedThingListingActivity.this,
                                        R.string.delete_multi_reddit_failed, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void goBackToMainPageEvent(GoBackToMainPageEvent event) {
        finish();
    }

    @Subscribe
    public void onRefreshMultiRedditsEvent(RefreshMultiRedditsEvent event) {
        loadMultiReddits();
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
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

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SubscribedSubredditsListingFragment subscribedSubredditsListingFragment;
        private FollowedUsersListingFragment followedUsersListingFragment;
        private MultiRedditListingFragment multiRedditListingFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    SubscribedSubredditsListingFragment fragment = new SubscribedSubredditsListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(SubscribedSubredditsListingFragment.EXTRA_IS_SUBREDDIT_SELECTION, false);
                    bundle.putString(SubscribedSubredditsListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                case 1: {
                    FollowedUsersListingFragment fragment = new FollowedUsersListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(FollowedUsersListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    bundle.putString(FollowedUsersListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                default: {
                    MultiRedditListingFragment fragment = new MultiRedditListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(MultiRedditListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(MultiRedditListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                    fragment.setArguments(bundle);
                    return fragment;
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
                    return getString(R.string.subreddits);
                case 1:
                    return getString(R.string.users);
                case 2:
                    return getString(R.string.multi_reddits);
            }

            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (position == 0) {
                subscribedSubredditsListingFragment = (SubscribedSubredditsListingFragment) fragment;
            } else if (position == 1) {
                followedUsersListingFragment = (FollowedUsersListingFragment) fragment;
            } else {
                multiRedditListingFragment = (MultiRedditListingFragment) fragment;
            }

            return fragment;
        }

        void stopRefreshProgressbar() {
            if (subscribedSubredditsListingFragment != null) {
                ((FragmentCommunicator) subscribedSubredditsListingFragment).stopRefreshProgressbar();
            }
            if (followedUsersListingFragment != null) {
                ((FragmentCommunicator) followedUsersListingFragment).stopRefreshProgressbar();
            }
        }

        void stopMultiRedditRefreshProgressbar() {
            if (multiRedditListingFragment != null) {
                ((FragmentCommunicator) multiRedditListingFragment).stopRefreshProgressbar();
            }
        }

        void goBackToTop() {
            if (viewPager.getCurrentItem() == 0) {
                subscribedSubredditsListingFragment.goBackToTop();
            } else if (viewPager.getCurrentItem() == 1) {
                followedUsersListingFragment.goBackToTop();
            } else {
                multiRedditListingFragment.goBackToTop();
            }
        }
    }
}
