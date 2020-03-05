package ml.docilealligator.infinityforreddit.CustomTheme;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_themes")
public class CustomTheme {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
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
    @ColumnInfo(name = "background_color")
    public int backgroundColor;
    @ColumnInfo(name = "rounded_bottom_sheet_primary_background")
    public int roundedBottomSheetPrimaryBackground;
    @ColumnInfo(name = "card_view_background_color")
    public int cardViewBackgroundColor;
    @ColumnInfo(name = "toolbar_primary_text_and_icon_color")
    public int toolbarPrimaryTextAndIconColor;
    @ColumnInfo(name = "toolbar_and_tab_background_color")
    public int toolbarAndTabBackgroundColor;
    @ColumnInfo(name = "circular_progress_bar_background")
    public int circularProgressBarBackground;
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
    @ColumnInfo(name = "post_type")
    public int postType;
    @ColumnInfo(name = "spoiler_color")
    public int spoilerColor;
    @ColumnInfo(name = "nsfw_color")
    public int nsfwColor;
    @ColumnInfo(name = "flair_color")
    public int flairColor;
    @ColumnInfo(name = "archived_tint")
    public int archivedTint;
    @ColumnInfo(name = "locked_icon_tint")
    public int lockedIconTint;
    @ColumnInfo(name = "crosspost")
    public int crosspost;
    @ColumnInfo(name = "stickied_post")
    public int stickiedPost;
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
    @ColumnInfo(name = "notification_icon_color")
    public int notificationIconColor;
    @ColumnInfo(name = "single_comment_thread_background_color")
    public int singleCommentThreadBackgroundColor;
    @ColumnInfo(name = "unread_message_background_color")
    public int unreadMessageBackgroundColor;
    @ColumnInfo(name = "divider_color")
    public int dividerColor;
    @ColumnInfo(name = "no_preview_link_background_color")
    public int noPreviewLinkBackgroundColor;
    @ColumnInfo(name = "vote_and_reply_unavailable_vote_button_color")
    public int voteAndReplyUnavailableVoteButtonColor;
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

    public CustomTheme(@NonNull String name) {
        this.name = name;
    }
}
