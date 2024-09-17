package ml.docilealligator.infinityforreddit.post;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchPost {
    public static void fetchPost(Executor executor, Handler handler, Retrofit retrofit, String id, @Nullable String accessToken,
                                 @NonNull String accountName,
                                 FetchPostListener fetchPostListener) {
        Call<String> postCall;
        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            postCall = retrofit.create(RedditAPI.class).getPost(id);
        } else {
            postCall = retrofit.create(RedditAPI.class).getPostOauth(id, APIUtils.getOAuthHeader(accessToken));
        }
        postCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePost(executor, handler, response.body(), new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            fetchPostListener.fetchPostSuccess(post);
                        }

                        @Override
                        public void onParsePostFail() {
                            fetchPostListener.fetchPostFailed();
                        }
                    });
                } else {
                    fetchPostListener.fetchPostFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchPostListener.fetchPostFailed();
            }
        });
    }

    @WorkerThread
    @Nullable
    public static Post fetchPostSync(Retrofit retrofit, String id, @Nullable String accessToken,
                                 @NonNull String accountName) {
        Call<String> postCall;
        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            postCall = retrofit.create(RedditAPI.class).getPost(id);
        } else {
            postCall = retrofit.create(RedditAPI.class).getPostOauth(id, APIUtils.getOAuthHeader(accessToken));
        }
        try {
            Response<String> response = postCall.execute();
            if (response.isSuccessful()) {
                return ParsePost.parsePostSync(response.body());
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    public static void fetchRandomPost(Executor executor, Handler handler, Retrofit retrofit, boolean isNSFW,
                                       FetchRandomPostListener fetchRandomPostListener) {
        Call<String> call;
        if (isNSFW) {
            call = retrofit.create(RedditAPI.class).getRandomNSFWPost();
        } else {
            call = retrofit.create(RedditAPI.class).getRandomPost();
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parseRandomPost(executor, handler, response.body(), isNSFW,
                            new ParsePost.ParseRandomPostListener() {
                                @Override
                                public void onParseRandomPostSuccess(String postId, String subredditName) {
                                    fetchRandomPostListener.fetchRandomPostSuccess(postId, subredditName);
                                }

                                @Override
                                public void onParseRandomPostFailed() {
                                    fetchRandomPostListener.fetchRandomPostFailed();
                                }
                            });
                } else {
                    fetchRandomPostListener.fetchRandomPostFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchRandomPostListener.fetchRandomPostFailed();
            }
        });
    }

    public interface FetchPostListener {
        void fetchPostSuccess(Post post);

        void fetchPostFailed();
    }

    public interface FetchRandomPostListener {
        void fetchRandomPostSuccess(String postId, String subredditName);
        void fetchRandomPostFailed();
    }
}
