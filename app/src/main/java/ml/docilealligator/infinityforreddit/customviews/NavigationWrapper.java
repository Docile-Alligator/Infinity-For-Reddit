package ml.docilealligator.infinityforreddit.customviews;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import ml.docilealligator.infinityforreddit.R;

public class NavigationWrapper {
    public BottomAppBar bottomAppBar;
    public LinearLayout linearLayoutBottomAppBar;
    public ImageView option1BottomAppBar;
    public ImageView option2BottomAppBar;
    public ImageView option3BottomAppBar;
    public ImageView option4BottomAppBar;

    public NavigationRailView navigationRailView;
    public FloatingActionButton floatingActionButton;

    public NavigationWrapper(BottomAppBar bottomAppBar, LinearLayout linearLayoutBottomAppBar,
                             ImageView option1BottomAppBar, ImageView option2BottomAppBar,
                             ImageView option3BottomAppBar, ImageView option4BottomAppBar,
                             FloatingActionButton floatingActionButton, NavigationRailView navigationRailView,
                             boolean showBottomAppBar) {
        this.bottomAppBar = bottomAppBar;
        this.linearLayoutBottomAppBar = linearLayoutBottomAppBar;
        this.option1BottomAppBar = option1BottomAppBar;
        this.option2BottomAppBar = option2BottomAppBar;
        this.option3BottomAppBar = option3BottomAppBar;
        this.option4BottomAppBar = option4BottomAppBar;
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
            option1BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            option2BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            option3BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            option4BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            bottomAppBar.setBackgroundTint(ColorStateList.valueOf(bottomAppBarBackgroundColor));
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
                linearLayoutBottomAppBar.setWeightSum(3);
                option1BottomAppBar.setVisibility(View.GONE);
                option3BottomAppBar.setVisibility(View.GONE);

                option2BottomAppBar.setImageResource(imageResources[0]);
                option4BottomAppBar.setImageResource(imageResources[1]);
            } else {
                Menu menu = navigationRailView.getMenu();
                menu.findItem(R.id.navigation_rail_option_1).setIcon(imageResources[0]);
                menu.findItem(R.id.navigation_rail_option_2).setIcon(imageResources[1]);
                menu.findItem(R.id.navigation_rail_option_3).setVisible(false);
                menu.findItem(R.id.navigation_rail_option_4).setVisible(false);
            }
        } else {
            if (navigationRailView == null) {
                option1BottomAppBar.setImageResource(imageResources[0]);
                option2BottomAppBar.setImageResource(imageResources[1]);
                option3BottomAppBar.setImageResource(imageResources[2]);
                option4BottomAppBar.setImageResource(imageResources[3]);
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
