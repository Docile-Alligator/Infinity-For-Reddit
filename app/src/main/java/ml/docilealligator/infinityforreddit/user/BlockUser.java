package ml.docilealligator.infinityforreddit.user;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BlockUser {
    public interface BlockUserListener {
        void success();
        void failed();
    }

    public static void blockUser(Retrofit oauthRetrofit, String accessToken, String username, BlockUserListener blockUserListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.NAME_KEY, username);
        oauthRetrofit.create(RedditAPI.class).blockUser(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    blockUserListener.success();
                } else {
                    blockUserListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                blockUserListener.failed();
            }
        });
    }
}
