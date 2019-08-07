package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedUserDatabase.SubscribedUserData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchSubscribedThing {
    interface FetchSubscribedThingListener {
        void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                           ArrayList<SubscribedUserData> subscribedUserData,
                                           ArrayList<SubredditData> subredditData);
        void onFetchSubscribedThingFail();
    }

    static void fetchSubscribedThing(final Retrofit retrofit, String accessToken, String accountName,
                                     final String lastItem, final ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                     final ArrayList<SubscribedUserData> subscribedUserData,
                                     final ArrayList<SubredditData> subredditData,
                                     final FetchSubscribedThingListener fetchSubscribedThingListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditDataCall = api.getSubscribedThing(lastItem, RedditUtils.getOAuthHeader(accessToken));
        subredditDataCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseSubscribedThing.parseSubscribedSubreddits(response.body(), accountName,
                            subscribedSubredditData, subscribedUserData, subredditData,
                            new ParseSubscribedThing.ParseSubscribedSubredditsListener() {

                                @Override
                                public void onParseSubscribedSubredditsSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                               ArrayList<SubscribedUserData> subscribedUserData,
                                                                               ArrayList<SubredditData> subredditData,
                                                                               String lastItem) {
                                    if(lastItem.equals("null")) {
                                        fetchSubscribedThingListener.onFetchSubscribedThingSuccess(
                                                subscribedSubredditData, subscribedUserData, subredditData);
                                    } else {
                                        fetchSubscribedThing(retrofit, accessToken, accountName, lastItem,
                                                subscribedSubredditData, subscribedUserData, subredditData,
                                                fetchSubscribedThingListener);
                                    }
                                }

                                @Override
                                public void onParseSubscribedSubredditsFail() {
                                    fetchSubscribedThingListener.onFetchSubscribedThingFail();
                                }
                            });
                } else {
                    Log.i("call failed", response.message());
                    fetchSubscribedThingListener.onFetchSubscribedThingFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchSubscribedThingListener.onFetchSubscribedThingFail();
            }
        });
    }
}
