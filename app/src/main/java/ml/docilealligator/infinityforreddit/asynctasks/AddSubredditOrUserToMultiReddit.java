package ml.ino6962.postinfinityforreddit.asynctasks;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.ino6962.postinfinityforreddit.apis.RedditAPI;
import ml.ino6962.postinfinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddSubredditOrUserToMultiReddit {
    public interface AddSubredditOrUserToMultiRedditListener {
        void success();
        void failed(int code);
    }

    public static void addSubredditOrUserToMultiReddit(Retrofit oauthRetrofit, String accessToken, String multipath,
                                                       String subredditName,
                                                       AddSubredditOrUserToMultiRedditListener addSubredditOrUserToMultiRedditListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.MODEL_KEY, "{\"name\":\"" + subredditName + "\"}");
        oauthRetrofit.create(RedditAPI.class).addSubredditToMultiReddit(APIUtils.getOAuthHeader(accessToken), params, multipath, subredditName)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            addSubredditOrUserToMultiRedditListener.success();
                        } else {
                            addSubredditOrUserToMultiRedditListener.failed(response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        addSubredditOrUserToMultiRedditListener.failed(-1);
                    }
                });
    }
}
