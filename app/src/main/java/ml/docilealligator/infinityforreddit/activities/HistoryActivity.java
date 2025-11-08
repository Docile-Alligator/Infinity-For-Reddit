package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityHistoryBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.CommentsListingFragment;
import ml.docilealligator.infinityforreddit.fragments.HistoryPostFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class HistoryActivity extends BaseActivity implements ActivityToolbarInterface,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback {

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        mViewPager2 = binding.viewPagerHistoryActivity;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutHistoryActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                ViewGroupCompat.installCompatInsetsDispatch(binding.getRoot());
                ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = Utils.getInsets(insets, false);

                        setMargins(binding.toolbarHistoryActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        binding.viewPagerHistoryActivity.setPadding(allInsets.left, 0, allInsets.right, allInsets.bottom);

                        return insets;
                    }
                });
                //adjustToolbar(binding.toolbarHistoryActivity);
            }
        }

        setSupportActionBar(binding.toolbarHistoryActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarHistoryActivity);

        fragmentManager = getSupportFragmentManager();

        initializeViewPager();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutHistoryActivity,
                binding.collapsingToolbarLayoutHistoryActivity, binding.toolbarHistoryActivity);
        applyTabLayoutTheme(binding.tabLayoutTabLayoutHistoryActivityActivity);
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        binding.tabLayoutTabLayoutHistoryActivityActivity.setVisibility(View.GONE);
        binding.viewPagerHistoryActivity.setAdapter(sectionsPagerAdapter);
        //viewPager2.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        binding.viewPagerHistoryActivity.setUserInputEnabled(false);
        /*new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.posts));
                    break;
            }
        }).attach();*/

        binding.viewPagerHistoryActivity.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });

        fixViewPager2Sensitivity(binding.viewPagerHistoryActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh_history_activity) {
            sectionsPagerAdapter.refresh();
            return true;
        } else if (itemId == R.id.action_change_post_layout_history_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

    @Override
    public void postLayoutSelected(int postLayout) {
        if (sectionsPagerAdapter != null) {
            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.HISTORY_POST_LAYOUT_READ_POST, postLayout).apply();
            sectionsPagerAdapter.changePostLayout(postLayout);
        }
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                HistoryPostFragment fragment = new HistoryPostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(HistoryPostFragment.EXTRA_HISTORY_TYPE, HistoryPostFragment.HISTORY_TYPE_READ_POSTS);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                HistoryPostFragment fragment = new HistoryPostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(HistoryPostFragment.EXTRA_HISTORY_TYPE, HistoryPostFragment.HISTORY_TYPE_READ_POSTS);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + binding.viewPagerHistoryActivity.getCurrentItem());
        }

        public boolean handleKeyDown(int keyCode) {
            if (binding.viewPagerHistoryActivity.getCurrentItem() == 0) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof PostFragment) {
                    return ((PostFragment) fragment).handleKeyDown(keyCode);
                }
            }
            return false;
        }

        public void refresh() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).refresh();
            } else if (fragment instanceof CommentsListingFragment) {
                ((CommentsListingFragment) fragment).refresh();
            }
        }

        public void changeNSFW(boolean nsfw) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeNSFW(nsfw);
            }
        }

        public void changePostLayout(int postLayout) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof HistoryPostFragment) {
                ((HistoryPostFragment) fragment).changePostLayout(postLayout);
            }
        }


        public void goBackToTop() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).goBackToTop();
            } else if (fragment instanceof CommentsListingFragment) {
                ((CommentsListingFragment) fragment).goBackToTop();
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}