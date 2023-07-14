package ml.docilealligator.infinityforreddit.customtheme;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;

public class CustomThemeSettingsItem implements Parcelable {
    public String itemName;
    public String itemDetails;
    public int colorValue;
    public boolean isEnabled;

    private CustomThemeSettingsItem(String itemName, String itemDetails, int colorValue) {
        this.itemName = itemName;
        this.itemDetails = itemDetails;
        this.colorValue = colorValue;
    }

    private CustomThemeSettingsItem(String itemName, boolean isEnabled) {
        this.itemName = itemName;
        this.isEnabled = isEnabled;
    }

    protected CustomThemeSettingsItem(Parcel in) {
        itemName = in.readString();
        itemDetails = in.readString();
        colorValue = in.readInt();
        isEnabled = in.readByte() != 0;
    }

    public static final Creator<CustomThemeSettingsItem> CREATOR = new Creator<CustomThemeSettingsItem>() {
        @Override
        public CustomThemeSettingsItem createFromParcel(Parcel in) {
            return new CustomThemeSettingsItem(in);
        }

        @Override
        public CustomThemeSettingsItem[] newArray(int size) {
            return new CustomThemeSettingsItem[size];
        }
    };

    public static ArrayList<CustomThemeSettingsItem> convertCustomThemeToSettingsItem(Context context,
                                                                                      CustomTheme customTheme,
                                                                                      int androidVersion) {
        ArrayList<CustomThemeSettingsItem> customThemeSettingsItems = new ArrayList<>();

        if (customTheme == null) {
            return customThemeSettingsItems;
        }

        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_is_light_theme),
                customTheme.isLightTheme
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_is_dark_theme),
                customTheme.isDarkTheme
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_is_amoled_theme),
                customTheme.isAmoledTheme
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_color_primary),
                context.getString(R.string.theme_item_color_primary_detail),
                customTheme.colorPrimary));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_color_primary_dark),
                context.getString(R.string.theme_item_color_primary_dark_detail),
                customTheme.colorPrimaryDark));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_color_accent),
                context.getString(R.string.theme_item_color_accent_detail),
                customTheme.colorAccent));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_color_primary_light_theme),
                context.getString(R.string.theme_item_color_primary_light_theme_detail),
                customTheme.colorPrimaryLightTheme));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_primary_text_color),
                context.getString(R.string.theme_item_primary_text_color_detail),
                customTheme.primaryTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_secondary_text_color),
                context.getString(R.string.theme_item_secondary_text_color_detail),
                customTheme.secondaryTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_post_title_color),
                context.getString(R.string.theme_item_post_title_color_detail),
                customTheme.postTitleColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_post_content_color),
                context.getString(R.string.theme_item_post_content_color_detail),
                customTheme.postContentColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_read_post_title_color),
                context.getString(R.string.theme_item_read_post_title_color_detail),
                customTheme.readPostTitleColor
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_read_post_content_color),
                context.getString(R.string.theme_item_read_post_content_color_detail),
                customTheme.readPostContentColor
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_color),
                context.getString(R.string.theme_item_comment_color_detail),
                customTheme.commentColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_button_text_color),
                context.getString(R.string.theme_item_button_text_color_detail),
                customTheme.buttonTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_chip_text_color),
                context.getString(R.string.theme_item_chip_text_color_detail),
                customTheme.chipTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_link_color),
                context.getString(R.string.theme_item_link_color_detail),
                customTheme.linkColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_received_message_text_color),
                context.getString(R.string.theme_item_received_message_text_color_detail),
                customTheme.receivedMessageTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_sent_message_text_color),
                context.getString(R.string.theme_item_sent_message_text_color_detail),
                customTheme.sentMessageTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_background_color),
                context.getString(R.string.theme_item_background_color_detail),
                customTheme.backgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_card_view_background_color),
                context.getString(R.string.theme_item_card_view_background_color_detail),
                customTheme.cardViewBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_read_post_card_view_background_color),
                context.getString(R.string.theme_item_read_post_card_view_background_color_detail),
                customTheme.readPostCardViewBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_background_color),
                context.getString(R.string.theme_item_comment_background_color_detail),
                customTheme.commentBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_fully_collapsed_comment_background_color),
                context.getString(R.string.theme_item_fully_collapsed_comment_background_color_detail),
                customTheme.fullyCollapsedCommentBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_awarded_comment_background_color),
                context.getString(R.string.theme_item_awarded_comment_background_color_detail),
                customTheme.awardedCommentBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_received_message_background_color),
                context.getString(R.string.theme_item_received_message_background_color_detail),
                customTheme.receivedMessageBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_sent_message_background_color),
                context.getString(R.string.theme_item_sent_message_background_color_detail),
                customTheme.sentMessageBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_bottom_app_bar_background_color),
                context.getString(R.string.theme_item_bottom_app_bar_background_color_detail),
                customTheme.bottomAppBarBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_primary_icon_color),
                context.getString(R.string.theme_item_primary_icon_color_detail),
                customTheme.primaryIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_bottom_app_bar_icon_color),
                context.getString(R.string.theme_item_bottom_app_bar_icon_color_detail),
                customTheme.bottomAppBarIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_post_icon_and_info_color),
                context.getString(R.string.theme_item_post_icon_and_info_color_detail),
                customTheme.postIconAndInfoColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_icon_and_info_color),
                context.getString(R.string.theme_item_comment_icon_and_info_color_detail),
                customTheme.commentIconAndInfoColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_fab_icon_color),
                context.getString(R.string.theme_item_fab_icon_color_detail),
                customTheme.fabIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_send_message_icon_color),
                context.getString(R.string.theme_item_send_message_icon_color_detail),
                customTheme.sendMessageIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_toolbar_primary_text_and_icon_color),
                context.getString(R.string.theme_item_toolbar_primary_text_and_icon_color_detail),
                customTheme.toolbarPrimaryTextAndIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_toolbar_secondary_text_color),
                context.getString(R.string.theme_item_toolbar_secondary_text_color_detail),
                customTheme.toolbarSecondaryTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_circular_progress_bar_background_color),
                context.getString(R.string.theme_item_circular_progress_bar_background_color_detail),
                customTheme.circularProgressBarBackground));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_media_indicator_icon_color),
                context.getString(R.string.theme_item_media_indicator_icon_color_detail),
                customTheme.mediaIndicatorIconColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_media_indicator_background_color),
                context.getString(R.string.theme_item_media_indicator_background_color_detail),
                customTheme.mediaIndicatorBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_tab_background),
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_tab_background_detail),
                customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_text_color),
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_text_color_detail),
                customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_tab_indicator),
                context.getString(R.string.theme_item_tab_layout_with_expanded_collapsing_toolbar_tab_indicator_detail),
                customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_tab_background),
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_tab_background_detail),
                customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_text_color),
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_text_color_detail),
                customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_tab_indicator),
                context.getString(R.string.theme_item_tab_layout_with_collapsed_collapsing_toolbar_tab_indicator_detail),
                customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_upvoted_color),
                context.getString(R.string.theme_item_upvoted_color_detail),
                customTheme.upvoted));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_downvoted_color),
                context.getString(R.string.theme_item_downvoted_color_detail),
                customTheme.downvoted));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_post_type_background_color),
                context.getString(R.string.theme_item_post_type_background_color_detail),
                customTheme.postTypeBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_post_type_text_color),
                context.getString(R.string.theme_item_post_type_text_color_detail),
                customTheme.postTypeTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_spoiler_background_color),
                context.getString(R.string.theme_item_spoiler_background_color_detail),
                customTheme.spoilerBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_spoiler_text_color),
                context.getString(R.string.theme_item_spoiler_text_color_detail),
                customTheme.spoilerTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_nsfw_background_color),
                context.getString(R.string.theme_item_nsfw_background_color_detail),
                customTheme.nsfwBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_nsfw_text_color),
                context.getString(R.string.theme_item_nsfw_text_color_detail),
                customTheme.nsfwTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_flair_background_color),
                context.getString(R.string.theme_item_flair_background_color_detail),
                customTheme.flairBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_flair_text_color),
                context.getString(R.string.theme_item_flair_text_color_detail),
                customTheme.flairTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_awards_background_color),
                context.getString(R.string.theme_item_awards_background_color_detail),
                customTheme.awardsBackgroundColor
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_awards_text_color),
                context.getString(R.string.theme_item_awards_text_color_detail),
                customTheme.awardsTextColor
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_archived_tint),
                context.getString(R.string.theme_item_archived_tint_detail),
                customTheme.archivedTint));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_locked_icon_tint),
                context.getString(R.string.theme_item_locked_icon_tint_detail),
                customTheme.lockedIconTint));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_crosspost_icon_tint),
                context.getString(R.string.theme_item_crosspost_icon_tint_detail),
                customTheme.crosspostIconTint));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_upvote_ratio_icon_tint),
                context.getString(R.string.theme_item_upvote_ratio_icon_tint_detail),
                customTheme.upvoteRatioIconTint
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_stickied_post_icon_tint),
                context.getString(R.string.theme_item_stickied_post_icon_tint_detail),
                customTheme.stickiedPostIconTint));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_no_preview_post_type_icon_tint),
                context.getString(R.string.theme_item_no_preview_post_type_icon_tint_detail),
                customTheme.noPreviewPostTypeIconTint
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_subscribed_color),
                context.getString(R.string.theme_item_subscribed_color_detail),
                customTheme.subscribed));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_unsubscribed_color),
                context.getString(R.string.theme_item_unsubscribed_color_detail),
                customTheme.unsubscribed));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_username_color),
                context.getString(R.string.theme_item_username_color_detail),
                customTheme.username));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_subreddit_color),
                context.getString(R.string.theme_item_subreddit_color_detail),
                customTheme.subreddit));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_author_flair_text_color),
                context.getString(R.string.theme_item_author_flair_text_color_detail),
                customTheme.authorFlairTextColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_submitter_color),
                context.getString(R.string.theme_item_submitter_color_detail),
                customTheme.submitter));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_moderator_color),
                context.getString(R.string.theme_item_moderator_color_detail),
                customTheme.moderator));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_current_user_color),
                context.getString(R.string.theme_item_current_user_color_detail),
                customTheme.currentUser
        ));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_single_comment_thread_background_color),
                context.getString(R.string.theme_item_single_comment_thread_background_color_detail),
                customTheme.singleCommentThreadBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_unread_message_background_color),
                context.getString(R.string.theme_item_unread_message_background_color_detail),
                customTheme.unreadMessageBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_divider_color),
                context.getString(R.string.theme_item_divider_color_detail),
                customTheme.dividerColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_no_preview_post_type_background_color),
                context.getString(R.string.theme_item_no_preview_post_type_background_color_detail),
                customTheme.noPreviewPostTypeBackgroundColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_vote_and_reply_unavailable_button_color),
                context.getString(R.string.theme_item_vote_and_reply_unavailable_button_color_detail),
                customTheme.voteAndReplyUnavailableButtonColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_1),
                context.getString(R.string.theme_item_comment_vertical_bar_color_1_detail),
                customTheme.commentVerticalBarColor1));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_2),
                context.getString(R.string.theme_item_comment_vertical_bar_color_2_detail),
                customTheme.commentVerticalBarColor2));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_3),
                context.getString(R.string.theme_item_comment_vertical_bar_color_3_detail),
                customTheme.commentVerticalBarColor3));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_4),
                context.getString(R.string.theme_item_comment_vertical_bar_color_4_detail),
                customTheme.commentVerticalBarColor4));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_5),
                context.getString(R.string.theme_item_comment_vertical_bar_color_5_detail),
                customTheme.commentVerticalBarColor5));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_6),
                context.getString(R.string.theme_item_comment_vertical_bar_color_6_detail),
                customTheme.commentVerticalBarColor6));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_comment_vertical_bar_color_7),
                context.getString(R.string.theme_item_comment_vertical_bar_color_7_detail),
                customTheme.commentVerticalBarColor7));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_nav_bar_color),
                context.getString(R.string.theme_item_nav_bar_color_detail),
                customTheme.navBarColor));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_light_status_bar),
                customTheme.isLightStatusBar));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_light_nav_bar),
                customTheme.isLightNavBar));
        customThemeSettingsItems.add(new CustomThemeSettingsItem(
                context.getString(R.string.theme_item_change_status_bar_icon_color_after_toolbar_collapsed_in_immersive_interface),
                customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface));
        if (androidVersion < Build.VERSION_CODES.O) {
            customThemeSettingsItems.get(customThemeSettingsItems.size() - 2).itemDetails = context.getString(R.string.theme_item_available_on_android_8);
        }
        if (androidVersion < Build.VERSION_CODES.M) {
            customThemeSettingsItems.get(customThemeSettingsItems.size() - 3).itemDetails = context.getString(R.string.theme_item_available_on_android_6);
        }
        return customThemeSettingsItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(itemName);
        parcel.writeString(itemDetails);
        parcel.writeInt(colorValue);
        parcel.writeByte((byte) (isEnabled ? 1 : 0));
    }
}
