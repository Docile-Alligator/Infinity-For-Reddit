package ml.docilealligator.infinityforreddit;

import android.support.annotation.NonNull;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

class FetchComment {
    interface FetchCommentListener {
        void onFetchCommentSuccess(String response);
        void onFetchCommentFail();
    }

    static void queryComment(String subredditNamePrefixed, String article, final FetchCommentListener fetchCommentListener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> comments = api.getComments(subredditNamePrefixed, article);
        comments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    fetchCommentListener.onFetchCommentSuccess(response.body());
                } else {
                    Log.i("call failed", response.message());
                    fetchCommentListener.onFetchCommentFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchCommentListener.onFetchCommentFail();
            }
        });
    }
}
