package ml.docilealligator.infinityforreddit.MultiReddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMultiReddit {
    public interface FetchMultiRedditListener {
        void success(ArrayList<MultiReddit> multiReddits);
        void failed();
    }

    public static void fetchMyMultiReddits(Retrofit oauthRetrofit, String accessToken, FetchMultiRedditListener fetchMultiRedditListener) {
        oauthRetrofit.create(RedditAPI.class)
                .getMyMultiReddits(RedditUtils.getOAuthHeader(accessToken)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseMultiReddit.parseMultiRedditsList(response.body(), new ParseMultiReddit.ParseMultiRedditsListListener() {
                        @Override
                        public void success(ArrayList<MultiReddit> multiReddits) {
                            fetchMultiRedditListener.success(multiReddits);
                        }

                        @Override
                        public void failed() {
                            fetchMultiRedditListener.failed();
                        }
                    });
                } else {
                    fetchMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMultiRedditListener.failed();
            }
        });
    }
}
