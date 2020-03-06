package ml.docilealligator.infinityforreddit.CustomTheme;

import android.content.SharedPreferences;
import android.graphics.Color;

import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

public class CustomThemeWrapper {
    public static final int NORMAL = 0;
    public static final int DARK = 1;
    public static final int AMOLED_DARK = 2;

    public int colorPrimary;
    public int colorPrimaryDark;
    public int colorAccent;
    public int colorPrimaryLightTheme;
    public int primaryTextColor;
    public int secondaryTextColor;
    public int backgroundColor;
    public int roundedBottomSheetPrimaryBackground;
    public int cardViewBackgroundColor;
    public int toolbarPrimaryTextAndIconColor;
    public int toolbarAndTabBackgroundColor;
    public int circularProgressBarBackground;
    public int tabLayoutWithExpandedCollapsingToolbarTabBackground;
    public int tabLayoutWithExpandedCollapsingToolbarTextColor;
    public int tabLayoutWithExpandedCollapsingToolbarTabIndicator;
    public int tabLayoutWithCollapsedCollapsingToolbarTabBackground;
    public int tabLayoutWithCollapsedCollapsingToolbarTextColor;
    public int tabLayoutWithCollapsedCollapsingToolbarTabIndicator;
    public int navBarColor;
    public int upvoted;
    public int downvoted;
    public int postType;
    public int spoilerColor;
    public int nsfwColor;
    public int flairColor;
    public int archivedTint;
    public int lockedIconTint;
    public int crosspost;
    public int stickiedPost;
    public int subscribed;
    public int unsubscribed;
    public int username;
    public int subreddit;
    public int authorFlairTextColor;
    public int submitter;
    public int moderator;
    public int notificationIconColor;
    public int singleCommentThreadBackgroundColor;
    public int unreadMessageBackgroundColor;
    public int dividerColor;
    public int noPreviewLinkBackgroundColor;
    public int voteAndReplyUnavailableVoteButtonColor;
    public int commentVerticalBarColor1;
    public int commentVerticalBarColor2;
    public int commentVerticalBarColor3;
    public int commentVerticalBarColor4;
    public int commentVerticalBarColor5;
    public int commentVerticalBarColor6;
    public int commentVerticalBarColor7;

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
    public int getColorPrimary(int themeType) {
        //f
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COLOR_PRIMARY,
                getDefaultColor(themeType, "#1565C0", "#242424", "#000000"));
    }

    public int getColorPrimaryDark(int themeType) {
        //f
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COLOR_PRIMARY_DARK,
                getDefaultColor(themeType, "#0D47A1", "#121212", "#000000"));
    }

    public int getColorAccent(int themeType) {
        //f
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COLOR_ACCENT,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getColorPrimaryLightTheme(int themeType) {
        //f
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getPrimaryTextColor(int themeType) {
        //f
        return themeSharedPreferences.getInt(SharedPreferencesUtils.PRIMARY_TEXT_COLOR,
                getDefaultColor(themeType, "#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getSecondaryTextColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SECONDARY_TEXT_COLOR,
                getDefaultColor(themeType, "#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.BACKGROUND_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#121212", "#000000"));
    }

    public int getRoundedBottomSheetPrimaryBackground(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.ROUNDED_BOTTOM_SHEET_PRIMARY_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#121212", "#000000"));
    }

    public int getCardViewBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.CARD_VIEW_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getToolbarPrimaryTextAndIconColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TOOLBAR_PRIMARY_TEXT_AND_ICON_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getToolbarAndTabBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TOOLBAR_AND_TAB_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#1565C0", "#282828", "#000000"));
    }

    public int getCircularProgressBarBackground(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.CIRCULAR_PROGRESS_BAR_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabBackground(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor(themeType, "#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTextColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor(themeType, "#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabIndicator(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor(themeType, "#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabBackground(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor(themeType, "#1565C0", "#242424", "#000000"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTextColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabIndicator(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor(themeType, "#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNavBarColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.NAV_BAR_COLOR,
                getDefaultColor(themeType, "#FFFFFF", "#121212", "#000000"));
    }

    public int getUpvoted(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.UPVOTED,
                getDefaultColor(themeType, "#E91E63", "#E91E63", "#E91E63"));
    }

    public int getDownvoted(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.DOWNVOTED,
                getDefaultColor(themeType, "#007DDE", "#007DDE", "#007DDE"));
    }

    public int getPostType(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.POST_TYPE,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSpoilerColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SPOILER_COLOR,
                getDefaultColor(themeType, "#EE02EB", "#EE02EB", "#EE02EB"));
    }

    public int getNsfwColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.NSFW_COLOR,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getFlairColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.FLAIR_COLOR,
                getDefaultColor(themeType, "#00AA8C", "#00AA8C", "#00AA8C"));
    }

    public int getArchivedTint(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.ARCHIVED_TINT,
                getDefaultColor(themeType, "#B4009F", "#B4009F", "#B4009F"));
    }

    public int getLockedIconTint(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.LOCKED_ICON_TINT,
                getDefaultColor(themeType, "#EE7302", "#EE7302", "#EE7302"));
    }

    public int getCrosspost(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.CROSSPOST,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getStickiedPost(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.STICKIED_POST,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSubscribed(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SUBSCRIBED,
                getDefaultColor(themeType, "#FF4081", "#FF4081", "#FF4081"));
    }

    public int getUnsubscribed(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.UNSUBSCRIBED,
                getDefaultColor(themeType, "#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getUsername(int themeType) {
        return themeSharedPreferences.getInt(SharedPreferencesUtils.USERNAME,
                getDefaultColor(themeType, "#0D47A1", "#1E88E5", "#1E88E5"));
    }

    public int getSubreddit(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SUBREDDIT,
                getDefaultColor(themeType, "#E91E63", "#E91E63", "#E91E63"));
    }

    public int getAuthorFlairTextColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.AUTHOR_FLAIR_TEXT_COLOR,
                getDefaultColor(themeType, "#EE02C4", "#EE02C4", "#EE02C4"));
    }

    public int getSubmitter(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SUBMITTER,
                getDefaultColor(themeType, "#EE8A02", "#EE8A02", "#EE8A02"));
    }

    public int getModerator(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.MODERATOR,
                getDefaultColor(themeType, "#00BA81", "#00BA81", "#00BA81"));
    }

    public int getNotificationIconColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.NOTIFICATION_ICON_COLOR,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getSingleCommentThreadBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.SINGLE_COMMENT_THREAD_BACKGROUND,
                getDefaultColor(themeType, "#B3E5F9", "#123E77", "#123E77"));
    }

    public int getUnreadMessageBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.UNREAD_MESSAGE_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#B3E5F9", "#123E77", "#123E77"));
    }

    public int getDividerColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.DIVIDER_COLOR,
                getDefaultColor(themeType, "#E0E0E0", "#69666C", "#69666C"));
    }

    public int getNoPreviewLinkBackgroundColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.NO_PREVIEW_LINK_BACKGROUND_COLOR,
                getDefaultColor(themeType, "#E0E0E0", "#424242", "#424242"));
    }

    public int getVoteAndReplyUnavailableVoteButtonColor(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.VOTE_AND_REPLY_UNAVAILABLE_VOTE_BUTTON_COLOR,
                getDefaultColor(themeType, "#F0F0F0", "#3C3C3C", "#3C3C3C"));
    }

    public int getCommentVerticalBarColor1(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_1,
                getDefaultColor(themeType, "#1565C0", "#1565C0", "#1565C0"));
    }

    public int getCommentVerticalBarColor2(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_2,
                getDefaultColor(themeType, "#EE02BE", "#C300B3", "#C300B3"));
    }

    public int getCommentVerticalBarColor3(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_3,
                getDefaultColor(themeType, "#02DFEE", "#00B8DA", "#00B8DA"));
    }

    public int getCommentVerticalBarColor4(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_4,
                getDefaultColor(themeType, "#EED502", "#EDCA00", "#EDCA00"));
    }

    public int getCommentVerticalBarColor5(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_5,
                getDefaultColor(themeType, "#EE0220", "#EE0219", "#EE0219"));
    }

    public int getCommentVerticalBarColor6(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_6,
                getDefaultColor(themeType, "#02EE6E", "#00B925", "#00B925"));
    }

    public int getCommentVerticalBarColor7(int themeType) {
        //F
        return themeSharedPreferences.getInt(SharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_7,
                getDefaultColor(themeType, "#EE4602", "#EE4602", "#EE4602"));
    }
}
