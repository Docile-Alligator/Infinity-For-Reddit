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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.FetchSubscribedThing;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.asynctasks.DeleteMultiredditInDatabase;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThings;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySubscribedThingListingBinding;
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
    private boolean mInsertSuccess = false;
    private boolean mInsertMultiredditSuccess = false;
    private boolean showMultiReddits = false;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private Menu mMenu;
    private ActivitySubscribedThingListingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivitySubscribedThingListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSubscribedThingListingActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarSubscribedThingListingActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.fabSubscribedThingListingActivity.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    binding.fabSubscribedThingListingActivity.setLayoutParams(params);
                }
            }
        }

        setSupportActionBar(binding.toolbarSubscribedThingListingActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarSubscribedThingListingActivity);

        if (savedInstanceState != null) {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
            mInsertMultiredditSuccess = savedInstanceState.getBoolean(INSERT_MULTIREDDIT_STATE);
        } else {
            showMultiReddits = getIntent().getBooleanExtra(EXTRA_SHOW_MULTIREDDITS, false);
        }

        if (accountName.equals(Account.ANONYMOUS_ACCOUNT) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.searchEditTextSubscribedThingListingActivity.setImeOptions(binding.searchEditTextSubscribedThingListingActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        binding.searchEditTextSubscribedThingListingActivity.addTextChangedListener(new TextWatcher() {
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
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSubscribedThingListingActivity,
                binding.collapsingToolbarLayoutSubscribedThingListingActivity, binding.toolbarSubscribedThingListingActivity);
        applyTabLayoutTheme(binding.tabLayoutSubscribedThingListingActivity);
        applyFABTheme(binding.fabSubscribedThingListingActivity);
        binding.searchEditTextSubscribedThingListingActivity.setTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.searchEditTextSubscribedThingListingActivity.setHintTextColor(mCustomThemeWrapper.getToolbarSecondaryTextColor());
    }

    private void initializeViewPagerAndLoadSubscriptions() {
        binding.fabSubscribedThingListingActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateMultiRedditActivity.class);
            startActivity(intent);
        });
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        binding.viewPagerSubscribedThingListingActivity.setAdapter(sectionsPagerAdapter);
        binding.viewPagerSubscribedThingListingActivity.setOffscreenPageLimit(3);
        if (binding.viewPagerSubscribedThingListingActivity.getCurrentItem() != 2) {
            binding.fabSubscribedThingListingActivity.hide();
        }
        binding.viewPagerSubscribedThingListingActivity.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                    binding.fabSubscribedThingListingActivity.hide();
                } else {
                    lockSwipeRightToGoBack();
                    if (position != 2) {
                        binding.fabSubscribedThingListingActivity.hide();
                    } else {
                        binding.fabSubscribedThingListingActivity.show();
                    }
                }
            }
        });
        binding.tabLayoutSubscribedThingListingActivity.setupWithViewPager(binding.viewPagerSubscribedThingListingActivity);

        if (showMultiReddits) {
            binding.viewPagerSubscribedThingListingActivity.setCurrentItem(2, false);
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
            binding.searchEditTextSubscribedThingListingActivity.setVisibility(View.VISIBLE);
            binding.searchEditTextSubscribedThingListingActivity.requestFocus();
            if (binding.searchEditTextSubscribedThingListingActivity.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.searchEditTextSubscribedThingListingActivity, InputMethodManager.SHOW_IMPLICIT);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (binding.searchEditTextSubscribedThingListingActivity.getVisibility() == View.VISIBLE) {
                Utils.hideKeyboard(this);
                binding.searchEditTextSubscribedThingListingActivity.setVisibility(View.GONE);
                binding.searchEditTextSubscribedThingListingActivity.setText("");
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
        if (binding.searchEditTextSubscribedThingListingActivity.getVisibility() == View.VISIBLE) {
            Utils.hideKeyboard(this);
            binding.searchEditTextSubscribedThingListingActivity.setVisibility(View.GONE);
            binding.searchEditTextSubscribedThingListingActivity.setText("");
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
        if (!forceLoad && System.currentTimeMillis() - mCurrentAccountSharedPreferences.getLong(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME, 0L) < 24 * 60 * 60 * 1000) {
            return;
        }

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT) && !(!forceLoad && mInsertSuccess)) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, accessToken, accountName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            mCurrentAccountSharedPreferences.edit().putLong(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME, System.currentTimeMillis()).apply();
                            InsertSubscribedThings.insertSubscribedThings(
                                    mExecutor,
                                    new Handler(),
                                    mRedditDataRoomDatabase,
                                    accountName,
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
        if (binding.viewPagerSubscribedThingListingActivity.getCurrentItem() == 2) {
            binding.fabSubscribedThingListingActivity.show();
        }
    }

    public void hideFabInMultiredditTab() {
        if (binding.viewPagerSubscribedThingListingActivity.getCurrentItem() == 2) {
            binding.fabSubscribedThingListingActivity.hide();
        }
    }

    private void loadMultiReddits() {
        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, accessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
                @Override
                public void success(ArrayList<MultiReddit> multiReddits) {
                    InsertMultireddit.insertMultireddits(mExecutor, new Handler(), mRedditDataRoomDatabase, multiReddits, accountName, () -> {
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
                    if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                        DeleteMultiredditInDatabase.deleteMultiredditInDatabase(mExecutor, new Handler(), mRedditDataRoomDatabase, accountName, multiReddit.getPath(),
                                () -> Toast.makeText(SubscribedThingListingActivity.this,
                                        R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show());
                    } else {
                        DeleteMultiReddit.deleteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                accessToken, accountName, multiReddit.getPath(), new DeleteMultiReddit.DeleteMultiRedditListener() {
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

    @Override
    public void lockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.lock();
        }
    }

    @Override
    public void unlockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.unlock();
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
                    fragment.setArguments(bundle);
                    return fragment;
                }
                case 1: {
                    FollowedUsersListingFragment fragment = new FollowedUsersListingFragment();
                    Bundle bundle = new Bundle();
                    fragment.setArguments(bundle);
                    return fragment;
                }
                default: {
                    MultiRedditListingFragment fragment = new MultiRedditListingFragment();
                    Bundle bundle = new Bundle();
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
            if (binding.viewPagerSubscribedThingListingActivity.getCurrentItem() == 0) {
                subscribedSubredditsListingFragment.goBackToTop();
            } else if (binding.viewPagerSubscribedThingListingActivity.getCurrentItem() == 1) {
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
