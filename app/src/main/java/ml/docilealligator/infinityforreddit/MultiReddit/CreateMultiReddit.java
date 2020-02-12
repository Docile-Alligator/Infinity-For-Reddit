package ml.docilealligator.infinityforreddit.MultiReddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.RedditAPI;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CreateMultiReddit {
    public interface CreateMultiRedditListener {
        void success();
        void failed(int errorType);
    }

    public static void createMultiReddit(Retrofit oauthRetrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                         String accessToken, String multipath, String model,
                                         CreateMultiRedditListener createMultiRedditListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.MULTIPATH_KEY, multipath);
        params.put(RedditUtils.MODEL_KEY, model);
        oauthRetrofit.create(RedditAPI.class).createMultiReddit(RedditUtils.getOAuthHeader(accessToken),
                params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseMultiReddit.parseAndSaveMultiReddit(response.body(), redditDataRoomDatabase,
                            new ParseMultiReddit.ParseMultiRedditListener() {
                        @Override
                        public void success() {
                            Log.i("asfasfas", response.body());
                            createMultiRedditListener.success();
                        }

                        @Override
                        public void failed() {
                            createMultiRedditListener.failed(1);
                        }
                    });
                } else {
                    createMultiRedditListener.failed(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                createMultiRedditListener.failed(0);
            }
        });
    }
}
