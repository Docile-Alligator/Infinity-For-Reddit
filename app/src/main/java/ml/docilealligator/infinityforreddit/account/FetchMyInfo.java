package ml.docilealligator.infinityforreddit.account;

import android.os.Handler;
import android.text.Html;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMyInfo {

    public static void fetchAccountInfo(final Executor executor, final Handler handler, final Retrofit retrofit,
                                        final RedditDataRoomDatabase redditDataRoomDatabase,
                                        final String accessToken, final FetchMyInfoListener fetchMyInfoListener) {
        retrofit.create(RedditAPI.class).getMyInfo(APIUtils.getOAuthHeader(accessToken)).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body());
                            String name = jsonResponse.getString(JSONUtils.NAME_KEY);
                            String profileImageUrl = Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString();
                            String bannerImageUrl = !jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY) ? Html.fromHtml(jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY)).toString() : null;
                            int karma = jsonResponse.getInt(JSONUtils.TOTAL_KARMA_KEY);
                            boolean isMod = jsonResponse.getBoolean(JSONUtils.IS_MOD_KEY);

                            redditDataRoomDatabase.accountDao().updateAccountInfo(name, profileImageUrl, bannerImageUrl, karma, isMod);

                            handler.post(() -> fetchMyInfoListener.onFetchMyInfoSuccess(name, profileImageUrl, bannerImageUrl, karma, isMod));
                        } catch (JSONException e) {
                            handler.post(() -> fetchMyInfoListener.onFetchMyInfoFailed(true));
                        }
                    });
                } else {
                    fetchMyInfoListener.onFetchMyInfoFailed(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                fetchMyInfoListener.onFetchMyInfoFailed(false);
            }
        });
    }

    public interface FetchMyInfoListener {
        void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma, boolean isMod);

        void onFetchMyInfoFailed(boolean parseFailed);
    }
}
