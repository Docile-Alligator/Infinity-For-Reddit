package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.asynctasks.DeleteMultiredditInDatabase;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThings;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.ViewPagerBugFixed;
import ml.docilealligator.infinityforreddit.events.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.events.RefreshMultiRedditsEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.FollowedUsersListingFragment;
import ml.docilealligator.infinityforreddit.fragments.MultiRedditListingFragment;
import ml.docilealligator.infinityforreddit.fragments.SubscribedSubredditsListingFragment;
import ml.docilealligator.infinityforreddit.multireddit.DeleteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.FetchMyMultiReddits;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class SubscribedThingListingActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SHOW_MULTIREDDITS = "ESM";
    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";
    private static final String INSERT_MULTIREDDIT_STATE = "IMS";

    @BindView(R.id.coordinator_layout_subscribed_thing_listing_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_subscribed_thing_listing_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_subscribed_thing_listing_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_subscribed_thing_listing_activity)
    Toolbar toolbar;
    @BindView(R.id.search_edit_text_subscribed_thing_listing_activity)
    EditText searchEditText;
    @BindView(R.id.tab_layout_subscribed_thing_listing_activity)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_subscribed_thing_listing_activity)
    ViewPagerBugFixed viewPager;
    @BindView(R.id.fab_subscribed_thing_listing_activity)
    FloatingActionButton fab;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private SlidrInterface mSlidrInterface;
    private String mAccessToken;
    private String mAccountName;
    private boolean mInsertSuccess = false;
    private boolean mInsertMultiredditSuccess = false;
    private boolean showMultiReddits = false;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private Menu mMenu;

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

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "-");

        if (savedInstanceState != null) {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
            mInsertMultiredditSuccess = savedInstanceState.getBoolean(INSERT_MULTIREDDIT_STATE);
        } else {
            showMultiReddits = getIntent().getBooleanExtra(EXTRA_SHOW_MULTIREDDITS, false);
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sectionsPagerAdapter.changeSearchQuery(editable.toString());
            }
        });
        initializeViewPagerAndLoadSubscriptions();
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
        searchEditText.setTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        searchEditText.setHintTextColor(mCustomThemeWrapper.getToolbarSecondaryTextColor());
    }

    private void initializeViewPagerAndLoadSubscriptions() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateMultiRedditActivity.class);
            startActivity(intent);
        });
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        if (viewPager.getCurrentItem() != 2) {
            fab.hide();
        }
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                    fab.hide();
                } else {
                    lockSwipeRightToGoBack();
                    if (position != 2) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                }
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        if (showMultiReddits) {
            viewPager.setCurrentItem(2, false);
        }

        loadSubscriptions(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subscribed_thing_listing_activity, menu);
        mMenu = menu;
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search_subscribed_thing_listing_activity) {
            item.setVisible(false);
            searchEditText.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
            if (searchEditText.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (searchEditText.getVisibility() == View.VISIBLE) {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                searchEditText.setVisibility(View.GONE);
                searchEditText.setText("");
                mMenu.findItem(R.id.action_search_subscribed_thing_listing_activity).setVisible(true);
                sectionsPagerAdapter.changeSearchQuery("");
                return true;
            }
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (searchEditText.getVisibility() == View.VISIBLE) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            searchEditText.setVisibility(View.GONE);
            searchEditText.setText("");
            mMenu.findItem(R.id.action_search_subscribed_thing_listing_activity).setVisible(true);
            sectionsPagerAdapter.changeSearchQuery("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
        outState.putBoolean(INSERT_MULTIREDDIT_STATE, mInsertMultiredditSuccess);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void loadSubscriptions(boolean forceLoad) {
        if (mAccessToken != null && !(!forceLoad && mInsertSuccess)) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, mAccessToken, mAccountName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            InsertSubscribedThings.insertSubscribedThings(
                                    mExecutor,
                                    new Handler(),
                                    mRedditDataRoomDatabase,
                                    mAccountName,
                                    subscribedSubredditData,
                                    subscribedUserData,
                                    subredditData,
                                    () -> {
                                        mInsertSuccess = true;
                                        sectionsPagerAdapter.stopRefreshProgressbar();
                                    });
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

    public void showFabInMultiredditTab() {
        if (viewPager.getCurrentItem() == 2) {
            fab.show();
        }
    }

    public void hideFabInMultiredditTab() {
        if (viewPager.getCurrentItem() == 2) {
            fab.hide();
        }
    }

    private void loadMultiReddits() {
        if (mAccessToken != null) {
            FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, mAccessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
                @Override
                public void success(ArrayList<MultiReddit> multiReddits) {
                    InsertMultireddit.insertMultireddits(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddits, mAccountName, () -> {
                        mInsertMultiredditSuccess = true;
                        sectionsPagerAdapter.stopMultiRedditRefreshProgressbar();
                    });
                }

                @Override
                public void failed() {
                    mInsertMultiredditSuccess = false;
                    sectionsPagerAdapter.stopMultiRedditRefreshProgressbar();
                    Toast.makeText(SubscribedThingListingActivity.this, R.string.error_loading_multi_reddit_list, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void deleteMultiReddit(MultiReddit multiReddit) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_multi_reddit_dialog_message)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> {
                    if (mAccessToken == null) {
                        DeleteMultiredditInDatabase.deleteMultiredditInDatabase(mExecutor, new Handler(), mRedditDataRoomDatabase, mAccountName, multiReddit.getPath(),
                                () -> Toast.makeText(SubscribedThingListingActivity.this,
                                        R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show());
                    } else {
                        DeleteMultiReddit.deleteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
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
                                });
                    }
                })
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
                    bundle.putString(SubscribedSubredditsListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                case 1: {
                    FollowedUsersListingFragment fragment = new FollowedUsersListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(FollowedUsersListingFragment.EXTRA_ACCOUNT_NAME, mAccountName == null ? "-" : mAccountName);
                    bundle.putString(FollowedUsersListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                default: {
                    MultiRedditListingFragment fragment = new MultiRedditListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(MultiRedditListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putString(MultiRedditListingFragment.EXTRA_ACCOUNT_NAME, mAccountName == null ? "-" : mAccountName);
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
                    return Utils.getTabTextWithCustomFont(typeface, getString(R.string.subreddits));
                case 1:
                    return Utils.getTabTextWithCustomFont(typeface, getString(R.string.users));
                case 2:
                    return Utils.getTabTextWithCustomFont(typeface, getString(R.string.multi_reddits));
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

        void changeSearchQuery(String searchQuery) {
            if (subscribedSubredditsListingFragment != null) {
                subscribedSubredditsListingFragment.changeSearchQuery(searchQuery);
            }
            if (followedUsersListingFragment != null) {
                followedUsersListingFragment.changeSearchQuery(searchQuery);
            }
            if (multiRedditListingFragment != null) {
                multiRedditListingFragment.changeSearchQuery(searchQuery);
            }
        }
    }
}
