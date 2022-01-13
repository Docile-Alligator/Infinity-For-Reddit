package ml.docilealligator.infinityforreddit.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.customviews.ViewPagerBugFixed;
import ml.docilealligator.infinityforreddit.font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.font.FontStyle;
import ml.docilealligator.infinityforreddit.font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.fragments.ThemePreviewCommentsFragment;
import ml.docilealligator.infinityforreddit.fragments.ThemePreviewPostsFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CustomThemePreviewActivity extends AppCompatActivity implements CustomFontReceiver {

    public static final String EXTRA_CUSTOM_THEME_SETTINGS_ITEMS = "ECTSI";
    public Typeface typeface;
    public Typeface titleTypeface;
    public Typeface contentTypeface;

    @BindView(R.id.coordinator_layout_theme_preview_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_theme_preview_activity)
    ViewPagerBugFixed viewPager;
    @BindView(R.id.appbar_layout_theme_preview_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_theme_preview_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_linear_layout_theme_preview_activity)
    LinearLayout linearLayout;
    @BindView(R.id.subreddit_name_text_view_theme_preview_activity)
    TextView subredditNameTextView;
    @BindView(R.id.user_name_text_view_theme_preview_activity)
    TextView usernameTextView;
    @BindView(R.id.subscribe_subreddit_chip_theme_preview_activity)
    Chip subscribeSubredditChip;
    @BindView(R.id.primary_text_text_view_theme_preview_activity)
    TextView primaryTextView;
    @BindView(R.id.secondary_text_text_view_theme_preview_activity)
    TextView secondaryTextView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_theme_preview_activity)
    TabLayout tabLayout;
    @BindView(R.id.bottom_navigation_theme_preview_activity)
    BottomAppBar bottomNavigationView;
    @BindView(R.id.linear_layout_bottom_app_bar_theme_preview_activity)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.subscriptions_bottom_app_bar_theme_preview_activity)
    ImageView subscriptionsBottomAppBar;
    @BindView(R.id.multi_reddit_bottom_app_bar_theme_preview_activity)
    ImageView multiRedditBottomAppBar;
    @BindView(R.id.message_bottom_app_bar_theme_preview_activity)
    ImageView messageBottomAppBar;
    @BindView(R.id.profile_bottom_app_bar_theme_preview_activity)
    ImageView profileBottomAppBar;
    @BindView(R.id.fab_theme_preview_activity)
    FloatingActionButton fab;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    private ArrayList<CustomThemeSettingsItem> customThemeSettingsItems;
    private CustomTheme customTheme;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private int unsubscribedColor;
    private int subscribedColor;
    private int systemVisibilityToolbarExpanded = 0;
    private int systemVisibilityToolbarCollapsed = 0;
    private SlidrInterface mSlidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        customThemeSettingsItems = getIntent().getParcelableArrayListExtra(EXTRA_CUSTOM_THEME_SETTINGS_ITEMS);
        customTheme = CustomTheme.convertSettingsItemsToCustomTheme(customThemeSettingsItems, "ThemePreview");

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int systemThemeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (systemThemeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Normal, true);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                } else {
                    getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                }
                break;
            case 2:
                if (systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    getTheme().applyStyle(R.style.Theme_Normal, true);
                } else {
                    if (mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                        getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                    } else {
                        getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                    }
                }
        }

        boolean immersiveInterface = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true);
        boolean changeStatusBarIconColor = false;
        if (immersiveInterface) {
            changeStatusBarIconColor = customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface;
        }
        boolean isLightStatusbar = customTheme.isLightStatusBar;
        Window window = getWindow();
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean isLightNavBar = customTheme.isLightNavBar;
            if (isLightStatusbar) {
                if (isLightNavBar) {
                    systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    if (changeStatusBarIconColor) {
                        systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    } else {
                        systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    }
                } else {
                    systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    if (!changeStatusBarIconColor) {
                        systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                }
            } else {
                if (isLightNavBar) {
                    systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    if (changeStatusBarIconColor) {
                        systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    }
                } else {
                    if (changeStatusBarIconColor) {
                        systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                }
            }
            decorView.setSystemUiVisibility(systemVisibilityToolbarExpanded);
            window.setNavigationBarColor(customTheme.navBarColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isLightStatusbar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        setContentView(R.layout.activity_theme_preview);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (immersiveInterface) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                Resources resources = getResources();
                int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    int navBarHeight = resources.getDimensionPixelSize(navBarResourceId);
                    if (navBarHeight > 0) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                        params.bottomMargin = navBarHeight;
                        fab.setLayoutParams(params);
                        linearLayoutBottomAppBar.setPadding(0,
                                (int) (6 * getResources().getDisplayMetrics().density), 0, navBarHeight);
                    }
                }
            }

            if (changeStatusBarIconColor) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                        if (state == State.COLLAPSED) {
                            decorView.setSystemUiVisibility(systemVisibilityToolbarCollapsed);
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            decorView.setSystemUiVisibility(systemVisibilityToolbarExpanded);
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            } else {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                        if (state == State.COLLAPSED) {
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            }
        } else {
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
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

        adjustToolbar(toolbar);
        setSupportActionBar(toolbar);

        subscribeSubredditChip.setOnClickListener(view -> {
            if (subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                subscribeSubredditChip.setText(R.string.unsubscribe);
                subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
            } else {
                subscribeSubredditChip.setText(R.string.subscribe);
                subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
            }
        });

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    private void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(customTheme.backgroundColor);
        collapsingToolbarLayout.setContentScrimColor(customTheme.colorPrimary);
        subscribeSubredditChip.setTextColor(customTheme.chipTextColor);
        subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(customTheme.unsubscribed));
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        expandedTabTextColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor;
        expandedTabIndicatorColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator;
        expandedTabBackgroundColor = customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground;
        collapsedTabTextColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor;
        collapsedTabIndicatorColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator;
        collapsedTabBackgroundColor = customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground;
        linearLayout.setBackgroundColor(customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground);
        subredditNameTextView.setTextColor(customTheme.subreddit);
        usernameTextView.setTextColor(customTheme.username);
        subscribeSubredditChip.setTextColor(customTheme.chipTextColor);
        primaryTextView.setTextColor(customTheme.primaryTextColor);
        secondaryTextView.setTextColor(customTheme.secondaryTextColor);
        bottomNavigationView.setBackgroundTint(ColorStateList.valueOf(customTheme.bottomAppBarBackgroundColor));
        int bottomAppBarIconColor = customTheme.bottomAppBarIconColor;
        subscriptionsBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        multiRedditBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        messageBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        profileBottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
        unsubscribedColor = customTheme.unsubscribed;
        subscribedColor = customTheme.subscribed;
        if (typeface != null) {
            subredditNameTextView.setTypeface(typeface);
            usernameTextView.setTypeface(typeface);
            primaryTextView.setTypeface(typeface);
            secondaryTextView.setTypeface(typeface);
            subscribeSubredditChip.setTypeface(typeface);
        }
    }

    protected void applyAppBarLayoutAndToolbarTheme(AppBarLayout appBarLayout, Toolbar toolbar) {
        appBarLayout.setBackgroundColor(customTheme.colorPrimary);
        toolbar.setTitleTextColor(customTheme.toolbarPrimaryTextAndIconColor);
        toolbar.setSubtitleTextColor(customTheme.toolbarSecondaryTextColor);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(customTheme.toolbarPrimaryTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setColorFilter(customTheme.toolbarPrimaryTextAndIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (typeface != null) {
            toolbar.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
                for (int j = 0; j < toolbar.getChildCount(); j++) {
                    if (toolbar.getChildAt(j) instanceof TextView) {
                        ((TextView) toolbar.getChildAt(j)).setTypeface(typeface);
                    }
                }
            });
        }
    }

    private void adjustToolbar(Toolbar toolbar) {
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarResourceId > 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            int statusBarHeight = getResources().getDimensionPixelSize(statusBarResourceId);
            params.topMargin = statusBarHeight;
            toolbar.setLayoutParams(params);
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                ((ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams()).setMargins(0,
                        TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) + statusBarHeight, 0, 0);
            }
        }
    }

    protected void applyTabLayoutTheme(TabLayout tabLayout) {
        int toolbarAndTabBackgroundColor = customTheme.colorPrimary;
        tabLayout.setBackgroundColor(toolbarAndTabBackgroundColor);
        tabLayout.setSelectedTabIndicatorColor(customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator);
        tabLayout.setTabTextColors(customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor,
                customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor);
    }

    protected void applyFABTheme(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(customTheme.colorPrimaryLightTheme));
        fab.setImageTintList(ColorStateList.valueOf(customTheme.fabIconColor));
    }

    public CustomTheme getCustomTheme() {
        return customTheme;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
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

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
        this.titleTypeface = titleTypeface;
        this.contentTypeface = contentTypeface;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ThemePreviewPostsFragment themePreviewPostsFragment;
        private ThemePreviewCommentsFragment themePreviewCommentsFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ThemePreviewPostsFragment();
            }
            return new ThemePreviewCommentsFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return Utils.getTabTextWithCustomFont(typeface, "Posts");
                case 1:
                    return Utils.getTabTextWithCustomFont(typeface, "Comments");
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    themePreviewPostsFragment = (ThemePreviewPostsFragment) fragment;
                    break;
                case 1:
                    themePreviewCommentsFragment = (ThemePreviewCommentsFragment) fragment;
            }
            return fragment;
        }
    }
}
