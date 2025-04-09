package ml.docilealligator.infinityforreddit.account;

import android.os.Handler;
import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMyInfo {

    public static void fetchAccountInfo(final Executor executor, final Handler handler, final Retrofit retrofit,
                                        final RedditDataRoomDatabase redditDataRoomDatabase,
                                        final String accessToken, final FetchMyInfoListener fetchMyInfoListener) {
        executor.execute(() -> {
            try {
                Response<String> response = retrofit.create(RedditAPI.class).getMyInfo(APIUtils.getOAuthHeader(accessToken)).execute();
                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    String name = jsonResponse.getString(JSONUtils.NAME_KEY);
                    String profileImageUrl = Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString();
                    String bannerImageUrl = !jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY) ? Html.fromHtml(jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY)).toString() : null;
                    int karma = jsonResponse.getInt(JSONUtils.TOTAL_KARMA_KEY);

                    redditDataRoomDatabase.accountDao().updateAccountInfo(name, profileImageUrl, bannerImageUrl, karma);

                    handler.post(() -> fetchMyInfoListener.onFetchMyInfoSuccess(name, profileImageUrl, bannerImageUrl, karma));
                } else {
                    handler.post(() -> fetchMyInfoListener.onFetchMyInfoFailed(false));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(() -> fetchMyInfoListener.onFetchMyInfoFailed(e instanceof JSONException));
            }
        });
    }

    public interface FetchMyInfoListener {
        void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma);

        void onFetchMyInfoFailed(boolean parseFailed);
    }
}
