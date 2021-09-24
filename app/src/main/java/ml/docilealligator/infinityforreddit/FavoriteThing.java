package ml.docilealligator.infinityforreddit;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubscribedThings;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteThing {
    public static void favoriteSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                         RedditDataRoomDatabase redditDataRoomDatabase,
                                         String accessToken, SubscribedSubredditData subscribedSubredditData,
                                         FavoriteThingListener favoriteThingListener) {
        if (accessToken == null) {
            InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase, subscribedSubredditData,
                    favoriteThingListener::success);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.SR_NAME_KEY, subscribedSubredditData.getName());
            params.put(APIUtils.MAKE_FAVORITE_KEY, "true");
            oauthRetrofit.create(RedditAPI.class).favoriteThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase, subscribedSubredditData,
                                favoriteThingListener::success);
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
    }

    public static void unfavoriteSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                           RedditDataRoomDatabase redditDataRoomDatabase,
                                           String accessToken, SubscribedSubredditData subscribedSubredditData,
                                           FavoriteThingListener favoriteThingListener) {
        if (accessToken == null) {
            InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase,
                    subscribedSubredditData, favoriteThingListener::success);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.SR_NAME_KEY, subscribedSubredditData.getName());
            params.put(APIUtils.MAKE_FAVORITE_KEY, "false");
            oauthRetrofit.create(RedditAPI.class).favoriteThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase,
                                subscribedSubredditData, favoriteThingListener::success);
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
    }

    public static void favoriteUser(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                    RedditDataRoomDatabase redditDataRoomDatabase,
                                    String accessToken, SubscribedUserData subscribedUserData,
                                    FavoriteThingListener favoriteThingListener) {
        if (accessToken == null) {
            InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase,
                    subscribedUserData, favoriteThingListener::success);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.SR_NAME_KEY, "u_" + subscribedUserData.getName());
            params.put(APIUtils.MAKE_FAVORITE_KEY, "true");
            oauthRetrofit.create(RedditAPI.class).favoriteThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase,
                                subscribedUserData, favoriteThingListener::success);
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
    }

    public static void unfavoriteUser(Executor executor, Handler handler, Retrofit oauthRetrofit,
                                      RedditDataRoomDatabase redditDataRoomDatabase,
                                      String accessToken, SubscribedUserData subscribedUserData,
                                      FavoriteThingListener favoriteThingListener) {
        if (accessToken == null) {
            InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase, subscribedUserData,
                    favoriteThingListener::success);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.SR_NAME_KEY, "u_" + subscribedUserData.getName());
            params.put(APIUtils.MAKE_FAVORITE_KEY, "false");
            oauthRetrofit.create(RedditAPI.class).favoriteThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase,
                                subscribedUserData, favoriteThingListener::success);
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
    }

    public interface FavoriteThingListener {
        void success();

        void failed();
    }
}
