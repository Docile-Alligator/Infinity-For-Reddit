package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class FetchMyInfo {

    interface FetchUserMyListener {
        void onFetchMyInfoSuccess(String response);
        void onFetchMyInfoFail();
    }

    static void fetchAccountInfo(final Retrofit retrofit, String accessToken,
                                 final FetchUserMyListener fetchUserMyListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.getMyInfo(RedditUtils.getOAuthHeader(accessToken));
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    fetchUserMyListener.onFetchMyInfoSuccess(response.body());
                } else {
                    Log.i("call failed", response.message());
                    fetchUserMyListener.onFetchMyInfoFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchUserMyListener.onFetchMyInfoFail();
            }
        });
    }
}
