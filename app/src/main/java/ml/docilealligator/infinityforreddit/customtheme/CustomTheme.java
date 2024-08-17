package ml.docilealligator.infinityforreddit.customtheme;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

@Entity(tableName = "custom_themes")
public class CustomTheme implements Parcelable {
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
    @ColumnInfo(name = "filled_card_view_background_color")
    public int filledCardViewBackgroundColor;
    @ColumnInfo(name = "read_post_filled_card_view_background_color")
    public int readPostFilledCardViewBackgroundColor;
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

    protected CustomTheme(Parcel in) {
        name = in.readString();
        isLightTheme = in.readByte() != 0;
        isDarkTheme = in.readByte() != 0;
        isAmoledTheme = in.readByte() != 0;
        colorPrimary = in.readInt();
        colorPrimaryDark = in.readInt();
        colorAccent = in.readInt();
        colorPrimaryLightTheme = in.readInt();
        primaryTextColor = in.readInt();
        secondaryTextColor = in.readInt();
        postTitleColor = in.readInt();
        postContentColor = in.readInt();
        readPostTitleColor = in.readInt();
        readPostContentColor = in.readInt();
        commentColor = in.readInt();
        buttonTextColor = in.readInt();
        backgroundColor = in.readInt();
        cardViewBackgroundColor = in.readInt();
        readPostCardViewBackgroundColor = in.readInt();
        filledCardViewBackgroundColor = in.readInt();
        readPostFilledCardViewBackgroundColor = in.readInt();
        commentBackgroundColor = in.readInt();
        bottomAppBarBackgroundColor = in.readInt();
        primaryIconColor = in.readInt();
        bottomAppBarIconColor = in.readInt();
        postIconAndInfoColor = in.readInt();
        commentIconAndInfoColor = in.readInt();
        toolbarPrimaryTextAndIconColor = in.readInt();
        toolbarSecondaryTextColor = in.readInt();
        circularProgressBarBackground = in.readInt();
        mediaIndicatorIconColor = in.readInt();
        mediaIndicatorBackgroundColor = in.readInt();
        tabLayoutWithExpandedCollapsingToolbarTabBackground = in.readInt();
        tabLayoutWithExpandedCollapsingToolbarTextColor = in.readInt();
        tabLayoutWithExpandedCollapsingToolbarTabIndicator = in.readInt();
        tabLayoutWithCollapsedCollapsingToolbarTabBackground = in.readInt();
        tabLayoutWithCollapsedCollapsingToolbarTextColor = in.readInt();
        tabLayoutWithCollapsedCollapsingToolbarTabIndicator = in.readInt();
        navBarColor = in.readInt();
        upvoted = in.readInt();
        downvoted = in.readInt();
        postTypeBackgroundColor = in.readInt();
        postTypeTextColor = in.readInt();
        spoilerBackgroundColor = in.readInt();
        spoilerTextColor = in.readInt();
        nsfwBackgroundColor = in.readInt();
        nsfwTextColor = in.readInt();
        flairBackgroundColor = in.readInt();
        flairTextColor = in.readInt();
        awardsBackgroundColor = in.readInt();
        awardsTextColor = in.readInt();
        archivedTint = in.readInt();
        lockedIconTint = in.readInt();
        crosspostIconTint = in.readInt();
        upvoteRatioIconTint = in.readInt();
        stickiedPostIconTint = in.readInt();
        noPreviewPostTypeIconTint = in.readInt();
        subscribed = in.readInt();
        unsubscribed = in.readInt();
        username = in.readInt();
        subreddit = in.readInt();
        authorFlairTextColor = in.readInt();
        submitter = in.readInt();
        moderator = in.readInt();
        currentUser = in.readInt();
        singleCommentThreadBackgroundColor = in.readInt();
        unreadMessageBackgroundColor = in.readInt();
        dividerColor = in.readInt();
        noPreviewPostTypeBackgroundColor = in.readInt();
        voteAndReplyUnavailableButtonColor = in.readInt();
        commentVerticalBarColor1 = in.readInt();
        commentVerticalBarColor2 = in.readInt();
        commentVerticalBarColor3 = in.readInt();
        commentVerticalBarColor4 = in.readInt();
        commentVerticalBarColor5 = in.readInt();
        commentVerticalBarColor6 = in.readInt();
        commentVerticalBarColor7 = in.readInt();
        fabIconColor = in.readInt();
        chipTextColor = in.readInt();
        linkColor = in.readInt();
        receivedMessageTextColor = in.readInt();
        sentMessageTextColor = in.readInt();
        receivedMessageBackgroundColor = in.readInt();
        sentMessageBackgroundColor = in.readInt();
        sendMessageIconColor = in.readInt();
        fullyCollapsedCommentBackgroundColor = in.readInt();
        awardedCommentBackgroundColor = in.readInt();
        isLightStatusBar = in.readByte() != 0;
        isLightNavBar = in.readByte() != 0;
        isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = in.readByte() != 0;
    }

