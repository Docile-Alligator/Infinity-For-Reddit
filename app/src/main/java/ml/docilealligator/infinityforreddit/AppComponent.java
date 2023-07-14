package ml.ino6962.postinfinityforreddit;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import ml.ino6962.postinfinityforreddit.activities.AccountPostsActivity;
import ml.ino6962.postinfinityforreddit.activities.AccountSavedThingActivity;
import ml.ino6962.postinfinityforreddit.activities.CommentActivity;
import ml.ino6962.postinfinityforreddit.activities.CreateMultiRedditActivity;
import ml.ino6962.postinfinityforreddit.activities.CustomThemeListingActivity;
import ml.ino6962.postinfinityforreddit.activities.CustomThemePreviewActivity;
import ml.ino6962.postinfinityforreddit.activities.CustomizePostFilterActivity;
import ml.ino6962.postinfinityforreddit.activities.CustomizeThemeActivity;
import ml.ino6962.postinfinityforreddit.activities.EditCommentActivity;
import ml.ino6962.postinfinityforreddit.activities.EditMultiRedditActivity;
import ml.ino6962.postinfinityforreddit.activities.EditPostActivity;
import ml.ino6962.postinfinityforreddit.activities.EditProfileActivity;
import ml.ino6962.postinfinityforreddit.activities.FetchRandomSubredditOrPostActivity;
import ml.ino6962.postinfinityforreddit.activities.FilteredPostsActivity;
import ml.ino6962.postinfinityforreddit.activities.FullMarkdownActivity;
import ml.ino6962.postinfinityforreddit.activities.GiveAwardActivity;
import ml.ino6962.postinfinityforreddit.activities.HistoryActivity;
import ml.ino6962.postinfinityforreddit.activities.InboxActivity;
import ml.ino6962.postinfinityforreddit.activities.LinkResolverActivity;
import ml.ino6962.postinfinityforreddit.activities.LockScreenActivity;
import ml.ino6962.postinfinityforreddit.activities.LoginActivity;
import ml.ino6962.postinfinityforreddit.activities.MainActivity;
import ml.ino6962.postinfinityforreddit.activities.MultiredditSelectionActivity;
import ml.ino6962.postinfinityforreddit.activities.PostFilterPreferenceActivity;
import ml.ino6962.postinfinityforreddit.activities.PostFilterUsageListingActivity;
import ml.ino6962.postinfinityforreddit.activities.PostGalleryActivity;
import ml.ino6962.postinfinityforreddit.activities.PostImageActivity;
import ml.ino6962.postinfinityforreddit.activities.PostLinkActivity;
import ml.ino6962.postinfinityforreddit.activities.PostPollActivity;
import ml.ino6962.postinfinityforreddit.activities.PostTextActivity;
import ml.ino6962.postinfinityforreddit.activities.PostVideoActivity;
import ml.ino6962.postinfinityforreddit.activities.ReportActivity;
import ml.ino6962.postinfinityforreddit.activities.RulesActivity;
import ml.ino6962.postinfinityforreddit.activities.SearchActivity;
import ml.ino6962.postinfinityforreddit.activities.SearchResultActivity;
import ml.ino6962.postinfinityforreddit.activities.SearchSubredditsResultActivity;
import ml.ino6962.postinfinityforreddit.activities.SearchUsersResultActivity;
import ml.ino6962.postinfinityforreddit.activities.SelectUserFlairActivity;
import ml.ino6962.postinfinityforreddit.activities.SelectedSubredditsAndUsersActivity;
import ml.ino6962.postinfinityforreddit.activities.SendPrivateMessageActivity;
import ml.ino6962.postinfinityforreddit.activities.SettingsActivity;
import ml.ino6962.postinfinityforreddit.activities.SubmitCrosspostActivity;
import ml.ino6962.postinfinityforreddit.activities.SubredditMultiselectionActivity;
import ml.ino6962.postinfinityforreddit.activities.SubredditSelectionActivity;
import ml.ino6962.postinfinityforreddit.activities.SubscribedThingListingActivity;
import ml.ino6962.postinfinityforreddit.activities.SuicidePreventionActivity;
import ml.ino6962.postinfinityforreddit.activities.TrendingActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewImageOrGifActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewImgurMediaActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewMultiRedditDetailActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewPostDetailActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewPrivateMessagesActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewRedditGalleryActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewSubredditDetailActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewUserDetailActivity;
import ml.ino6962.postinfinityforreddit.activities.ViewVideoActivity;
import ml.ino6962.postinfinityforreddit.activities.WebViewActivity;
import ml.ino6962.postinfinityforreddit.activities.WikiActivity;
import ml.ino6962.postinfinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.ino6962.postinfinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.ino6962.postinfinityforreddit.fragments.CommentsListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.FollowedUsersListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.HistoryPostFragment;
import ml.ino6962.postinfinityforreddit.fragments.InboxFragment;
import ml.ino6962.postinfinityforreddit.fragments.MorePostsInfoFragment;
import ml.ino6962.postinfinityforreddit.fragments.MultiRedditListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.PostFragment;
import ml.ino6962.postinfinityforreddit.fragments.SidebarFragment;
import ml.ino6962.postinfinityforreddit.fragments.SubredditListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.SubscribedSubredditsListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.UserListingFragment;
import ml.ino6962.postinfinityforreddit.fragments.ViewImgurImageFragment;
import ml.ino6962.postinfinityforreddit.fragments.ViewImgurVideoFragment;
import ml.ino6962.postinfinityforreddit.fragments.ViewPostDetailFragment;
import ml.ino6962.postinfinityforreddit.fragments.ViewRedditGalleryImageOrGifFragment;
import ml.ino6962.postinfinityforreddit.fragments.ViewRedditGalleryVideoFragment;
import ml.ino6962.postinfinityforreddit.services.DownloadMediaService;
import ml.ino6962.postinfinityforreddit.services.DownloadRedditVideoService;
import ml.ino6962.postinfinityforreddit.services.EditProfileService;
import ml.ino6962.postinfinityforreddit.services.SubmitPostService;
import ml.ino6962.postinfinityforreddit.settings.AdvancedPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.CommentPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.CrashReportsFragment;
import ml.ino6962.postinfinityforreddit.settings.CustomizeBottomAppBarFragment;
import ml.ino6962.postinfinityforreddit.settings.CustomizeMainPageTabsFragment;
import ml.ino6962.postinfinityforreddit.settings.DownloadLocationPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.FontPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.GesturesAndButtonsPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.MainPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.MiscellaneousPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.NotificationPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.NsfwAndSpoilerFragment;
import ml.ino6962.postinfinityforreddit.settings.PostHistoryFragment;
import ml.ino6962.postinfinityforreddit.settings.SecurityPreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.ThemePreferenceFragment;
import ml.ino6962.postinfinityforreddit.settings.TranslationFragment;
import ml.ino6962.postinfinityforreddit.settings.VideoPreferenceFragment;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(LoginActivity loginActivity);

    void inject(PostFragment postFragment);

    void inject(SubredditListingFragment subredditListingFragment);

    void inject(UserListingFragment userListingFragment);

    void inject(ViewPostDetailActivity viewPostDetailActivity);

    void inject(ViewSubredditDetailActivity viewSubredditDetailActivity);

    void inject(ViewUserDetailActivity viewUserDetailActivity);

    void inject(CommentActivity commentActivity);

    void inject(SubscribedThingListingActivity subscribedThingListingActivity);

    void inject(PostTextActivity postTextActivity);

    void inject(SubscribedSubredditsListingFragment subscribedSubredditsListingFragment);

    void inject(PostLinkActivity postLinkActivity);

    void inject(PostImageActivity postImageActivity);

    void inject(PostVideoActivity postVideoActivity);

    void inject(FlairBottomSheetFragment flairBottomSheetFragment);

    void inject(RulesActivity rulesActivity);

    void inject(CommentsListingFragment commentsListingFragment);

    void inject(SubmitPostService submitPostService);

    void inject(FilteredPostsActivity filteredPostsActivity);

    void inject(SearchResultActivity searchResultActivity);

    void inject(SearchSubredditsResultActivity searchSubredditsResultActivity);

    void inject(FollowedUsersListingFragment followedUsersListingFragment);

    void inject(SubredditSelectionActivity subredditSelectionActivity);

    void inject(EditPostActivity editPostActivity);

    void inject(EditCommentActivity editCommentActivity);

    void inject(AccountPostsActivity accountPostsActivity);

    void inject(PullNotificationWorker pullNotificationWorker);

    void inject(InboxActivity inboxActivity);

    void inject(NotificationPreferenceFragment notificationPreferenceFragment);

    void inject(LinkResolverActivity linkResolverActivity);

    void inject(SearchActivity searchActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(MainPreferenceFragment mainPreferenceFragment);

    void inject(AccountSavedThingActivity accountSavedThingActivity);

    void inject(ViewImageOrGifActivity viewGIFActivity);

    void inject(ViewMultiRedditDetailActivity viewMultiRedditDetailActivity);

    void inject(ViewVideoActivity viewVideoActivity);

    void inject(GesturesAndButtonsPreferenceFragment gesturesAndButtonsPreferenceFragment);

    void inject(CreateMultiRedditActivity createMultiRedditActivity);

    void inject(SubredditMultiselectionActivity subredditMultiselectionActivity);

    void inject(ThemePreferenceFragment themePreferenceFragment);

    void inject(CustomizeThemeActivity customizeThemeActivity);

    void inject(CustomThemeListingActivity customThemeListingActivity);

    void inject(SidebarFragment sidebarFragment);

    void inject(AdvancedPreferenceFragment advancedPreferenceFragment);

    void inject(CustomThemePreviewActivity customThemePreviewActivity);

    void inject(EditMultiRedditActivity editMultiRedditActivity);

    void inject(SelectedSubredditsAndUsersActivity selectedSubredditsAndUsersActivity);

    void inject(ReportActivity reportActivity);

    void inject(ViewImgurMediaActivity viewImgurMediaActivity);

    void inject(ViewImgurVideoFragment viewImgurVideoFragment);

    void inject(DownloadRedditVideoService downloadRedditVideoService);

    void inject(MultiRedditListingFragment multiRedditListingFragment);

    void inject(InboxFragment inboxFragment);

    void inject(ViewPrivateMessagesActivity viewPrivateMessagesActivity);

    void inject(SendPrivateMessageActivity sendPrivateMessageActivity);

    void inject(VideoPreferenceFragment videoPreferenceFragment);

    void inject(ViewRedditGalleryActivity viewRedditGalleryActivity);

    void inject(ViewRedditGalleryVideoFragment viewRedditGalleryVideoFragment);

    void inject(CustomizeMainPageTabsFragment customizeMainPageTabsFragment);

    void inject(DownloadMediaService downloadMediaService);

    void inject(DownloadLocationPreferenceFragment downloadLocationPreferenceFragment);

    void inject(SubmitCrosspostActivity submitCrosspostActivity);

    void inject(FullMarkdownActivity fullMarkdownActivity);

    void inject(SelectUserFlairActivity selectUserFlairActivity);

    void inject(SecurityPreferenceFragment securityPreferenceFragment);

    void inject(NsfwAndSpoilerFragment nsfwAndSpoilerFragment);

    void inject(CustomizeBottomAppBarFragment customizeBottomAppBarFragment);

    void inject(GiveAwardActivity giveAwardActivity);

    void inject(TranslationFragment translationFragment);

    void inject(FetchRandomSubredditOrPostActivity fetchRandomSubredditOrPostActivity);

    void inject(MiscellaneousPreferenceFragment miscellaneousPreferenceFragment);

    void inject(CustomizePostFilterActivity customizePostFilterActivity);

    void inject(PostHistoryFragment postHistoryFragment);

    void inject(PostFilterPreferenceActivity postFilterPreferenceActivity);

    void inject(PostFilterUsageListingActivity postFilterUsageListingActivity);

    void inject(SearchUsersResultActivity searchUsersResultActivity);

    void inject(MultiredditSelectionActivity multiredditSelectionActivity);

    void inject(ViewImgurImageFragment viewImgurImageFragment);

    void inject(ViewRedditGalleryImageOrGifFragment viewRedditGalleryImageOrGifFragment);

    void inject(ViewPostDetailFragment viewPostDetailFragment);

    void inject(SuicidePreventionActivity suicidePreventionActivity);

    void inject(WebViewActivity webViewActivity);

    void inject(CrashReportsFragment crashReportsFragment);

    void inject(LockScreenActivity lockScreenActivity);

    void inject(PostGalleryActivity postGalleryActivity);

    void inject(TrendingActivity trendingActivity);

    void inject(WikiActivity wikiActivity);

    void inject(Infinity infinity);

    void inject(EditProfileService editProfileService);

    void inject(EditProfileActivity editProfileActivity);

    void inject(FontPreferenceFragment fontPreferenceFragment);

    void inject(CommentPreferenceFragment commentPreferenceFragment);

    void inject(PostPollActivity postPollActivity);

    void inject(AccountChooserBottomSheetFragment accountChooserBottomSheetFragment);

    void inject(MaterialYouWorker materialYouWorker);

    void inject(HistoryPostFragment historyPostFragment);

    void inject(HistoryActivity historyActivity);

    void inject(MorePostsInfoFragment morePostsInfoFragment);

    @Component.Factory
    interface Factory {
        AppComponent create(@BindsInstance Application application);
    }
}
