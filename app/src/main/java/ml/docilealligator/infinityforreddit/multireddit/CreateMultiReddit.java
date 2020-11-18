package ml.docilealligator.infinityforreddit.multireddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
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
        params.put(APIUtils.MULTIPATH_KEY, multipath);
        params.put(APIUtils.MODEL_KEY, model);
        oauthRetrofit.create(RedditAPI.class).createMultiReddit(APIUtils.getOAuthHeader(accessToken),
                params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseMultiReddit.parseAndSaveMultiReddit(response.body(), redditDataRoomDatabase,
                            new ParseMultiReddit.ParseMultiRedditListener() {
                        @Override
                        public void success() {
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
