package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class FetchMyInfo {

  static void fetchAccountInfo(final Retrofit retrofit, String accessToken,
      final FetchUserMyListener fetchUserMyListener) {
    RedditAPI api = retrofit.create(RedditAPI.class);

    Call<String> userInfo = api.getMyInfo(RedditUtils.getOAuthHeader(accessToken));
    userInfo.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call,
          @NonNull retrofit2.Response<String> response) {
        if (response.isSuccessful()) {
          fetchUserMyListener.onFetchMyInfoSuccess(response.body());
        } else {
          fetchUserMyListener.onFetchMyInfoFail();
        }
      }

      @Override
      public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        fetchUserMyListener.onFetchMyInfoFail();
      }
    });
  }

  interface FetchUserMyListener {

    void onFetchMyInfoSuccess(String response);

    void onFetchMyInfoFail();
  }
}
