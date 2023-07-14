package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class SubredditSubscription {
    public static void subscribeToSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                            Retrofit retrofit, String accessToken, String subredditName,
                                            String accountName, RedditDataRoomDatabase redditDataRoomDatabase,
                                            SubredditSubscriptionListener subredditSubscriptionListener) {
        subredditSubscription(executor, handler, oauthRetrofit, retrofit, accessToken, subredditName,
                accountName, "sub", redditDataRoomDatabase, subredditSubscriptionListener);
    }

    public static void anonymousSubscribeToSubreddit(Executor executor, Handler handler, Retrofit retrofit,
                                                     RedditDataRoomDatabase redditDataRoomDatabase,
                                                     String subredditName,
                                                     SubredditSubscriptionListener subredditSubscriptionListener) {
        FetchSubredditData.fetchSubredditData(null, retrofit, subredditName, "", new FetchSubredditData.FetchSubredditDataListener() {
            @Override
            public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                insertSubscription(executor, handler, redditDataRoomDatabase,
                        subredditData, "-", subredditSubscriptionListener);
            }

            @Override
            public void onFetchSubredditDataFail(boolean isQuarantined) {
                subredditSubscriptionListener.onSubredditSubscriptionFail();
            }
        });
    }

    public static void unsubscribeToSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                              String accessToken, String subredditName, String accountName,
                                              RedditDataRoomDatabase redditDataRoomDatabase,
                                              SubredditSubscriptionListener subredditSubscriptionListener) {
        subredditSubscription(executor, handler, oauthRetrofit, null, accessToken, subredditName,
                accountName, "unsub", redditDataRoomDatabase, subredditSubscriptionListener);
    }

    public static void anonymousUnsubscribeToSubreddit(Executor executor, Handler handler,
                                                       RedditDataRoomDatabase redditDataRoomDatabase,
                                                       String subredditName,
                                                       SubredditSubscriptionListener subredditSubscriptionListener) {
        removeSubscription(executor, handler, redditDataRoomDatabase, subredditName, "-", subredditSubscriptionListener);
    }

    private static void subredditSubscription(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                              Retrofit retrofit, String accessToken, String subredditName,
                                              String accountName, String action,
                                              RedditDataRoomDatabase redditDataRoomDatabase,
                                              SubredditSubscriptionListener subredditSubscriptionListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ACTION_KEY, action);
        params.put(APIUtils.SR_NAME_KEY, subredditName);

        Call<String> subredditSubscriptionCall = api.subredditSubscription(APIUtils.getOAuthHeader(accessToken), params);
        subredditSubscriptionCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (action.equals("sub")) {
                        FetchSubredditData.fetchSubredditData(oauthRetrofit, retrofit, subredditName, accessToken, new FetchSubredditData.FetchSubredditDataListener() {
                            @Override
                            public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                                insertSubscription(executor, handler, redditDataRoomDatabase,
                                        subredditData, accountName, subredditSubscriptionListener);
                            }

                            @Override
                            public void onFetchSubredditDataFail(boolean isQuarantined) {

                            }
                        });
                    } else {
                        removeSubscription(executor, handler, redditDataRoomDatabase, subredditName,
                                accountName, subredditSubscriptionListener);
                    }
                } else {
                    subredditSubscriptionListener.onSubredditSubscriptionFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                subredditSubscriptionListener.onSubredditSubscriptionFail();
            }
        });
    }

    public interface SubredditSubscriptionListener {
        void onSubredditSubscriptionSuccess();

        void onSubredditSubscriptionFail();
    }

    private static void insertSubscription(Executor executor, Handler handler,
                                           RedditDataRoomDatabase redditDataRoomDatabase,
                                           SubredditData subredditData, String accountName,
                                           SubredditSubscriptionListener subredditSubscriptionListener) {
        executor.execute(() -> {
            SubscribedSubredditData subscribedSubredditData = new SubscribedSubredditData(subredditData.getId(), subredditData.getName(),
                    subredditData.getIconUrl(), accountName, false);
            if (accountName.equals("-")) {
                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                }
            }
            redditDataRoomDatabase.subscribedSubredditDao().insert(subscribedSubredditData);
            handler.post(subredditSubscriptionListener::onSubredditSubscriptionSuccess);
        });
    }

    private static void removeSubscription(Executor executor, Handler handler,
                                           RedditDataRoomDatabase redditDataRoomDatabase,
                                           String subredditName, String accountName,
                                           SubredditSubscriptionListener subredditSubscriptionListener) {
        executor.execute(() -> {
            if (accountName.equals("-")) {
                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                }
            }
            redditDataRoomDatabase.subscribedSubredditDao().deleteSubscribedSubreddit(subredditName, accountName);
            handler.post(subredditSubscriptionListener::onSubredditSubscriptionSuccess);
        });
    }
}
