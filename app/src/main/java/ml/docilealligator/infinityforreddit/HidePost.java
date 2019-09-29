package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HidePost {
    public static void hidePost(Retrofit oauthRetrofit, String accessToken, String fullname,
                                HidePostListener hidePostListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).hide(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    hidePostListener.success();
                } else {
                    hidePostListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                hidePostListener.failed();
            }
        });
    }

    public static void unhidePost(Retrofit oauthRetrofit, String accessToken, String fullname,
                                  HidePostListener hidePostListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).unhide(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    hidePostListener.success();
                } else {
                    hidePostListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                hidePostListener.failed();
            }
        });
    }

    public interface HidePostListener {
        void success();

        void failed();
    }
}
