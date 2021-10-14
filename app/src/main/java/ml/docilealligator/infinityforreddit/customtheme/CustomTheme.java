package ml.docilealligator.infinityforreddit.customtheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

import java.util.ArrayList;

@Entity(tableName = "custom_themes")
public class CustomTheme {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "is_light_theme")
    public boolean isLightTheme;
    @ColumnInfo(name = "is_dark_theme")
    public boolean isDarkTheme;
    @ColumnInfo(name = "is_amoled_theme")
    public boolean isAmoledTheme;
    @ColumnInfo(name = "color_primary")
    public int colorPrimary;
    @ColumnInfo(name = "color_primary_dark")
    public int colorPrimaryDark;
    @ColumnInfo(name = "color_accent")
    public int colorAccent;
    @ColumnInfo(name = "color_primary_light_theme")
    public int colorPrimaryLightTheme;
    @ColumnInfo(name = "primary_text_color")
    public int primaryTextColor;
    @ColumnInfo(name = "secondary_text_color")
    public int secondaryTextColor;
    @ColumnInfo(name = "post_title_color")
    public int postTitleColor;
    @ColumnInfo(name = "post_content_color")
    public int postContentColor;
    @ColumnInfo(name = "read_post_title_color")
    public int readPostTitleColor;
    @ColumnInfo(name = "read_post_content_color")
    public int readPostContentColor;
    @ColumnInfo(name = "comment_color")
    public int commentColor;
    @ColumnInfo(name = "button_text_color")
    public int buttonTextColor;
    @ColumnInfo(name = "background_color")
    public int backgroundColor;
    @ColumnInfo(name = "card_view_background_color")
    public int cardViewBackgroundColor;
    @ColumnInfo(name = "read_post_card_view_background_color")
    public int readPostCardViewBackgroundColor;
    @ColumnInfo(name = "comment_background_color")
    public int commentBackgroundColor;
    @ColumnInfo(name = "bottom_app_bar_background_color")
    public int bottomAppBarBackgroundColor;
    @ColumnInfo(name = "primary_icon_color")
    public int primaryIconColor;
    @ColumnInfo(name = "bottom_app_bar_icon_color")
    public int bottomAppBarIconColor;
    @ColumnInfo(name = "post_icon_and_info_color")
    public int postIconAndInfoColor;
    @ColumnInfo(name = "comment_icon_and_info_color")
    public int commentIconAndInfoColor;
    @ColumnInfo(name = "toolbar_primary_text_and_icon_color")
    public int toolbarPrimaryTextAndIconColor;
    @ColumnInfo(name = "toolbar_secondary_text_color")
    public int toolbarSecondaryTextColor;
    @ColumnInfo(name = "circular_progress_bar_background")
    public int circularProgressBarBackground;
    @ColumnInfo(name = "media_indicator_icon_color")
    public int mediaIndicatorIconColor;
    @ColumnInfo(name = "media_indicator_background_color")
    public int mediaIndicatorBackgroundColor;
    @ColumnInfo(name = "tab_layout_with_expanded_collapsing_toolbar_tab_background")
    public int tabLayoutWithExpandedCollapsingToolbarTabBackground;
    @ColumnInfo(name = "tab_layout_with_expanded_collapsing_toolbar_text_color")
    public int tabLayoutWithExpandedCollapsingToolbarTextColor;
    @ColumnInfo(name = "tab_layout_with_expanded_collapsing_toolbar_tab_indicator")
    public int tabLayoutWithExpandedCollapsingToolbarTabIndicator;
    @ColumnInfo(name = "tab_layout_with_collapsed_collapsing_toolbar_tab_background")
    public int tabLayoutWithCollapsedCollapsingToolbarTabBackground;
    @ColumnInfo(name = "tab_layout_with_collapsed_collapsing_toolbar_text_color")
    public int tabLayoutWithCollapsedCollapsingToolbarTextColor;
    @ColumnInfo(name = "tab_layout_with_collapsed_collapsing_toolbar_tab_indicator")
    public int tabLayoutWithCollapsedCollapsingToolbarTabIndicator;
    @ColumnInfo(name = "nav_bar_color")
    public int navBarColor;
    @ColumnInfo(name = "upvoted")
    public int upvoted;
    @ColumnInfo(name = "downvoted")
    public int downvoted;
    @ColumnInfo(name = "post_type_background_color")
    public int postTypeBackgroundColor;
    @ColumnInfo(name = "post_type_text_color")
    public int postTypeTextColor;
    @ColumnInfo(name = "spoiler_background_color")
    public int spoilerBackgroundColor;
    @ColumnInfo(name = "spoiler_text_color")
    public int spoilerTextColor;
    @ColumnInfo(name = "nsfw_background_color")
    public int nsfwBackgroundColor;
    @ColumnInfo(name = "nsfw_text_color")
    public int nsfwTextColor;
    @ColumnInfo(name = "flair_background_color")
    public int flairBackgroundColor;
    @ColumnInfo(name = "flair_text_color")
    public int flairTextColor;
    @ColumnInfo(name = "awards_background_color")
    public int awardsBackgroundColor;
    @ColumnInfo(name = "awards_text_color")
    public int awardsTextColor;
    @ColumnInfo(name = "archived_tint")
    public int archivedTint;
    @ColumnInfo(name = "locked_icon_tint")
    public int lockedIconTint;
    @ColumnInfo(name = "crosspost_icon_tint")
    public int crosspostIconTint;
    @ColumnInfo(name = "upvote_ratio_icon_tint")
    public int upvoteRatioIconTint;
    @ColumnInfo(name = "stickied_post_icon_tint")
    public int stickiedPostIconTint;
    @ColumnInfo(name = "no_preview_post_type_icon_tint")
    public int noPreviewPostTypeIconTint;
    @ColumnInfo(name = "subscribed")
    public int subscribed;
    @ColumnInfo(name = "unsubscribed")
    public int unsubscribed;
    @ColumnInfo(name = "username")
    public int username;
    @ColumnInfo(name = "subreddit")
    public int subreddit;
    @ColumnInfo(name = "author_flair_text_color")
    public int authorFlairTextColor;
    @ColumnInfo(name = "submitter")
    public int submitter;
    @ColumnInfo(name = "moderator")
    public int moderator;
    @ColumnInfo(name = "current_user")
    public int currentUser;
    @ColumnInfo(name = "single_comment_thread_background_color")
    public int singleCommentThreadBackgroundColor;
    @ColumnInfo(name = "unread_message_background_color")
    public int unreadMessageBackgroundColor;
    @ColumnInfo(name = "divider_color")
    public int dividerColor;
    @ColumnInfo(name = "no_preview_link_background_color")
    public int noPreviewPostTypeBackgroundColor;
    @ColumnInfo(name = "vote_and_reply_unavailable_button_color")
    public int voteAndReplyUnavailableButtonColor;
    @ColumnInfo(name = "comment_vertical_bar_color_1")
    public int commentVerticalBarColor1;
    @ColumnInfo(name = "comment_vertical_bar_color_2")
    public int commentVerticalBarColor2;
    @ColumnInfo(name = "comment_vertical_bar_color_3")
    public int commentVerticalBarColor3;
    @ColumnInfo(name = "comment_vertical_bar_color_4")
    public int commentVerticalBarColor4;
    @ColumnInfo(name = "comment_vertical_bar_color_5")
    public int commentVerticalBarColor5;
    @ColumnInfo(name = "comment_vertical_bar_color_6")
    public int commentVerticalBarColor6;
    @ColumnInfo(name = "comment_vertical_bar_color_7")
    public int commentVerticalBarColor7;
    @ColumnInfo(name = "fab_icon_color")
    public int fabIconColor;
    @ColumnInfo(name = "chip_text_color")
    public int chipTextColor;
    @ColumnInfo(name = "link_color")
    public int linkColor;
    @ColumnInfo(name = "received_message_text_color")
    public int receivedMessageTextColor;
    @ColumnInfo(name = "sent_message_text_color")
    public int sentMessageTextColor;
    @ColumnInfo(name = "received_message_background_color")
    public int receivedMessageBackgroundColor;
    @ColumnInfo(name = "sent_message_background_color")
    public int sentMessageBackgroundColor;
    @ColumnInfo(name = "send_message_icon_color")
    public int sendMessageIconColor;
    @ColumnInfo(name = "fully_collapsed_comment_background_color")
    public int fullyCollapsedCommentBackgroundColor;
    @ColumnInfo(name = "awarded_comment_background_color")
    public int awardedCommentBackgroundColor;
    @ColumnInfo(name = "is_light_status_bar")
    public boolean isLightStatusBar;
    @ColumnInfo(name = "is_light_nav_bar")
    public boolean isLightNavBar;
    @ColumnInfo(name = "is_change_status_bar_icon_color_after_toolbar_collapsed_in_immersive_interface")
    public boolean isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface;

    public CustomTheme() {}

    public CustomTheme(@NonNull String name) {
        this.name = name;
    }

    public String getJSONModel() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static CustomTheme convertSettingsItemsToCustomTheme(ArrayList<CustomThemeSettingsItem> customThemeSettingsItems, String themeName) {
        CustomTheme customTheme = new CustomTheme(themeName);

        if (customThemeSettingsItems.isEmpty()) {
            return customTheme;
        }

        customTheme.isLightTheme = customThemeSettingsItems.get(0).isEnabled;
        customTheme.isDarkTheme = customThemeSettingsItems.get(1).isEnabled;
        customTheme.isAmoledTheme = customThemeSettingsItems.get(2).isEnabled;
        customTheme.colorPrimary = customThemeSettingsItems.get(3).colorValue;
        customTheme.colorPrimaryDark = customThemeSettingsItems.get(4).colorValue;
        customTheme.colorAccent = customThemeSettingsItems.get(5).colorValue;
        customTheme.colorPrimaryLightTheme = customThemeSettingsItems.get(6).colorValue;
        customTheme.primaryTextColor = customThemeSettingsItems.get(7).colorValue;
        customTheme.secondaryTextColor = customThemeSettingsItems.get(8).colorValue;
        customTheme.postTitleColor = customThemeSettingsItems.get(9).colorValue;
        customTheme.postContentColor = customThemeSettingsItems.get(10).colorValue;
        customTheme.readPostTitleColor = customThemeSettingsItems.get(11).colorValue;
        customTheme.readPostContentColor = customThemeSettingsItems.get(12).colorValue;
        customTheme.commentColor = customThemeSettingsItems.get(13).colorValue;
        customTheme.buttonTextColor = customThemeSettingsItems.get(14).colorValue;
        customTheme.chipTextColor = customThemeSettingsItems.get(15).colorValue;
        customTheme.linkColor = customThemeSettingsItems.get(16).colorValue;
        customTheme.receivedMessageTextColor = customThemeSettingsItems.get(17).colorValue;
        customTheme.sentMessageTextColor = customThemeSettingsItems.get(18).colorValue;
        customTheme.backgroundColor = customThemeSettingsItems.get(19).colorValue;
        customTheme.cardViewBackgroundColor = customThemeSettingsItems.get(20).colorValue;
        customTheme.readPostCardViewBackgroundColor = customThemeSettingsItems.get(21).colorValue;
        customTheme.commentBackgroundColor = customThemeSettingsItems.get(22).colorValue;
        customTheme.fullyCollapsedCommentBackgroundColor = customThemeSettingsItems.get(23).colorValue;
        customTheme.awardedCommentBackgroundColor = customThemeSettingsItems.get(24).colorValue;
        customTheme.receivedMessageBackgroundColor = customThemeSettingsItems.get(25).colorValue;
        customTheme.sentMessageBackgroundColor = customThemeSettingsItems.get(26).colorValue;
        customTheme.bottomAppBarBackgroundColor = customThemeSettingsItems.get(27).colorValue;
        customTheme.primaryIconColor = customThemeSettingsItems.get(28).colorValue;
        customTheme.bottomAppBarIconColor = customThemeSettingsItems.get(29).colorValue;
        customTheme.postIconAndInfoColor = customThemeSettingsItems.get(30).colorValue;
        customTheme.commentIconAndInfoColor = customThemeSettingsItems.get(31).colorValue;
        customTheme.fabIconColor = customThemeSettingsItems.get(32).colorValue;
        customTheme.sendMessageIconColor = customThemeSettingsItems.get(33).colorValue;
        customTheme.toolbarPrimaryTextAndIconColor = customThemeSettingsItems.get(34).colorValue;
        customTheme.toolbarSecondaryTextColor = customThemeSettingsItems.get(35).colorValue;
        customTheme.circularProgressBarBackground = customThemeSettingsItems.get(36).colorValue;
        customTheme.mediaIndicatorIconColor = customThemeSettingsItems.get(37).colorValue;
        customTheme.mediaIndicatorBackgroundColor = customThemeSettingsItems.get(38).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground = customThemeSettingsItems.get(39).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor = customThemeSettingsItems.get(40).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator = customThemeSettingsItems.get(41).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground = customThemeSettingsItems.get(42).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor = customThemeSettingsItems.get(43).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator = customThemeSettingsItems.get(44).colorValue;
        customTheme.upvoted = customThemeSettingsItems.get(45).colorValue;
        customTheme.downvoted = customThemeSettingsItems.get(46).colorValue;
        customTheme.postTypeBackgroundColor = customThemeSettingsItems.get(47).colorValue;
        customTheme.postTypeTextColor = customThemeSettingsItems.get(48).colorValue;
        customTheme.spoilerBackgroundColor = customThemeSettingsItems.get(49).colorValue;
        customTheme.spoilerTextColor = customThemeSettingsItems.get(50).colorValue;
        customTheme.nsfwBackgroundColor = customThemeSettingsItems.get(51).colorValue;
        customTheme.nsfwTextColor = customThemeSettingsItems.get(52).colorValue;
        customTheme.flairBackgroundColor = customThemeSettingsItems.get(53).colorValue;
        customTheme.flairTextColor = customThemeSettingsItems.get(54).colorValue;
        customTheme.awardsBackgroundColor = customThemeSettingsItems.get(55).colorValue;
        customTheme.awardsTextColor = customThemeSettingsItems.get(56).colorValue;
        customTheme.archivedTint = customThemeSettingsItems.get(57).colorValue;
        customTheme.lockedIconTint = customThemeSettingsItems.get(58).colorValue;
        customTheme.crosspostIconTint = customThemeSettingsItems.get(59).colorValue;
        customTheme.upvoteRatioIconTint = customThemeSettingsItems.get(60).colorValue;
        customTheme.stickiedPostIconTint = customThemeSettingsItems.get(61).colorValue;
        customTheme.noPreviewPostTypeIconTint = customThemeSettingsItems.get(62).colorValue;
        customTheme.subscribed = customThemeSettingsItems.get(63).colorValue;
        customTheme.unsubscribed = customThemeSettingsItems.get(64).colorValue;
        customTheme.username = customThemeSettingsItems.get(65).colorValue;
        customTheme.subreddit = customThemeSettingsItems.get(66).colorValue;
        customTheme.authorFlairTextColor = customThemeSettingsItems.get(67).colorValue;
        customTheme.submitter = customThemeSettingsItems.get(68).colorValue;
        customTheme.moderator = customThemeSettingsItems.get(69).colorValue;
        customTheme.currentUser = customThemeSettingsItems.get(70).colorValue;
        customTheme.singleCommentThreadBackgroundColor = customThemeSettingsItems.get(71).colorValue;
        customTheme.unreadMessageBackgroundColor = customThemeSettingsItems.get(72).colorValue;
        customTheme.dividerColor = customThemeSettingsItems.get(73).colorValue;
        customTheme.noPreviewPostTypeBackgroundColor = customThemeSettingsItems.get(74).colorValue;
        customTheme.voteAndReplyUnavailableButtonColor = customThemeSettingsItems.get(75).colorValue;
        customTheme.commentVerticalBarColor1 = customThemeSettingsItems.get(76).colorValue;
        customTheme.commentVerticalBarColor2 = customThemeSettingsItems.get(77).colorValue;
        customTheme.commentVerticalBarColor3 = customThemeSettingsItems.get(78).colorValue;
        customTheme.commentVerticalBarColor4 = customThemeSettingsItems.get(79).colorValue;
        customTheme.commentVerticalBarColor5 = customThemeSettingsItems.get(80).colorValue;
        customTheme.commentVerticalBarColor6 = customThemeSettingsItems.get(81).colorValue;
        customTheme.commentVerticalBarColor7 = customThemeSettingsItems.get(82).colorValue;
        customTheme.navBarColor = customThemeSettingsItems.get(83).colorValue;
        customTheme.isLightStatusBar = customThemeSettingsItems.get(84).isEnabled;
        customTheme.isLightNavBar = customThemeSettingsItems.get(85).isEnabled;
        customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = customThemeSettingsItems.get(86).isEnabled;

        return customTheme;
    }
}
