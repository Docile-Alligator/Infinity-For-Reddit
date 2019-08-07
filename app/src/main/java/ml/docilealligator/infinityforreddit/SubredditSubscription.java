package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class SubredditSubscription {
    interface SubredditSubscriptionListener {
        void onSubredditSubscriptionSuccess();
        void onSubredditSubscriptionFail();
    }

    static void subscribeToSubreddit(Retrofit oauthRetrofit, Retrofit retrofit,
                                     String accessToken, String subredditName, String accountName,
                                     SubscribedSubredditDao subscribedSubredditDao,
                                     SubredditSubscriptionListener subredditSubscriptionListener) {
        subredditSubscription(oauthRetrofit, retrofit, accessToken, subredditName, accountName, "sub",
                subscribedSubredditDao, subredditSubscriptionListener);
    }

    static void unsubscribeToSubreddit(Retrofit oauthRetrofit, String accessToken,
                                       String subredditName, String accountName,
                                       SubscribedSubredditDao subscribedSubredditDao,
                                       SubredditSubscriptionListener subredditSubscriptionListener) {
        subredditSubscription(oauthRetrofit, null, accessToken, subredditName, accountName, "unsub",
                subscribedSubredditDao,subredditSubscriptionListener);
    }

    private static void subredditSubscription(Retrofit oauthRetrofit, Retrofit retrofit, String accessToken,
                                              String subredditName, String accountName, String action,
                                              SubscribedSubredditDao subscribedSubredditDao,
                                              SubredditSubscriptionListener subredditSubscriptionListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ACTION_KEY, action);
        params.put(RedditUtils.SR_NAME_KEY, subredditName);

        Call<String> subredditSubscriptionCall = api.subredditSubscription(RedditUtils.getOAuthHeader(accessToken), params);
        subredditSubscriptionCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    if(action.equals("sub")) {
                        FetchSubredditData.fetchSubredditData(retrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
                            @Override
                            public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                                new UpdateSubscriptionAsyncTask(subscribedSubredditDao,
                                        subredditData, accountName, true).execute();
                            }

                            @Override
                            public void onFetchSubredditDataFail() {

                            }
                        });
                    } else {
                        new UpdateSubscriptionAsyncTask(subscribedSubredditDao, subredditName, accountName, false).execute();
                    }
                    subredditSubscriptionListener.onSubredditSubscriptionSuccess();
                } else {
                    Log.i("call failed", response.message());
                    subredditSubscriptionListener.onSubredditSubscriptionFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                subredditSubscriptionListener.onSubredditSubscriptionFail();
            }
        });
    }

    private static class UpdateSubscriptionAsyncTask extends AsyncTask<Void, Void, Void> {

        private SubscribedSubredditDao subscribedSubredditDao;
        private String subredditName;
        private String accountName;
        private SubscribedSubredditData subscribedSubredditData;
        private boolean isSubscribing;

        UpdateSubscriptionAsyncTask(SubscribedSubredditDao subscribedSubredditDao, String subredditName,
                                    String accountName, boolean isSubscribing) {
            this.subscribedSubredditDao = subscribedSubredditDao;
            this.subredditName = subredditName;
            this.accountName = accountName;
            this.isSubscribing = isSubscribing;
        }

        UpdateSubscriptionAsyncTask(SubscribedSubredditDao subscribedSubredditDao, SubredditData subredditData,
                                    String accountName, boolean isSubscribing) {
            this.subscribedSubredditDao = subscribedSubredditDao;
            this.subscribedSubredditData = new SubscribedSubredditData(subredditData.getId(), subredditData.getName(),
                    subredditData.getIconUrl(), accountName);
            this.accountName = accountName;
            this.isSubscribing = isSubscribing;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(isSubscribing) {
                subscribedSubredditDao.insert(subscribedSubredditData);
            } else {
                subscribedSubredditDao.deleteSubscribedSubreddit(subredditName, accountName);
            }
            return null;
        }
    }
}
