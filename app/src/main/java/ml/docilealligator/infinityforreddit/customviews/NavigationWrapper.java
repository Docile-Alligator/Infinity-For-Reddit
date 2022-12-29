package ml.docilealligator.infinityforreddit.customviews;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import ml.docilealligator.infinityforreddit.R;

public class NavigationWrapper {
    public BottomAppBar bottomAppBar;

    public NavigationRailView navigationRailView;
    public FloatingActionButton floatingActionButton;

    public NavigationWrapper(BottomAppBar bottomAppBar, FloatingActionButton floatingActionButton,
                             NavigationRailView navigationRailView, boolean showBottomAppBar) {
        this.bottomAppBar = bottomAppBar;
        this.navigationRailView = navigationRailView;
        if (navigationRailView != null) {
            if (showBottomAppBar) {
                this.floatingActionButton = (FloatingActionButton) navigationRailView.getHeaderView();
            } else {
                navigationRailView.setVisibility(View.GONE);
                this.floatingActionButton = floatingActionButton;
            }
        } else {
            this.floatingActionButton = floatingActionButton;
        }
    }

    public void applyCustomTheme(int bottomAppBarIconColor, int bottomAppBarBackgroundColor) {
        if (navigationRailView == null) {
            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(bottomAppBarBackgroundColor));
            applyMenuItemTheme(bottomAppBar.getMenu(), bottomAppBarIconColor);
        } else {
            navigationRailView.setBackgroundColor(bottomAppBarBackgroundColor);
            applyMenuItemTheme(navigationRailView.getMenu(), bottomAppBarIconColor);
        }
    }

    @SuppressLint("RestrictedApi")
    private void applyMenuItemTheme(Menu menu, int bottomAppBarIconColor) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (((MenuItemImpl) item).requestsActionButton()) {
                MenuItemCompat.setIconTintList(item, ColorStateList.valueOf(bottomAppBarIconColor));
            }
        }
    }

    public void bindOptionDrawableResource(int... imageResources) {
        if (navigationRailView == null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        } else {
            navigationRailView.setVisibility(View.VISIBLE);
        }

        if (imageResources.length == 2) {
            if (navigationRailView == null) {
                Menu menu = bottomAppBar.getMenu();
                menu.findItem(R.id.bottom_app_bar_option_1).setIcon(imageResources[0]);
                menu.findItem(R.id.bottom_app_bar_option_2).setIcon(imageResources[1]);
                menu.findItem(R.id.bottom_app_bar_option_3).setVisible(false);
                menu.findItem(R.id.bottom_app_bar_option_4).setVisible(false);
            } else {
                Menu menu = navigationRailView.getMenu();
                menu.findItem(R.id.navigation_rail_option_1).setIcon(imageResources[0]);
                menu.findItem(R.id.navigation_rail_option_2).setIcon(imageResources[1]);
                menu.findItem(R.id.navigation_rail_option_3).setVisible(false);
                menu.findItem(R.id.navigation_rail_option_4).setVisible(false);
            }
        } else {
            if (navigationRailView == null) {
                Menu menu = bottomAppBar.getMenu();
                menu.findItem(R.id.bottom_app_bar_option_1).setIcon(imageResources[0]);
                menu.findItem(R.id.bottom_app_bar_option_2).setIcon(imageResources[1]);
                menu.findItem(R.id.bottom_app_bar_option_3).setIcon(imageResources[2]);
                menu.findItem(R.id.bottom_app_bar_option_4).setIcon(imageResources[3]);
            } else {
                Menu menu = navigationRailView.getMenu();
                menu.findItem(R.id.navigation_rail_option_1).setIcon(imageResources[0]);
                menu.findItem(R.id.navigation_rail_option_2).setIcon(imageResources[1]);
                menu.findItem(R.id.navigation_rail_option_3).setIcon(imageResources[2]);
                menu.findItem(R.id.navigation_rail_option_4).setIcon(imageResources[3]);
            }
        }
    }

    public void showNavigation() {
        if (bottomAppBar != null) {
            bottomAppBar.performShow();
        }
    }

    public void hideNavigation() {
        if (bottomAppBar != null) {
            bottomAppBar.performHide();
        }
    }

    public void showFab() {
        if (navigationRailView == null) {
            floatingActionButton.show();
        }
    }

    public void hideFab() {
        if (navigationRailView == null) {
            floatingActionButton.hide();
        }
    }
}
