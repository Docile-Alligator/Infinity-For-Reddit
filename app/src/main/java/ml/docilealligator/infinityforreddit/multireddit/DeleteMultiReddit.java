package ml.docilealligator.infinityforreddit.multireddit;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.asynctasks.DeleteMultiredditInDatabaseAsyncTask;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DeleteMultiReddit {
    public interface DeleteMultiRedditListener {
        void success();
        void failed();
    }

    public static void deleteMultiReddit(Retrofit oauthRetrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                         String accessToken, String accountName, String multipath,
                                         DeleteMultiRedditListener deleteMultiRedditListener) {
        oauthRetrofit.create(RedditAPI.class).deleteMultiReddit(APIUtils.getOAuthHeader(accessToken),
                multipath).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new DeleteMultiredditInDatabaseAsyncTask(redditDataRoomDatabase, accountName, multipath,
                            deleteMultiRedditListener::success).execute();
                } else {
                    deleteMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                deleteMultiRedditListener.failed();
            }
        });
    }
}
