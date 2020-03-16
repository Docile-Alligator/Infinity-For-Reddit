package ml.docilealligator.infinityforreddit.CustomTheme;

import android.content.SharedPreferences;
import android.graphics.Color;

import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.AMOLED_DARK;
import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.DARK;

public class CustomThemeWrapper {
    private SharedPreferences themeSharedPreferences;
    private int themeType;

    public CustomThemeWrapper(SharedPreferences themeSharedPreferences) {
        this.themeSharedPreferences = themeSharedPreferences;
    }

    private int getDefaultColor(String normalHex, String darkHex, String amoledDarkHex) {
        switch (themeType) {
            case DARK:
                return Color.parseColor(darkHex);
            case AMOLED_DARK:
                return Color.parseColor(amoledDarkHex);
            default:
                return Color.parseColor(normalHex);
        }
    }

    public void setThemeType(int themeType) {
        this.themeType = themeType;
    }

    public boolean isLightStatusBar() {
        return themeSharedPreferences.getBoolean(CustomThemeSharedPreferencesUtils.LIGHT_STATUS_BAR, false);
    }

    public boolean isLightNavBar() {
        return themeSharedPreferences.getBoolean(CustomThemeSharedPreferencesUtils.LIGHT_NAV_BAR, false);
    }

    public boolean isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface() {
        return themeSharedPreferences.getBoolean(
                CustomThemeSharedPreferencesUtils.CHANGE_STATUS_BAR_ICON_COLOR_AFTER_TOOLBAR_COLLAPSED_IN_IMMERSIVE_INTERFACE,
                true);
    }

    public int getColorPrimary() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY,
                getDefaultColor("#1565C0", "#242424", "#000000"));
    }

    public int getColorPrimaryDark() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_DARK,
                getDefaultColor("#0D47A1", "#121212", "#000000"));
    }

    public int getColorAccent() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getColorPrimaryLightTheme() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getPostTitleColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_TITLE_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPostContentColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_CONTENT_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getCommentColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPrimaryTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.PRIMARY_TEXT_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getSecondaryTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SECONDARY_TEXT_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getButtonTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.BUTTON_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#121212", "#000000"));
    }

    public int getCardViewBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CARD_VIEW_BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getCommentBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getToolbarPrimaryTextAndIconColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_PRIMARY_TEXT_AND_ICON_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getToolbarAndTabBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_AND_TAB_BACKGROUND_COLOR,
                getDefaultColor("#1565C0", "#282828", "#000000"));
    }

    public int getCircularProgressBarBackground() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CIRCULAR_PROGRESS_BAR_BACKGROUND,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabBackground() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor("#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabIndicator() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor("#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabBackground() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor("#1565C0", "#242424", "#000000"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabIndicator() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNavBarColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NAV_BAR_COLOR,
                getDefaultColor("#FFFFFF", "#121212", "#000000"));
    }

    public int getUpvoted() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UPVOTED,
                getDefaultColor("#E91E63", "#E91E63", "#E91E63"));
    }

    public int getDownvoted() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.DOWNVOTED,
                getDefaultColor("#007DDE", "#007DDE", "#007DDE"));
    }

    public int getPostTypeBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_TYPE_BACKGROUND_COLOR,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getPostTypeTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_TYPE_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getSpoilerBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SPOILER_BACKGROUND_COLOR,
                getDefaultColor("#EE02EB", "#EE02EB", "#EE02EB"));
    }

    public int getSpoilerTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SPOILER_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNsfwBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NSFW_BACKGROUND_COLOR,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getNsfwTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NSFW_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getFlairBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.FLAIR_BACKGROUND_COLOR,
                getDefaultColor("#00AA8C", "#00AA8C", "#00AA8C"));
    }

    public int getFlairTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.FLAIR_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getArchivedTint() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.ARCHIVED_TINT,
                getDefaultColor("#B4009F", "#B4009F", "#B4009F"));
    }

    public int getLockedIconTint() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.LOCKED_ICON_TINT,
                getDefaultColor("#EE7302", "#EE7302", "#EE7302"));
    }

    public int getCrosspostIconTint() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CROSSPOST_ICON_TINT,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getStickiedPostIconTint() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.STICKIED_POST_ICON_TINT,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSubscribed() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBSCRIBED,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getUnsubscribed() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UNSUBSCRIBED,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getUsername() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.USERNAME,
                getDefaultColor("#0D47A1", "#1E88E5", "#1E88E5"));
    }

    public int getSubreddit() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBREDDIT,
                getDefaultColor("#E91E63", "#E91E63", "#E91E63"));
    }

    public int getAuthorFlairTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.AUTHOR_FLAIR_TEXT_COLOR,
                getDefaultColor("#EE02C4", "#EE02C4", "#EE02C4"));
    }

    public int getSubmitter() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBMITTER,
                getDefaultColor("#EE8A02", "#EE8A02", "#EE8A02"));
    }

    public int getModerator() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.MODERATOR,
                getDefaultColor("#00BA81", "#00BA81", "#00BA81"));
    }

    public int getNotificationIconColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NOTIFICATION_ICON_COLOR,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getSingleCommentThreadBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SINGLE_COMMENT_THREAD_BACKGROUND,
                getDefaultColor("#B3E5F9", "#123E77", "#123E77"));
    }

    public int getUnreadMessageBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UNREAD_MESSAGE_BACKGROUND_COLOR,
                getDefaultColor("#B3E5F9", "#123E77", "#123E77"));
    }

    public int getDividerColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.DIVIDER_COLOR,
                getDefaultColor("#E0E0E0", "#69666C", "#69666C"));
    }

    public int getNoPreviewLinkBackgroundColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NO_PREVIEW_LINK_BACKGROUND_COLOR,
                getDefaultColor("#E0E0E0", "#424242", "#424242"));
    }

    public int getVoteAndReplyUnavailableVoteButtonColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.VOTE_AND_REPLY_UNAVAILABLE_VOTE_BUTTON_COLOR,
                getDefaultColor("#F0F0F0", "#3C3C3C", "#3C3C3C"));
    }

    public int getCommentVerticalBarColor1() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_1,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getCommentVerticalBarColor2() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_2,
                getDefaultColor("#EE02BE", "#C300B3", "#C300B3"));
    }

    public int getCommentVerticalBarColor3() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_3,
                getDefaultColor("#02DFEE", "#00B8DA", "#00B8DA"));
    }

    public int getCommentVerticalBarColor4() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_4,
                getDefaultColor("#EED502", "#EDCA00", "#EDCA00"));
    }

    public int getCommentVerticalBarColor5() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_5,
                getDefaultColor("#EE0220", "#EE0219", "#EE0219"));
    }

    public int getCommentVerticalBarColor6() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_6,
                getDefaultColor("#02EE6E", "#00B925", "#00B925"));
    }

    public int getCommentVerticalBarColor7() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_7,
                getDefaultColor("#EE4602", "#EE4602", "#EE4602"));
    }

    public int getFABIconColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.FAB_ICON_COLOR,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getChipTextColor() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CHIP_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }
}
