package User;

import android.support.annotation.NonNull;
import android.util.Log;

import ml.docilealligator.infinityforreddit.RedditAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchUserData {
    public interface FetchUserDataListener {
        void onFetchUserDataSuccess(User user);
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
                    ParseUserData.parseMyInfo(response.body(), new ParseUserData.ParseUserDataListener() {
                        @Override
                        public void onParseUserDataSuccess(User user) {
                            fetchUserDataListener.onFetchUserDataSuccess(user);
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
