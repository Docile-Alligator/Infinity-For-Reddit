package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

class FetchUserInfo {
    interface FetchUserInfoListener {
        void onFetchUserInfoSuccess(String response);
        void onFetchUserInfoFail();
    }

    static void fetchUserInfo(final Context context, final FetchUserInfoListener fetchUserInfoListener, final int refreshTime) {
        if(refreshTime < 0) {
            fetchUserInfoListener.onFetchUserInfoFail();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);

        String accessToken = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        Call<String> userInfo = api.getUserInfo(RedditUtils.getOAuthHeader(accessToken));
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    fetchUserInfoListener.onFetchUserInfoSuccess(response.body());
                } else if(response.code() == 401){
                    RefreshAccessToken.refreshAccessToken(context, new RefreshAccessToken.RefreshAccessTokenListener() {
                        @Override
                        public void onRefreshAccessTokenSuccess() {
                            fetchUserInfo(context, fetchUserInfoListener, refreshTime - 1);
                        }

                        @Override
                        public void onRefreshAccessTokenFail() {}
                    });
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
