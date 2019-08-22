package ml.docilealligator.infinityforreddit;

import javax.inject.Singleton;

import Settings.NotificationPreferenceFragment;
import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
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
    void inject(FilteredThingActivity filteredPostsActivity);
    void inject(SearchResultActivity searchResultActivity);
    void inject(SearchSubredditsResultActivity searchSubredditsResultActivity);
    void inject(FollowedUsersListingFragment followedUsersListingFragment);
    void inject(SubredditSelectionActivity subredditSelectionActivity);
    void inject(EditPostActivity editPostActivity);
    void inject(EditCommentActivity editCommentActivity);
    void inject(AccountPostsActivity accountPostsActivity);
    void inject(PullNotificationWorker pullNotificationWorker);
    void inject(ViewMessageActivity viewMessageActivity);
    void inject(NotificationPreferenceFragment notificationPreferenceFragment);
    void inject(LinkResolverActivity linkResolverActivity);
}
