package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchPost {
    interface FetchPostListener {
        void fetchPostSuccess(Post post);
        void fetchPostFailed();
    }

    static void fetchPost(Retrofit retrofit, String id, String accessToken, Locale locale, FetchPostListener fetchPostListener) {
        Call<String> postCall;
        if(accessToken == null) {
            postCall = retrofit.create(RedditAPI.class).getPost(id);
        } else {
            postCall = retrofit.create(RedditAPI.class).getPostOauth(id, RedditUtils.getOAuthHeader(accessToken));
        }
        postCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            fetchPostListener.fetchPostSuccess(post);
                        }

                        @Override
                        public void onParsePostFail() {
                            fetchPostListener.fetchPostFailed();
                            Log.i("error", "fetch post failed");
                        }
                    });
                } else {
                    fetchPostListener.fetchPostFailed();
                    Log.i("call failed", Integer.toString(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchPostListener.fetchPostFailed();
                Log.i("call failed", t.getMessage());
            }
        });
    }
}
