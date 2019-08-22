package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class SaveThing {
    interface SaveThingListener {
        void success();
        void failed();
    }

    static void saveThing(Retrofit oauthRetrofit, String accessToken, String fullname,
                          SaveThingListener saveThingListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).save(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    saveThingListener.success();
                } else {
                    saveThingListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                saveThingListener.failed();
            }
        });
    }

    static void unsaveThing(Retrofit oauthRetrofit, String accessToken, String fullname,
                          SaveThingListener saveThingListener) {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, fullname);
        oauthRetrofit.create(RedditAPI.class).unsave(RedditUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    saveThingListener.success();
                } else {
                    saveThingListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                saveThingListener.failed();
            }
        });
    }
}
