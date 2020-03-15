package ml.docilealligator.infinityforreddit.CustomTheme;

import android.content.SharedPreferences;
import android.graphics.Color;

import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.AMOLED_DARK;
import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.DARK;
import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.NORMAL;

public class CustomThemeWrapper {
    private SharedPreferences themeSharedPreferences;

    public CustomThemeWrapper(SharedPreferences themeSharedPreferences) {
        this.themeSharedPreferences = themeSharedPreferences;
    }

    private int getDefaultColor(int themeType, String normalHex, String darkHex, String amoledDarkHex) {
        switch (themeType) {
            case DARK:
                return Color.parseColor(darkHex);
            case AMOLED_DARK:
                return Color.parseColor(amoledDarkHex);
            default:
                return Color.parseColor(normalHex);
        }
    }

    public int getThemeType() {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.THEME_TYPE_KEY, NORMAL);
    }

    public int getColorPrimary(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY,
                getDefaultColor(themeType, "#1565C0", "#242424", "#000000"));
    }

    public int getColorPrimaryDark(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_DARK,
                getDefaultColor(themeType, "#0D47A1", "#121212", "#000000"));
    }

    public int getColorAccent(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getColorPrimaryLightTheme(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getPostTitleColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_TITLE_COLOR,
                getDefaultColor(themeType, "#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPostContentColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_CONTENT_COLOR,
                getDefaultColor(themeType, "#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getCommentColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_COLOR,
                getDefaultColor(themeType, "#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPrimaryTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.PRIMARY_TEXT_COLOR,
                getDefaultColor(themeType, "#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getSecondaryTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SECONDARY_TEXT_COLOR,
                getDefaultColor(themeType, "#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getButtonTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.BUTTON_TEXT_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.BACKGROUND_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#121212", "#000000"));
    }

    public int getRoundedBottomSheetPrimaryBackground(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.ROUNDED_BOTTOM_SHEET_PRIMARY_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getCardViewBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CARD_VIEW_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getCommentBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getToolbarPrimaryTextAndIconColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_PRIMARY_TEXT_AND_ICON_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getToolbarAndTabBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_AND_TAB_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#1565C0", "#282828", "#000000"));
    }

    public int getCircularProgressBarBackground(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CIRCULAR_PROGRESS_BAR_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabBackground(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor(themeType, "#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabIndicator(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor(themeType, "#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabBackground(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor(themeType, "#1565C0", "#242424", "#000000"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabIndicator(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNavBarColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NAV_BAR_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#121212", "#000000"));
    }

    public int getUpvoted(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UPVOTED,
                getDefaultColor(themeType, "#E91E63", "#E91E63", "#E91E63"));
    }

    public int getDownvoted(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.DOWNVOTED,
                getDefaultColor(themeType, "#007DDE", "#007DDE", "#007DDE"));
    }

    public int getPostType(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.POST_TYPE,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSpoilerColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SPOILER_COLOR,
                getDefaultColor(themeType, "#EE02EB", "#EE02EB", "#EE02EB"));
    }

    public int getNsfwColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NSFW_COLOR,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getFlairColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.FLAIR_COLOR,
                getDefaultColor(themeType, "#00AA8C", "#00AA8C", "#00AA8C"));
    }

    public int getArchivedTint(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.ARCHIVED_TINT,
                getDefaultColor(themeType, "#B4009F", "#B4009F", "#B4009F"));
    }

    public int getLockedIconTint(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.LOCKED_ICON_TINT,
                getDefaultColor(themeType, "#EE7302", "#EE7302", "#EE7302"));
    }

    public int getCrosspost(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CROSSPOST,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getStickiedPost(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.STICKIED_POST,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSubscribed(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBSCRIBED,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getUnsubscribed(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UNSUBSCRIBED,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getUsername(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.USERNAME,
                getDefaultColor(themeType, "#0D47A1", "#1E88E5", "#1E88E5"));
    }

    public int getSubreddit(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBREDDIT,
                getDefaultColor(themeType, "#E91E63", "#E91E63", "#E91E63"));
    }

    public int getAuthorFlairTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.AUTHOR_FLAIR_TEXT_COLOR,
                getDefaultColor(themeType, "#EE02C4", "#EE02C4", "#EE02C4"));
    }

    public int getSubmitter(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SUBMITTER,
                getDefaultColor(themeType, "#EE8A02", "#EE8A02", "#EE8A02"));
    }

    public int getModerator(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.MODERATOR,
                getDefaultColor(themeType, "#00BA81", "#00BA81", "#00BA81"));
    }

    public int getNotificationIconColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NOTIFICATION_ICON_COLOR,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getSingleCommentThreadBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.SINGLE_COMMENT_THREAD_BACKGROUND,
                getDefaultColor(themeType, "#B3E5F9", "#123E77", "#123E77"));
    }

    public int getUnreadMessageBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.UNREAD_MESSAGE_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#B3E5F9", "#123E77", "#123E77"));
    }

    public int getDividerColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.DIVIDER_COLOR,
                getDefaultColor(themeType, "#E0E0E0", "#69666C", "#69666C"));
    }

    public int getNoPreviewLinkBackgroundColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.NO_PREVIEW_LINK_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#E0E0E0", "#424242", "#424242"));
    }

    public int getVoteAndReplyUnavailableVoteButtonColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.VOTE_AND_REPLY_UNAVAILABLE_VOTE_BUTTON_COLOR,
                getDefaultColor(themeType, "#F0F0F0", "#3C3C3C", "#3C3C3C"));
    }

    public int getCommentVerticalBarColor1(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_1,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getCommentVerticalBarColor2(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_2,
                getDefaultColor(themeType, "#EE02BE", "#C300B3", "#C300B3"));
    }

    public int getCommentVerticalBarColor3(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_3,
                getDefaultColor(themeType, "#02DFEE", "#00B8DA", "#00B8DA"));
    }

    public int getCommentVerticalBarColor4(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_4,
                getDefaultColor(themeType, "#EED502", "#EDCA00", "#EDCA00"));
    }

    public int getCommentVerticalBarColor5(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_5,
                getDefaultColor(themeType, "#EE0220", "#EE0219", "#EE0219"));
    }

    public int getCommentVerticalBarColor6(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_6,
                getDefaultColor(themeType, "#02EE6E", "#00B925", "#00B925"));
    }

    public int getCommentVerticalBarColor7(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_7,
                getDefaultColor(themeType, "#EE4602", "#EE4602", "#EE4602"));
    }

    public int getFABIconColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.FAB_ICON_COLOR,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getChipTextColor(int themeType) {
        return themeSharedPreferences.getInt(CustomThemeSharedPreferencesUtils.CHIP_TEXT_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }
}
