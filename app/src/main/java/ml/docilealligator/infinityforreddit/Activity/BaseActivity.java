package ml.docilealligator.infinityforreddit.Activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Font.ContentFontFamily;
import ml.docilealligator.infinityforreddit.Font.ContentFontStyle;
import ml.docilealligator.infinityforreddit.Font.FontFamily;
import ml.docilealligator.infinityforreddit.Font.FontStyle;
import ml.docilealligator.infinityforreddit.Font.TitleFontFamily;
import ml.docilealligator.infinityforreddit.Font.TitleFontStyle;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean immersiveInterface;
    private boolean changeStatusBarIconColor;
    private boolean transparentStatusBarAfterToolbarCollapsed;
    private boolean hasDrawerLayout = false;
    private boolean isImmersiveInterfaceApplicable = true;
    private int systemVisibilityToolbarExpanded = 0;
    private int systemVisibilityToolbarCollapsed = 0;
    private CustomThemeWrapper customThemeWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customThemeWrapper = getCustomThemeWrapper();

        SharedPreferences mSharedPreferences = getDefaultSharedPreferences();
        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int systemThemeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        immersiveInterface = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true);
        switch (systemThemeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                getTheme().applyStyle(R.style.Theme_Normal, true);
                customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                if(mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                    getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                    customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                } else {
                    getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                    customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                }
                break;
            case 2:
                if (systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }
                if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                    getTheme().applyStyle(R.style.Theme_Normal, true);
                    customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                } else {
                    if(mSharedPreferences.getBoolean(SharedPreferencesUtils.AMOLED_DARK_KEY, false)) {
                        getTheme().applyStyle(R.style.Theme_Normal_AmoledDark, true);
                        customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                    } else {
                        getTheme().applyStyle(R.style.Theme_Normal_NormalDark, true);
                        customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                    }
                }
        }

        boolean userDefinedChangeSatusBarIconColorInImmersiveInterface =
                customThemeWrapper.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface();
        if (immersiveInterface && isImmersiveInterfaceApplicable) {
            changeStatusBarIconColor = userDefinedChangeSatusBarIconColorInImmersiveInterface;
        } else {
            changeStatusBarIconColor = false;
        }

        getTheme().applyStyle(FontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(TitleFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, TitleFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(ContentFontStyle.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, ContentFontStyle.Normal.name())).getResId(), true);

        getTheme().applyStyle(FontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.FONT_FAMILY_KEY, FontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(TitleFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, TitleFontFamily.Default.name())).getResId(), true);

        getTheme().applyStyle(ContentFontFamily.valueOf(mSharedPreferences
                .getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ContentFontFamily.Default.name())).getResId(), true);

        Window window = getWindow();
        View decorView = window.getDecorView();
        if (!hasDrawerLayout) {
            window.setStatusBarColor(customThemeWrapper.getColorPrimaryDark());
        }
        boolean isLightStatusbar = customThemeWrapper.isLightStatusBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean isLightNavBar = customThemeWrapper.isLightNavBar();
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
            window.setNavigationBarColor(customThemeWrapper.getNavBarColor());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isLightStatusbar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                systemVisibilityToolbarExpanded = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (!changeStatusBarIconColor) {
                    systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            } else if (changeStatusBarIconColor) {
                systemVisibilityToolbarCollapsed = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
    }

    protected abstract SharedPreferences getDefaultSharedPreferences();

    protected abstract CustomThemeWrapper getCustomThemeWrapper();

    protected abstract void applyCustomTheme();

    protected boolean isChangeStatusBarIconColor() {
        return changeStatusBarIconColor;
    }

    protected int getSystemVisibilityToolbarExpanded() {
        return systemVisibilityToolbarExpanded;
    }

    protected int getSystemVisibilityToolbarCollapsed() {
        return systemVisibilityToolbarCollapsed;
    }

    public boolean isImmersiveInterface() {
        return immersiveInterface;
    }

    protected void setToolbarGoToTop(Toolbar toolbar) {
        toolbar.setOnLongClickListener(view -> {
            if (BaseActivity.this instanceof ActivityToolbarInterface) {
                ((ActivityToolbarInterface) BaseActivity.this).onLongPress();
            }
            return true;
        });
    }

    protected void adjustToolbar(Toolbar toolbar) {
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarResourceId > 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
            toolbar.setLayoutParams(params);
        }
    }

    protected void addOnOffsetChangedListener(AppBarLayout appBarLayout) {
        View decorView = getWindow().getDecorView();
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                if (state == State.COLLAPSED) {
                    decorView.setSystemUiVisibility(getSystemVisibilityToolbarCollapsed());
                } else if (state == State.EXPANDED) {
                    decorView.setSystemUiVisibility(getSystemVisibilityToolbarExpanded());
                }
            }
        });
    }

    public int getNavBarHeight() {
        if (getDefaultSharedPreferences().getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_IGNORE_NAV_BAR_KEY, false)) {
            return 0;
        }

        Resources resources = getResources();
        int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (navBarResourceId > 0) {
            return resources.getDimensionPixelSize(navBarResourceId);
        }
        return 0;
    }

    protected void setTransparentStatusBarAfterToolbarCollapsed() {
        this.transparentStatusBarAfterToolbarCollapsed = true;
    }

    protected void setHasDrawerLayout() {
        hasDrawerLayout = true;
    }

    public void setImmersiveModeNotApplicable() {
        isImmersiveInterfaceApplicable = false;
    }

    protected void applyAppBarLayoutAndToolbarTheme(AppBarLayout appBarLayout, Toolbar toolbar) {
        appBarLayout.setBackgroundColor(customThemeWrapper.getColorPrimary());
        toolbar.setTitleTextColor(customThemeWrapper.getToolbarPrimaryTextAndIconColor());
        toolbar.setSubtitleTextColor(customThemeWrapper.getToolbarSecondaryTextColor());
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(customThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (toolbar.getOverflowIcon() != null) {
            toolbar.getOverflowIcon().setColorFilter(customThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @SuppressLint("RestrictedApi")
    protected boolean applyMenuItemTheme(Menu menu) {
        if (customThemeWrapper != null) {
            int size = Math.min(menu.size(), 2);
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (((MenuItemImpl) item).requestsActionButton()) {
                    Drawable drawable = item.getIcon();
                    if (drawable != null) {
                        DrawableCompat.setTint(drawable, customThemeWrapper.getToolbarPrimaryTextAndIconColor());
                        item.setIcon(drawable);
                    }
                }
            }
        }
        return true;
    }

    protected void applyTabLayoutTheme(TabLayout tabLayout) {
        int toolbarAndTabBackgroundColor = customThemeWrapper.getColorPrimary();
        tabLayout.setBackgroundColor(toolbarAndTabBackgroundColor);
        tabLayout.setSelectedTabIndicatorColor(customThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabIndicator());
        tabLayout.setTabTextColors(customThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTextColor(),
                customThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTextColor());
    }

    protected void applyFABTheme(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(customThemeWrapper.getColorPrimaryLightTheme()));
        fab.setImageTintList(ColorStateList.valueOf(customThemeWrapper.getFABIconColor()));
    }
}
