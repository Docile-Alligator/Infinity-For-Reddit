package ml.ino6962.postinfinityforreddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ml.ino6962.postinfinityforreddit.apis.RedditAPI;
import ml.ino6962.postinfinityforreddit.subreddit.SubredditData;
import ml.ino6962.postinfinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.ino6962.postinfinityforreddit.subscribeduser.SubscribedUserData;
import ml.ino6962.postinfinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchSubscribedThing {
    public static void fetchSubscribedThing(final Retrofit oauthRetrofit, String accessToken, String accountName,
                                            final String lastItem, final ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                            final ArrayList<SubscribedUserData> subscribedUserData,
                                            final ArrayList<SubredditData> subredditData,
                                            final FetchSubscribedThingListener fetchSubscribedThingListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Call<String> subredditDataCall = api.getSubscribedThing(lastItem, APIUtils.getOAuthHeader(accessToken));
        subredditDataCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseSubscribedThing.parseSubscribedSubreddits(response.body(), accountName,
                            subscribedSubredditData, subscribedUserData, subredditData,
                            new ParseSubscribedThing.ParseSubscribedSubredditsListener() {

                                @Override
                                public void onParseSubscribedSubredditsSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                               ArrayList<SubscribedUserData> subscribedUserData,
                                                                               ArrayList<SubredditData> subredditData,
                                                                               String lastItem) {
                                    if (lastItem.equals("null")) {
                                        fetchSubscribedThingListener.onFetchSubscribedThingSuccess(
                                                subscribedSubredditData, subscribedUserData, subredditData);
                                    } else {
                                        fetchSubscribedThing(oauthRetrofit, accessToken, accountName, lastItem,
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
                    fetchSubscribedThingListener.onFetchSubscribedThingFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchSubscribedThingListener.onFetchSubscribedThingFail();
            }
        });
    }

    public interface FetchSubscribedThingListener {
        void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                           ArrayList<SubscribedUserData> subscribedUserData,
                                           ArrayList<SubredditData> subredditData);

        void onFetchSubscribedThingFail();
    }
}
