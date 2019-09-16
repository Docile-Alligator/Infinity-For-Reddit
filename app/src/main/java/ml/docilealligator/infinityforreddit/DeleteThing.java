package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class DeleteThing {

  static void delete(Retrofit oauthRetrofit, String fullname, String accessToken,
      DeleteThingListener deleteThingListener) {
    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ID_KEY, fullname);
    oauthRetrofit.create(RedditAPI.class).delete(RedditUtils.getOAuthHeader(accessToken), params)
        .enqueue(new Callback<String>() {
          @Override
          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            if (response.isSuccessful()) {
              deleteThingListener.deleteSuccess();
            } else {
              deleteThingListener.deleteFailed();
            }
          }

          @Override
          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            deleteThingListener.deleteFailed();
          }
        });
  }

  interface DeleteThingListener {

    void deleteSuccess();

    void deleteFailed();
  }
}
