package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.AsyncTask.InsertSubscribedThingsAsyncTask;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteThing {
    public static void favoriteThing(Retrofit oauthRetrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                     String accessToken, SubscribedSubredditData subscribedSubredditData,
                                     boolean isUser, FavoriteThingListener favoriteThingListener) {
        Map<String, String> params = new HashMap<>();
        if (isUser) {
            params.put(RedditUtils.SR_NAME_KEY, "u_" + subscribedSubredditData.getName());
        } else {
            params.put(RedditUtils.SR_NAME_KEY, subscribedSubredditData.getName());
        }
        params.put(RedditUtils.MAKE_FAVORITE_KEY, "true");
        oauthRetrofit.create(RedditAPI.class).favoriteThing(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new InsertSubscribedThingsAsyncTask(redditDataRoomDatabase, subscribedSubredditData,
                            favoriteThingListener::success).execute();
                } else {
                    favoriteThingListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                favoriteThingListener.failed();
            }
        });
    }

    public static void unfavoriteThing(Retrofit oauthRetrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                       String accessToken, SubscribedSubredditData subscribedSubredditData,
                                       boolean isUser, FavoriteThingListener favoriteThingListener) {
        Map<String, String> params = new HashMap<>();
        if (isUser) {
            params.put(RedditUtils.SR_NAME_KEY, "u_" + subscribedSubredditData.getName());
        } else {
            params.put(RedditUtils.SR_NAME_KEY, subscribedSubredditData.getName());
        }
        params.put(RedditUtils.MAKE_FAVORITE_KEY, "false");
        oauthRetrofit.create(RedditAPI.class).favoriteThing(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new InsertSubscribedThingsAsyncTask(redditDataRoomDatabase, subscribedSubredditData,
                            favoriteThingListener::success).execute();
                } else {
                    favoriteThingListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                favoriteThingListener.failed();
            }
        });
    }

    public interface FavoriteThingListener {
        void success();

        void failed();
    }
}
