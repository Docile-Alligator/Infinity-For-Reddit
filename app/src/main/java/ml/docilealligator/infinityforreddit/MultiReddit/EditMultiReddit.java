package ml.docilealligator.infinityforreddit.MultiReddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditMultiReddit {
    public interface EditMultiRedditListener {
        void success();
        void failed();
    }

    public static void editMultiReddit(Retrofit oauthRetrofit, String accessToken, String multipath, String model,
                                       EditMultiRedditListener editMultiRedditListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.MULTIPATH_KEY, multipath);
        params.put(RedditUtils.MODEL_KEY, model);
        oauthRetrofit.create(RedditAPI.class).updateMultiReddit(RedditUtils.getOAuthHeader(accessToken),
                params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    editMultiRedditListener.success();
                } else {
                    editMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                editMultiRedditListener.failed();
            }
        });
    }
}
