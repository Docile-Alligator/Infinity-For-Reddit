package ml.docilealligator.infinityforreddit;

import android.support.annotation.NonNull;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchSubredditData {
    interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(String response);
        void onFetchSubredditDataFail();
    }

    static void fetchSubredditData(Retrofit retrofit, String subredditName, final FetchSubredditDataListener fetchSubredditDataListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> subredditData = api.getSubredditData(subredditName);
        subredditData.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    fetchSubredditDataListener.onFetchSubredditDataSuccess(response.body());
                } else {
                    Log.i("call failed", response.message());
                    fetchSubredditDataListener.onFetchSubredditDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchSubredditDataListener.onFetchSubredditDataFail();
            }
        });
    }
}
