package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.LocalSave;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.LocalPostFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class LocalPostsActivity extends BaseActivity implements ActivityToolbarInterface,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback {

    @BindView(R.id.coordinator_layout_local_posts_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_local_posts_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_local_posts_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_local_posts_activity)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_tab_layout_local_posts_activity_activity)
    TabLayout tabLayout;
    @BindView(R.id.view_pager_local_posts_activity)
    ViewPager2 viewPager2;

    @BindView(R.id.local_posts_searchbar)
    SearchView searchView;
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
    private String mAccessToken;
    private String mAccountName;

    private void ChangeSort()
    {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme).setTitle("Sort");

        alert.setSingleChoiceItems(LocalSave.SortTypes, LocalSave.sortType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int sortType) {
                LocalSave.sortType = sortType;
                sectionsPagerAdapter.refresh();
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void ShowLocalSaveSettings()
    {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme).setTitle("Settings");

        View view = getLayoutInflater().inflate(R.layout.local_posts_settings, null);
        CheckBox cacheSavedCheck = view.findViewById(R.id.cache_saved_checkbox);
        CheckBox cacheHistoryCheck = view.findViewById(R.id.cache_history_checkbox);
        Button saveCachedBtn = view.findViewById(R.id.save_cached_btn);
        Button clearCachedBtn = view.findViewById(R.id.clear_cached_btn);
        Button removeAllSavedBtn = view.findViewById(R.id.remove_all_saved_btn);

        cacheSavedCheck.setChecked(LocalSave.cacheSaved);
        cacheSavedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSave.cacheSaved = isChecked;
            }
        });

        cacheHistoryCheck.setChecked(LocalSave.cacheHistory);
        cacheHistoryCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSave.cacheHistory = isChecked;
            }
        });


        saveCachedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save Cache
                LocalSave.GetAllSaved();
            }
        });

        clearCachedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalSave.ClearCachedPosts();
            }
        });

        removeAllSavedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(v.getContext(), R.style.MaterialAlertDialogTheme)
                        .setTitle("Clear?")
                        .setMessage("Clear Saved Posts")
                        .setPositiveButton(R.string.yes, (dialogInterface, i)
                                -> LocalSave.ClearSavedPosts())
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });

        alert.setView(view);

        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_local_posts);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        //mViewPager2 = viewPager2;

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                LocalSave.Filter(query);
                sectionsPagerAdapter.refresh();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                if(newText.isBlank())
                {
                    onQueryTextSubmit("");
                }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        fragmentManager = getSupportFragmentManager();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

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
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        applyTabLayoutTheme(tabLayout);
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        tabLayout.setVisibility(View.GONE);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(2);
        //viewPager2.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        viewPager2.setUserInputEnabled(false);
        /*new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.posts));
                    break;
            }
        }).attach();*/

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });

        fixViewPager2Sensitivity(viewPager2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.local_posts_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh_local_posts_activity) {
            sectionsPagerAdapter.refresh();
            return true;
        } else if (itemId == R.id.action_change_post_layout_local_posts_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }  else if (itemId == R.id.change_sort_type) {
            ChangeSort();
            return true;
        } else if (itemId == R.id.manual_save) {
            LocalSave.SaveLocalPosts();
            return true;
        } else if (itemId == R.id.local_settings) {
            ShowLocalSaveSettings();
            return true;
        } else if (itemId == R.id.backup_save) {

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "backup.txt");

            startActivityForResult(intent, LocalSave.CREATE_BACKUP);

            return true;
        } else if (itemId == R.id.backup_load) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");

            startActivityForResult(intent, LocalSave.LOAD_BACKUP);

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }

        if (requestCode == LocalSave.CREATE_BACKUP) {
            Uri uri = null;
            if (resultData != null) {
                LocalSave.SaveBackup(resultData.getData());
            }
        } else if(requestCode == LocalSave.LOAD_BACKUP) {
            Uri uri = null;
            if (resultData != null) {
                LocalSave.LoadBackup(resultData.getData());
            }
        }
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
            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.LOCAL_POST_LAYOUT, postLayout).apply();
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
                LocalPostFragment fragment = new LocalPostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(LocalPostFragment.EXTRA_LOCALPOST_TYPE, LocalPostFragment.LOCALPOST_TYPE_READ_POSTS);
                bundle.putString(LocalPostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(LocalPostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                LocalPostFragment fragment = new LocalPostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(LocalPostFragment.EXTRA_LOCALPOST_TYPE, LocalPostFragment.LOCALPOST_TYPE_READ_POSTS);
                bundle.putString(LocalPostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(LocalPostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
        }

        public boolean handleKeyDown(int keyCode) {
            if (viewPager2.getCurrentItem() == 0) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof PostFragment) {
                    return ((PostFragment) fragment).handleKeyDown(keyCode);
                }
            }
            return false;
        }

        public void refresh() {
            Fragment fragment = getCurrentFragment();
            ((LocalPostFragment)fragment).refresh();
        }

        public void changeNSFW(boolean nsfw) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeNSFW(nsfw);
            }
        }

        public void changePostLayout(int postLayout) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof LocalPostFragment) {
                ((LocalPostFragment) fragment).changePostLayout(postLayout);
            }
        }


        public void goBackToTop() {
            Fragment fragment = getCurrentFragment();
            ((LocalPostFragment)fragment).goBackToTop();
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}