    public static final Creator<CustomTheme> CREATOR = new Creator<CustomTheme>() {
        @Override
        public CustomTheme createFromParcel(Parcel in) {
            return new CustomTheme(in);
        }

        @Override
        public CustomTheme[] newArray(int size) {
            return new CustomTheme[size];
        }
    };

    public String getJSONModel() {
        Gson gson = getGsonBuilder().create();
        return gson.toJson(this);
    }

    private static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CustomTheme.class, new CustomThemeSerializer());
        builder.registerTypeAdapter(CustomTheme.class, new CustomThemeDeserializer());
        return builder;
    }

    public static CustomTheme fromJson(String json) throws JsonParseException {
        Gson gson = getGsonBuilder().create();
        return gson.fromJson(json, CustomTheme.class);
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
        customTheme.filledCardViewBackgroundColor = customThemeSettingsItems.get(22).colorValue;
        customTheme.readPostFilledCardViewBackgroundColor = customThemeSettingsItems.get(23).colorValue;
        customTheme.commentBackgroundColor = customThemeSettingsItems.get(24).colorValue;
        customTheme.fullyCollapsedCommentBackgroundColor = customThemeSettingsItems.get(25).colorValue;
        customTheme.awardedCommentBackgroundColor = customThemeSettingsItems.get(26).colorValue;
        customTheme.receivedMessageBackgroundColor = customThemeSettingsItems.get(27).colorValue;
        customTheme.sentMessageBackgroundColor = customThemeSettingsItems.get(28).colorValue;
        customTheme.bottomAppBarBackgroundColor = customThemeSettingsItems.get(29).colorValue;
        customTheme.primaryIconColor = customThemeSettingsItems.get(30).colorValue;
        customTheme.bottomAppBarIconColor = customThemeSettingsItems.get(31).colorValue;
        customTheme.postIconAndInfoColor = customThemeSettingsItems.get(32).colorValue;
        customTheme.commentIconAndInfoColor = customThemeSettingsItems.get(33).colorValue;
        customTheme.fabIconColor = customThemeSettingsItems.get(34).colorValue;
        customTheme.sendMessageIconColor = customThemeSettingsItems.get(35).colorValue;
        customTheme.toolbarPrimaryTextAndIconColor = customThemeSettingsItems.get(36).colorValue;
        customTheme.toolbarSecondaryTextColor = customThemeSettingsItems.get(37).colorValue;
        customTheme.circularProgressBarBackground = customThemeSettingsItems.get(38).colorValue;
        customTheme.mediaIndicatorIconColor = customThemeSettingsItems.get(39).colorValue;
        customTheme.mediaIndicatorBackgroundColor = customThemeSettingsItems.get(40).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground = customThemeSettingsItems.get(41).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor = customThemeSettingsItems.get(42).colorValue;
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator = customThemeSettingsItems.get(43).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground = customThemeSettingsItems.get(44).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor = customThemeSettingsItems.get(45).colorValue;
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator = customThemeSettingsItems.get(46).colorValue;
        customTheme.upvoted = customThemeSettingsItems.get(47).colorValue;
        customTheme.downvoted = customThemeSettingsItems.get(48).colorValue;
        customTheme.postTypeBackgroundColor = customThemeSettingsItems.get(49).colorValue;
        customTheme.postTypeTextColor = customThemeSettingsItems.get(50).colorValue;
        customTheme.spoilerBackgroundColor = customThemeSettingsItems.get(51).colorValue;
        customTheme.spoilerTextColor = customThemeSettingsItems.get(52).colorValue;
        customTheme.nsfwBackgroundColor = customThemeSettingsItems.get(53).colorValue;
        customTheme.nsfwTextColor = customThemeSettingsItems.get(54).colorValue;
        customTheme.flairBackgroundColor = customThemeSettingsItems.get(55).colorValue;
        customTheme.flairTextColor = customThemeSettingsItems.get(56).colorValue;
        customTheme.awardsBackgroundColor = customThemeSettingsItems.get(57).colorValue;
        customTheme.awardsTextColor = customThemeSettingsItems.get(58).colorValue;
        customTheme.archivedTint = customThemeSettingsItems.get(59).colorValue;
        customTheme.lockedIconTint = customThemeSettingsItems.get(60).colorValue;
        customTheme.crosspostIconTint = customThemeSettingsItems.get(61).colorValue;
        customTheme.upvoteRatioIconTint = customThemeSettingsItems.get(62).colorValue;
        customTheme.stickiedPostIconTint = customThemeSettingsItems.get(63).colorValue;
        customTheme.noPreviewPostTypeIconTint = customThemeSettingsItems.get(64).colorValue;
        customTheme.subscribed = customThemeSettingsItems.get(65).colorValue;
        customTheme.unsubscribed = customThemeSettingsItems.get(66).colorValue;
        customTheme.username = customThemeSettingsItems.get(67).colorValue;
        customTheme.subreddit = customThemeSettingsItems.get(68).colorValue;
        customTheme.authorFlairTextColor = customThemeSettingsItems.get(69).colorValue;
        customTheme.submitter = customThemeSettingsItems.get(70).colorValue;
        customTheme.moderator = customThemeSettingsItems.get(71).colorValue;
        customTheme.currentUser = customThemeSettingsItems.get(72).colorValue;
        customTheme.singleCommentThreadBackgroundColor = customThemeSettingsItems.get(73).colorValue;
        customTheme.unreadMessageBackgroundColor = customThemeSettingsItems.get(74).colorValue;
        customTheme.dividerColor = customThemeSettingsItems.get(75).colorValue;
        customTheme.noPreviewPostTypeBackgroundColor = customThemeSettingsItems.get(76).colorValue;
        customTheme.voteAndReplyUnavailableButtonColor = customThemeSettingsItems.get(77).colorValue;
        customTheme.commentVerticalBarColor1 = customThemeSettingsItems.get(78).colorValue;
        customTheme.commentVerticalBarColor2 = customThemeSettingsItems.get(79).colorValue;
        customTheme.commentVerticalBarColor3 = customThemeSettingsItems.get(80).colorValue;
        customTheme.commentVerticalBarColor4 = customThemeSettingsItems.get(81).colorValue;
        customTheme.commentVerticalBarColor5 = customThemeSettingsItems.get(82).colorValue;
        customTheme.commentVerticalBarColor6 = customThemeSettingsItems.get(83).colorValue;
        customTheme.commentVerticalBarColor7 = customThemeSettingsItems.get(84).colorValue;
        customTheme.navBarColor = customThemeSettingsItems.get(85).colorValue;
        customTheme.isLightStatusBar = customThemeSettingsItems.get(86).isEnabled;
        customTheme.isLightNavBar = customThemeSettingsItems.get(87).isEnabled;
        customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = customThemeSettingsItems.get(88).isEnabled;

        return customTheme;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (isLightTheme ? 1 : 0));
        dest.writeByte((byte) (isDarkTheme ? 1 : 0));
        dest.writeByte((byte) (isAmoledTheme ? 1 : 0));
        dest.writeInt(colorPrimary);
        dest.writeInt(colorPrimaryDark);
        dest.writeInt(colorAccent);
        dest.writeInt(colorPrimaryLightTheme);
        dest.writeInt(primaryTextColor);
        dest.writeInt(secondaryTextColor);
        dest.writeInt(postTitleColor);
        dest.writeInt(postContentColor);
        dest.writeInt(readPostTitleColor);
        dest.writeInt(readPostContentColor);
        dest.writeInt(commentColor);
        dest.writeInt(buttonTextColor);
        dest.writeInt(backgroundColor);
        dest.writeInt(cardViewBackgroundColor);
        dest.writeInt(readPostCardViewBackgroundColor);
        dest.writeInt(filledCardViewBackgroundColor);
        dest.writeInt(readPostFilledCardViewBackgroundColor);
        dest.writeInt(commentBackgroundColor);
        dest.writeInt(bottomAppBarBackgroundColor);
        dest.writeInt(primaryIconColor);
        dest.writeInt(bottomAppBarIconColor);
        dest.writeInt(postIconAndInfoColor);
        dest.writeInt(commentIconAndInfoColor);
        dest.writeInt(toolbarPrimaryTextAndIconColor);
        dest.writeInt(toolbarSecondaryTextColor);
        dest.writeInt(circularProgressBarBackground);
        dest.writeInt(mediaIndicatorIconColor);
        dest.writeInt(mediaIndicatorBackgroundColor);
        dest.writeInt(tabLayoutWithExpandedCollapsingToolbarTabBackground);
        dest.writeInt(tabLayoutWithExpandedCollapsingToolbarTextColor);
        dest.writeInt(tabLayoutWithExpandedCollapsingToolbarTabIndicator);
        dest.writeInt(tabLayoutWithCollapsedCollapsingToolbarTabBackground);
        dest.writeInt(tabLayoutWithCollapsedCollapsingToolbarTextColor);
        dest.writeInt(tabLayoutWithCollapsedCollapsingToolbarTabIndicator);
        dest.writeInt(navBarColor);
        dest.writeInt(upvoted);
        dest.writeInt(downvoted);
        dest.writeInt(postTypeBackgroundColor);
        dest.writeInt(postTypeTextColor);
        dest.writeInt(spoilerBackgroundColor);
        dest.writeInt(spoilerTextColor);
        dest.writeInt(nsfwBackgroundColor);
        dest.writeInt(nsfwTextColor);
        dest.writeInt(flairBackgroundColor);
        dest.writeInt(flairTextColor);
        dest.writeInt(awardsBackgroundColor);
        dest.writeInt(awardsTextColor);
        dest.writeInt(archivedTint);
        dest.writeInt(lockedIconTint);
        dest.writeInt(crosspostIconTint);
        dest.writeInt(upvoteRatioIconTint);
        dest.writeInt(stickiedPostIconTint);
        dest.writeInt(noPreviewPostTypeIconTint);
        dest.writeInt(subscribed);
        dest.writeInt(unsubscribed);
        dest.writeInt(username);
        dest.writeInt(subreddit);
        dest.writeInt(authorFlairTextColor);
        dest.writeInt(submitter);
        dest.writeInt(moderator);
        dest.writeInt(currentUser);
        dest.writeInt(singleCommentThreadBackgroundColor);
        dest.writeInt(unreadMessageBackgroundColor);
        dest.writeInt(dividerColor);
        dest.writeInt(noPreviewPostTypeBackgroundColor);
        dest.writeInt(voteAndReplyUnavailableButtonColor);
        dest.writeInt(commentVerticalBarColor1);
        dest.writeInt(commentVerticalBarColor2);
        dest.writeInt(commentVerticalBarColor3);
        dest.writeInt(commentVerticalBarColor4);
        dest.writeInt(commentVerticalBarColor5);
        dest.writeInt(commentVerticalBarColor6);
        dest.writeInt(commentVerticalBarColor7);
        dest.writeInt(fabIconColor);
        dest.writeInt(chipTextColor);
        dest.writeInt(linkColor);
        dest.writeInt(receivedMessageTextColor);
        dest.writeInt(sentMessageTextColor);
        dest.writeInt(receivedMessageBackgroundColor);
        dest.writeInt(sentMessageBackgroundColor);
        dest.writeInt(sendMessageIconColor);
        dest.writeInt(fullyCollapsedCommentBackgroundColor);
        dest.writeInt(awardedCommentBackgroundColor);
        dest.writeByte((byte) (isLightStatusBar ? 1 : 0));
        dest.writeByte((byte) (isLightNavBar ? 1 : 0));
        dest.writeByte((byte) (isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface ? 1 : 0));
    }

    private static class CustomThemeSerializer implements JsonSerializer<CustomTheme> {
        @Override
        public JsonElement serialize(CustomTheme src, Type typeofSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();

            for (Field field : src.getClass().getDeclaredFields()) {
                try {
                    if (field.getType() == int.class) {
                        obj.addProperty(field.getName(), String.format("#%08X", field.getInt(src)));
                    } else {
                        obj.add(field.getName(), context.serialize(field.get(src)));
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            return obj;
        }
    }

    private static class CustomThemeDeserializer implements JsonDeserializer<CustomTheme> {
        @Override
        public CustomTheme deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            CustomTheme customTheme = new CustomTheme();

            JsonObject obj = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {

                Field field;
                try {
                    field = customTheme.getClass().getDeclaredField(entry.getKey());
                } catch (NoSuchFieldException e) {
                    // Field not found, skip
                    continue;
                }

                JsonElement value = entry.getValue();

                try {
                    Class<?> type = field.getType();
                    if (int.class.equals(type)) {
                        if (value.getAsJsonPrimitive().isString()) {
                            // Hex or text color string
                            field.set(customTheme, Color.parseColor(value.getAsString()));
                        } else {
                            // Int color
                            field.set(customTheme, value.getAsInt());
                        }
                    } else if (String.class.equals(type)) {
                        field.set(customTheme, value.getAsString());
                    } else if (boolean.class.equals(type)) {
                        field.set(customTheme, value.getAsBoolean());
                    }

                } catch (IllegalAccessException e) {
                    throw new JsonParseException("Failed to access theme field.");
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException("Invalid color string.");
                }

            }
            return customTheme;
        }
    }
}
