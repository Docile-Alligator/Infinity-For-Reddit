package ml.docilealligator.infinityforreddit.user;

import android.os.Handler;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchUserFlairs {
    public static void fetchUserFlairsInSubreddit(Executor executor, Handler handler, Retrofit oauthRetrofit, String accessToken, String subredditName, FetchUserFlairsInSubredditListener fetchUserFlairsInSubredditListener) {
        oauthRetrofit.create(RedditAPI.class).getUserFlairs(APIUtils.getOAuthHeader(accessToken), subredditName)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            executor.execute(() -> {
                                ArrayList<UserFlair> userFlairs = parseUserFlairs(response.body());
                                if (userFlairs == null) {
                                    handler.post(fetchUserFlairsInSubredditListener::fetchFailed);
                                } else {
                                    handler.post(() -> fetchUserFlairsInSubredditListener.fetchSuccessful(userFlairs));
                                }
                            });
                        } else if (response.code() == 403) {
                            //No flairs
                            fetchUserFlairsInSubredditListener.fetchSuccessful(null);
                        } else {
                            fetchUserFlairsInSubredditListener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                        fetchUserFlairsInSubredditListener.fetchFailed();
                    }
                });
    }

    @WorkerThread
    private static ArrayList<UserFlair> parseUserFlairs(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            ArrayList<UserFlair> userFlairs = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject userFlairObject = jsonArray.getJSONObject(i);
                    String id = userFlairObject.getString(JSONUtils.ID_KEY);
                    String text = userFlairObject.getString(JSONUtils.TEXT_KEY);
                    boolean editable = userFlairObject.getBoolean(JSONUtils.TEXT_EDITABLE_KEY);
                    int maxEmojis = userFlairObject.getInt(JSONUtils.MAX_EMOJIS_KEY);

                    StringBuilder authorFlairHTMLBuilder = new StringBuilder();
                    if (userFlairObject.has(JSONUtils.RICHTEXT_KEY)) {
                        JSONArray flairArray = userFlairObject.getJSONArray(JSONUtils.RICHTEXT_KEY);
                        for (int j = 0; j < flairArray.length(); j++) {
                            JSONObject flairObject = flairArray.getJSONObject(j);
                            String e = flairObject.getString(JSONUtils.E_KEY);
                            if (e.equals("text")) {
                                authorFlairHTMLBuilder.append(Html.escapeHtml(flairObject.getString(JSONUtils.T_KEY)));
                            } else if (e.equals("emoji")) {
                                authorFlairHTMLBuilder.append("<img src=\"").append(Html.escapeHtml(flairObject.getString(JSONUtils.U_KEY))).append("\">");
                            }
                        }
                    }

                    userFlairs.add(new UserFlair(id, text, authorFlairHTMLBuilder.toString(), editable, maxEmojis));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return userFlairs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface FetchUserFlairsInSubredditListener {
        void fetchSuccessful(@Nullable ArrayList<UserFlair> userFlairs);

        void fetchFailed();
    }
}
