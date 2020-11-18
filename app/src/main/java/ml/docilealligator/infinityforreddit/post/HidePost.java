package ml.docilealligator.infinityforreddit.post;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HidePost {
    public static void hidePost(Retrofit oauthRetrofit, String accessToken, String fullname,
                                HidePostListener hidePostListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).hide(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
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
        params.put(APIUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).unhide(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
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
