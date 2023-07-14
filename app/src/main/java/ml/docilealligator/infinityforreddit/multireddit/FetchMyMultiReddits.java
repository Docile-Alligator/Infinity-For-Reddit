package ml.ino6962.postinfinityforreddit.multireddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ml.ino6962.postinfinityforreddit.apis.RedditAPI;
import ml.ino6962.postinfinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMyMultiReddits {
    public interface FetchMyMultiRedditsListener {
        void success(ArrayList<MultiReddit> multiReddits);
        void failed();
    }

    public static void fetchMyMultiReddits(Retrofit oauthRetrofit, String accessToken, FetchMyMultiRedditsListener fetchMyMultiRedditsListener) {
        oauthRetrofit.create(RedditAPI.class)
                .getMyMultiReddits(APIUtils.getOAuthHeader(accessToken)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseMultiReddit.parseMultiRedditsList(response.body(), new ParseMultiReddit.ParseMultiRedditsListListener() {
                        @Override
                        public void success(ArrayList<MultiReddit> multiReddits) {
                            fetchMyMultiRedditsListener.success(multiReddits);
                        }

                        @Override
                        public void failed() {
                            fetchMyMultiRedditsListener.failed();
                        }
                    });
                } else {
                    fetchMyMultiRedditsListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMyMultiRedditsListener.failed();
            }
        });
    }
}
