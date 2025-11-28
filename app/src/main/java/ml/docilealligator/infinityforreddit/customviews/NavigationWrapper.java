package ml.docilealligator.infinityforreddit.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class NavigationWrapper {
    public BottomAppBar bottomAppBar;
    public LinearLayout linearLayoutBottomAppBar;
    public ImageView option1BottomAppBar;
    public ImageView option2BottomAppBar;
    public ImageView option3BottomAppBar;
    public ImageView option4BottomAppBar;

    public NavigationRailView navigationRailView;
    public FloatingActionButton floatingActionButton;

    private CustomThemeWrapper customThemeWrapper;
    private int option1 = -1;
    private int option2 = -1;
    private int option3 = -1;
    private int option4 = -1;

    private int inboxCount;
    private BadgeDrawable badgeDrawable;

    public NavigationWrapper(BottomAppBar bottomAppBar, LinearLayout linearLayoutBottomAppBar,
                             ImageView option1BottomAppBar, ImageView option2BottomAppBar,
                             ImageView option3BottomAppBar, ImageView option4BottomAppBar,
                             FloatingActionButton floatingActionButton, NavigationRailView navigationRailView,
                             CustomThemeWrapper customThemeWrapper,
                             boolean showBottomAppBar) {
        this.bottomAppBar = bottomAppBar;
        this.linearLayoutBottomAppBar = linearLayoutBottomAppBar;
        this.option1BottomAppBar = option1BottomAppBar;
        this.option2BottomAppBar = option2BottomAppBar;
        this.option3BottomAppBar = option3BottomAppBar;
        this.option4BottomAppBar = option4BottomAppBar;
        this.navigationRailView = navigationRailView;
        this.customThemeWrapper = customThemeWrapper;
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

    public void bindOptions(int... options) {
        if (options.length == 2) {
            if (navigationRailView == null) {
                option2 = options[0];
                option4 = options[1];
            } else {
                option1 = options[0];
                option2 = options[1];
            }
        } else {
            option1 = options[0];
            option2 = options[1];
            option3 = options[2];
            option4 = options[3];
        }
    }

    public void setOtherActivitiesContentDescription(Context context, View view, int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME:
                view.setContentDescription(context.getString(R.string.content_description_home));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                view.setContentDescription(context.getString(R.string.content_description_subscriptions));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX:
                view.setContentDescription(context.getString(R.string.content_description_inbox));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE:
                view.setContentDescription(context.getString(R.string.content_description_profile));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                view.setContentDescription(context.getString(R.string.content_description_multireddits));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                view.setContentDescription(context.getString(R.string.content_description_submit_post));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH:
                view.setContentDescription(context.getString(R.string.content_description_refresh));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                view.setContentDescription(context.getString(R.string.content_description_change_sort_type));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                view.setContentDescription(context.getString(R.string.content_description_change_post_layout));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SEARCH:
                view.setContentDescription(context.getString(R.string.content_description_search));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                view.setContentDescription(context.getString(R.string.content_description_go_to_subreddit));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                view.setContentDescription(context.getString(R.string.content_description_go_to_user));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_RANDOM:
                view.setContentDescription(context.getString(R.string.content_description_random));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                view.setContentDescription(context.getString(R.string.content_description_hide_read_posts));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                view.setContentDescription(context.getString(R.string.content_description_filter_posts));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_UPVOTED:
                view.setContentDescription(context.getString(R.string.content_description_upvoted));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                view.setContentDescription(context.getString(R.string.content_description_downvoted));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDDEN:
                view.setContentDescription(context.getString(R.string.content_description_hidden));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SAVED:
                view.setContentDescription(context.getString(R.string.content_description_saved));
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
            default:
                view.setContentDescription(context.getString(R.string.content_description_go_to_top));
                break;
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

    @ExperimentalBadgeUtils
    public void setInboxCount(Context context, int inboxCount) {
        if (inboxCount < 0) {
            this.inboxCount = Math.max(0, this.inboxCount + inboxCount);
        } else {
            this.inboxCount = inboxCount;
        }

        if (option1 == SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX || option1 == SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX) {
            if (navigationRailView == null) {
                if (this.inboxCount == 0) {
                    BadgeUtils.detachBadgeDrawable(badgeDrawable, option1BottomAppBar);
                    badgeDrawable = null;
                } else {
                    BadgeUtils.attachBadgeDrawable(getBadgeDrawable(context, inboxCount, option1BottomAppBar), option1BottomAppBar);
                }
            }
        } else if (option2 == SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX || option2 == SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX) {
            if (navigationRailView == null) {
                if (this.inboxCount == 0) {
                    BadgeUtils.detachBadgeDrawable(badgeDrawable, option2BottomAppBar);
                    badgeDrawable = null;
                } else {
                    BadgeUtils.attachBadgeDrawable(getBadgeDrawable(context, inboxCount, option2BottomAppBar), option2BottomAppBar);
                }
            }
        } else if (option3 == SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX || option3 == SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX) {
            if (navigationRailView == null) {
                if (this.inboxCount == 0) {
                    BadgeUtils.detachBadgeDrawable(badgeDrawable, option3BottomAppBar);
                    badgeDrawable = null;
                } else {
                    BadgeUtils.attachBadgeDrawable(getBadgeDrawable(context, inboxCount, option3BottomAppBar), option3BottomAppBar);
                }
            }
        } else if (option4 == SharedPreferencesUtils.MAIN_ACTIVITY_BOTTOM_APP_BAR_OPTION_INBOX || option4 == SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX) {
            if (navigationRailView == null) {
                if (this.inboxCount == 0) {
                    BadgeUtils.detachBadgeDrawable(badgeDrawable, option4BottomAppBar);
                    badgeDrawable = null;
                } else {
                    BadgeUtils.attachBadgeDrawable(getBadgeDrawable(context, inboxCount, option4BottomAppBar), option4BottomAppBar);
                }
            }
        }
    }

    private BadgeDrawable getBadgeDrawable(Context context, int inboxCount, View anchorView) {
        BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
        badgeDrawable.setNumber(inboxCount);
        badgeDrawable.setBackgroundColor(customThemeWrapper.getColorAccent());
        badgeDrawable.setBadgeTextColor(customThemeWrapper.getButtonTextColor());
        badgeDrawable.setHorizontalOffset(anchorView.getWidth() / 2);

        this.badgeDrawable = badgeDrawable;

        return badgeDrawable;
    }
}
