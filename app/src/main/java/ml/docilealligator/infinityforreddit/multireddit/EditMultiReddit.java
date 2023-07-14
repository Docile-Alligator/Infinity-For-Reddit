package ml.ino6962.postinfinityforreddit.multireddit;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;
import ml.ino6962.postinfinityforreddit.apis.RedditAPI;
import ml.ino6962.postinfinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditMultiReddit {
    public interface EditMultiRedditListener {
        void success();
        void failed();
    }

    public static void editMultiReddit(Retrofit oauthRetrofit, String accessToken, String multipath, String model,
                                       EditMultiRedditListener editMultiRedditListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.MULTIPATH_KEY, multipath);
        params.put(APIUtils.MODEL_KEY, model);
        oauthRetrofit.create(RedditAPI.class).updateMultiReddit(APIUtils.getOAuthHeader(accessToken),
                params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    editMultiRedditListener.success();
                } else {
                    editMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                editMultiRedditListener.failed();
            }
        });
    }

    public static void anonymousEditMultiReddit(Executor executor, Handler handler,
                                                RedditDataRoomDatabase redditDataRoomDatabase,
                                                MultiReddit multiReddit,
                                                EditMultiRedditListener editMultiRedditListener) {
        executor.execute(() -> {
            ArrayList<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits = new ArrayList<>();
            ArrayList<String> subreddits = multiReddit.getSubreddits();
            redditDataRoomDatabase.multiRedditDao().insert(multiReddit);
            for (String s : subreddits) {
                anonymousMultiredditSubreddits.add(new AnonymousMultiredditSubreddit(multiReddit.getPath(), s));
            }
            redditDataRoomDatabase.anonymousMultiredditSubredditDao().insertAll(anonymousMultiredditSubreddits);
            handler.post(editMultiRedditListener::success);
        });
    }
}
