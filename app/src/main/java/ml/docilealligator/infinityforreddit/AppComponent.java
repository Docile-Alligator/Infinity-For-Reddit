package ml.docilealligator.infinityforreddit;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
interface AppComponent {
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
    void inject(PostMediaService postMediaService);
}
