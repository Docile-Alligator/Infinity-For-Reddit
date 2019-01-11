package ml.docilealligator.infinityforreddit;

import android.support.annotation.NonNull;
import android.util.Log;

import User.UserData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchUserData {
    public interface FetchUserDataListener {
        void onFetchUserDataSuccess(UserData userData);
        void onFetchUserDataFail();
    }

    public static void fetchUserData(final Retrofit retrofit, String userName,
                              final FetchUserDataListener fetchUserDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.getUserData(userName);
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParseUserData.parseUserData(response.body(), new ParseUserData.ParseUserDataListener() {
                        @Override
                        public void onParseUserDataSuccess(UserData userData) {
                            fetchUserDataListener.onFetchUserDataSuccess(userData);
                        }

                        @Override
                        public void onParseUserDataFail() {
                            fetchUserDataListener.onFetchUserDataFail();
                        }
                    });
                } else {
                    Log.i("call failed", response.message());
                    fetchUserDataListener.onFetchUserDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchUserDataListener.onFetchUserDataFail();
            }
        });
    }
}
