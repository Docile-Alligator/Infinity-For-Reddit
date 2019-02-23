package ml.docilealligator.infinityforreddit;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = NetworkModule.class)
interface NetworkComponent {
    void inject(MainActivity mainActivity);
    void inject(PostFragment postFragment);
    void inject(SubredditListingFragment subredditListingFragment);
    void inject(UserListingFragment userListingFragment);
    void inject(ViewPostDetailActivity viewPostDetailActivity);
    void inject(ViewSubredditDetailActivity viewSubredditDetailActivity);
    void inject(ViewUserDetailActivity viewUserDetailActivity);
}
