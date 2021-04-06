package ml.docilealligator.infinityforreddit.user;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class UserFollowing {
    public static void followUser(Retrofit oauthRetrofit, Retrofit retrofit,
                                  String accessToken, String username, String accountName,
                                  RedditDataRoomDatabase redditDataRoomDatabase,
                                  UserFollowingListener userFollowingListener) {
        userFollowing(oauthRetrofit, retrofit, accessToken, username, accountName, "sub",
                redditDataRoomDatabase.subscribedUserDao(), userFollowingListener);
    }

    public static void unfollowUser(Retrofit oauthRetrofit, Retrofit retrofit,
                                    String accessToken, String username, String accountName,
                                    RedditDataRoomDatabase redditDataRoomDatabase,
                                    UserFollowingListener userFollowingListener) {
        userFollowing(oauthRetrofit, retrofit, accessToken, username, accountName, "unsub",
                redditDataRoomDatabase.subscribedUserDao(), userFollowingListener);
    }

    private static void userFollowing(Retrofit oauthRetrofit, Retrofit retrofit, String accessToken,
                                      String username, String accountName, String action, SubscribedUserDao subscribedUserDao,
                                      UserFollowingListener userFollowingListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ACTION_KEY, action);
        params.put(APIUtils.SR_NAME_KEY, "u_" + username);

        Call<String> subredditSubscriptionCall = api.subredditSubscription(APIUtils.getOAuthHeader(accessToken), params);
        subredditSubscriptionCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (action.equals("sub")) {
                        FetchUserData.fetchUserData(retrofit, username, new FetchUserData.FetchUserDataListener() {
                            @Override
                            public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                                new UpdateSubscriptionAsyncTask(subscribedUserDao, userData, accountName, true).execute();
                            }

                            @Override
                            public void onFetchUserDataFailed() {

                            }
                        });
                    } else {
                        new UpdateSubscriptionAsyncTask(subscribedUserDao, username, accountName, false).execute();
                    }
                    userFollowingListener.onUserFollowingSuccess();
                } else {
                    userFollowingListener.onUserFollowingFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                userFollowingListener.onUserFollowingFail();
            }
        });
    }

    public interface UserFollowingListener {
        void onUserFollowingSuccess();

        void onUserFollowingFail();
    }

    private static class UpdateSubscriptionAsyncTask extends AsyncTask<Void, Void, Void> {

        private SubscribedUserDao subscribedUserDao;
        private String username;
        private String accountName;
        private SubscribedUserData subscribedUserData;
        private boolean isSubscribing;

        UpdateSubscriptionAsyncTask(SubscribedUserDao subscribedUserDao, String username,
                                    String accountName, boolean isSubscribing) {
            this.subscribedUserDao = subscribedUserDao;
            this.username = username;
            this.accountName = accountName;
            this.isSubscribing = isSubscribing;
        }

        UpdateSubscriptionAsyncTask(SubscribedUserDao subscribedUserDao, UserData userData,
                                    String accountName, boolean isSubscribing) {
            this.subscribedUserDao = subscribedUserDao;
            this.subscribedUserData = new SubscribedUserData(userData.getName(), userData.getIconUrl(),
                    accountName, false);
            this.accountName = accountName;
            this.isSubscribing = isSubscribing;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isSubscribing) {
                subscribedUserDao.insert(subscribedUserData);
            } else {
                subscribedUserDao.deleteSubscribedUser(username, accountName);
            }
            return null;
        }
    }
}
