package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class FetchUserInfo {

    interface FetchUserInfoListener {
        void onFetchUserInfoSuccess(String response);
        void onFetchUserInfoFail();
    }

    static void fetchUserInfo(final Retrofit retrofit, SharedPreferences authInfoSharedPreferences,
                              final FetchUserInfoListener fetchUserInfoListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> userInfo = api.getUserInfo(RedditUtils.getOAuthHeader(accessToken));
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    fetchUserInfoListener.onFetchUserInfoSuccess(response.body());
                } else {
                    Log.i("call failed", response.message());
                    fetchUserInfoListener.onFetchUserInfoFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchUserInfoListener.onFetchUserInfoFail();
            }
        });
    }
}
