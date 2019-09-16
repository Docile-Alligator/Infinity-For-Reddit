package ml.docilealligator.infinityforreddit;

import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class SubredditSubscription {

  static void subscribeToSubreddit(Retrofit oauthRetrofit, Retrofit retrofit,
      String accessToken, String subredditName, String accountName,
      RedditDataRoomDatabase redditDataRoomDatabase,
      SubredditSubscriptionListener subredditSubscriptionListener) {
    subredditSubscription(oauthRetrofit, retrofit, accessToken, subredditName, accountName, "sub",
        redditDataRoomDatabase, subredditSubscriptionListener);
  }

  static void unsubscribeToSubreddit(Retrofit oauthRetrofit, String accessToken,
      String subredditName, String accountName,
      RedditDataRoomDatabase redditDataRoomDatabase,
      SubredditSubscriptionListener subredditSubscriptionListener) {
    subredditSubscription(oauthRetrofit, null, accessToken, subredditName, accountName, "unsub",
        redditDataRoomDatabase, subredditSubscriptionListener);
  }

  private static void subredditSubscription(Retrofit oauthRetrofit, Retrofit retrofit,
      String accessToken,
      String subredditName, String accountName, String action,
      RedditDataRoomDatabase redditDataRoomDatabase,
      SubredditSubscriptionListener subredditSubscriptionListener) {
    RedditAPI api = oauthRetrofit.create(RedditAPI.class);

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ACTION_KEY, action);
    params.put(RedditUtils.SR_NAME_KEY, subredditName);

    Call<String> subredditSubscriptionCall = api
        .subredditSubscription(RedditUtils.getOAuthHeader(accessToken), params);
    subredditSubscriptionCall.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call,
          @NonNull retrofit2.Response<String> response) {
        if (response.isSuccessful()) {
          if (action.equals("sub")) {
            FetchSubredditData.fetchSubredditData(retrofit, subredditName,
                new FetchSubredditData.FetchSubredditDataListener() {
                  @Override
                  public void onFetchSubredditDataSuccess(SubredditData subredditData,
                      int nCurrentOnlineSubscribers) {
                    new UpdateSubscriptionAsyncTask(redditDataRoomDatabase,
                        subredditData, accountName, true).execute();
                  }

                  @Override
                  public void onFetchSubredditDataFail() {

                  }
                });
          } else {
            new UpdateSubscriptionAsyncTask(redditDataRoomDatabase, subredditName, accountName,
                false).execute();
          }
          subredditSubscriptionListener.onSubredditSubscriptionSuccess();
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

  interface SubredditSubscriptionListener {

    void onSubredditSubscriptionSuccess();

    void onSubredditSubscriptionFail();
  }

  private static class UpdateSubscriptionAsyncTask extends AsyncTask<Void, Void, Void> {

    private final RedditDataRoomDatabase redditDataRoomDatabase;
    private final String accountName;
    private final boolean isSubscribing;
    private String subredditName;
    private SubscribedSubredditData subscribedSubredditData;

    UpdateSubscriptionAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String subredditName,
        String accountName, boolean isSubscribing) {
      this.redditDataRoomDatabase = redditDataRoomDatabase;
      this.subredditName = subredditName;
      this.accountName = accountName;
      this.isSubscribing = isSubscribing;
    }

    UpdateSubscriptionAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
        SubredditData subredditData,
        String accountName, boolean isSubscribing) {
      this.redditDataRoomDatabase = redditDataRoomDatabase;
      this.subscribedSubredditData = new SubscribedSubredditData(subredditData.getId(),
          subredditData.getName(),
          subredditData.getIconUrl(), accountName);
      this.accountName = accountName;
      this.isSubscribing = isSubscribing;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      if (isSubscribing) {
        redditDataRoomDatabase.subscribedSubredditDao().insert(subscribedSubredditData);
      } else {
        redditDataRoomDatabase.subscribedSubredditDao()
            .deleteSubscribedSubreddit(subredditName, accountName);
      }
      return null;
    }
  }
}
