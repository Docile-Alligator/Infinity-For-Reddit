package ml.docilealligator.infinityforreddit.MultiReddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.AsyncTask.InsertMultiRedditAsyncTask;
import ml.docilealligator.infinityforreddit.API.RedditAPI;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FavoriteMultiReddit {
    public interface FavoriteMultiRedditListener {
        void success();
        void failed();
    }

    public static void favoriteMultiReddit(Retrofit oauthRetrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                           String accessToken, String accountName, boolean makeFavorite,
                                           MultiReddit multiReddit, FavoriteMultiRedditListener favoriteMultiRedditListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.MULTIPATH_KEY, multiReddit.getPath());
        params.put(APIUtils.MAKE_FAVORITE_KEY, String.valueOf(makeFavorite));
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        oauthRetrofit.create(RedditAPI.class).favoriteMultiReddit(APIUtils.getOAuthHeader(accessToken),
                params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    multiReddit.setFavorite(true);
                    ArrayList<MultiReddit> singleMultiRedditList = new ArrayList<>();
                    singleMultiRedditList.add(multiReddit);
                    new InsertMultiRedditAsyncTask(redditDataRoomDatabase, singleMultiRedditList, accountName,
                            favoriteMultiRedditListener::success).execute();
                } else {
                    favoriteMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                favoriteMultiRedditListener.failed();
            }
        });
    }
}
