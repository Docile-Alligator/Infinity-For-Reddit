package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;
import User.UserData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class UserFollowing {
    interface UserFollowingListener {
        void onUserFollowingSuccess();
        void onUserFollowingFail();
    }

    static void followUser(Retrofit oauthRetrofit, Retrofit retrofit,
                           SharedPreferences authInfoSharedPreferences, String userName,
                           SubscribedUserDao subscribedUserDao,
                           UserFollowingListener userFollowingListener) {
        userFollowing(oauthRetrofit, retrofit, authInfoSharedPreferences, userName, "sub",
                subscribedUserDao, userFollowingListener);
    }

    static void unfollowUser(Retrofit oauthRetrofit, Retrofit retrofit,
                             SharedPreferences authInfoSharedPreferences, String userName,
                             SubscribedUserDao subscribedUserDao,
                             UserFollowingListener userFollowingListener) {
        userFollowing(oauthRetrofit, retrofit, authInfoSharedPreferences, userName, "unsub",
                subscribedUserDao, userFollowingListener);
    }

    private static void userFollowing(Retrofit oauthRetrofit, Retrofit retrofit, SharedPreferences authInfoSharedPreferences,
                                      String userName, String action, SubscribedUserDao subscribedUserDao,
                                      UserFollowingListener userFollowingListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ACTION_KEY, action);
        params.put(RedditUtils.SR_NAME_KEY, "u_" + userName);

        Call<String> subredditSubscriptionCall = api.subredditSubscription(RedditUtils.getOAuthHeader(accessToken), params);
        subredditSubscriptionCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    if(action.equals("sub")) {
                        FetchUserData.fetchUserData(retrofit, userName, new FetchUserData.FetchUserDataListener() {
                            @Override
                            public void onFetchUserDataSuccess(UserData userData) {
                                new UpdateSubscriptionAsyncTask(subscribedUserDao, userData, true).execute();
                            }

                            @Override
                            public void onFetchUserDataFailed() {

                            }
                        });
                    } else {
                        new UpdateSubscriptionAsyncTask(subscribedUserDao, userName, false).execute();
                    }
                    userFollowingListener.onUserFollowingSuccess();
                } else {
                    Log.i("call failed", Integer.toString(response.code()));
                    userFollowingListener.onUserFollowingFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                userFollowingListener.onUserFollowingFail();
            }
        });
    }

    private static class UpdateSubscriptionAsyncTask extends AsyncTask<Void, Void, Void> {

        private SubscribedUserDao subscribedUserDao;
        private String userName;
        private SubscribedUserData subscribedUserData;
        private boolean isSubscribing;

        UpdateSubscriptionAsyncTask(SubscribedUserDao subscribedUserDao, String userName,
                                    boolean isSubscribing) {
            this.subscribedUserDao = subscribedUserDao;
            this.userName = userName;
            this.isSubscribing = isSubscribing;
        }

        UpdateSubscriptionAsyncTask(SubscribedUserDao subscribedUserDao, SubscribedUserData subscribedUserData,
                                    boolean isSubscribing) {
            this.subscribedUserDao = subscribedUserDao;
            this.subscribedUserData = subscribedUserData;
            this.isSubscribing = isSubscribing;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(isSubscribing) {
                subscribedUserDao.insert(subscribedUserData);
            } else {
                subscribedUserDao.deleteSubscribedUser(userName);;
            }
            return null;
        }
    }
}
