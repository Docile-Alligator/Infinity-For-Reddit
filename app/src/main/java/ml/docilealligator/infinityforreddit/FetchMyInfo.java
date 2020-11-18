package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchMyInfo {

    public static void fetchAccountInfo(final Retrofit retrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                        String accessToken, final FetchMyInfoListener fetchMyInfoListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.getMyInfo(APIUtils.getOAuthHeader(accessToken));
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseAndSaveAccountInfoAsyncTask(response.body(), redditDataRoomDatabase, fetchMyInfoListener).execute();
                } else {
                    fetchMyInfoListener.onFetchMyInfoFailed(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMyInfoListener.onFetchMyInfoFailed(false);
            }
        });
    }

    public interface FetchMyInfoListener {
        void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma);

        void onFetchMyInfoFailed(boolean parseFailed);
    }

    private static class ParseAndSaveAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private FetchMyInfoListener fetchMyInfoListener;
        private boolean parseFailed;

        private String name;
        private String profileImageUrl;
        private String bannerImageUrl;
        private int karma;

        ParseAndSaveAccountInfoAsyncTask(String response, RedditDataRoomDatabase redditDataRoomDatabase,
                                         FetchMyInfoListener fetchMyInfoListener) {
            try {
                jsonResponse = new JSONObject(response);
                this.redditDataRoomDatabase = redditDataRoomDatabase;
                this.fetchMyInfoListener = fetchMyInfoListener;
                parseFailed = false;
            } catch (JSONException e) {
                fetchMyInfoListener.onFetchMyInfoFailed(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                name = jsonResponse.getString(JSONUtils.NAME_KEY);
                profileImageUrl = Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString();
                if (!jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) {
                    bannerImageUrl = Html.fromHtml(jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY)).toString();
                }
                karma = jsonResponse.getInt(JSONUtils.TOTAL_KARMA_KEY);

                redditDataRoomDatabase.accountDao().updateAccountInfo(name, profileImageUrl, bannerImageUrl, karma);
            } catch (JSONException e) {
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                fetchMyInfoListener.onFetchMyInfoSuccess(name, profileImageUrl, bannerImageUrl, karma);
            } else {
                fetchMyInfoListener.onFetchMyInfoFailed(true);
            }
        }
    }
}
