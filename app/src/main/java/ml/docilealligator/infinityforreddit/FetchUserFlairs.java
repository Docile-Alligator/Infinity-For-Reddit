package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchUserFlairs {
    public static void fetchUserFlairsInSubreddit(Retrofit oauthRetrofit, String accessToken, String subredditName, FetchUserFlairsInSubredditListener fetchUserFlairsInSubredditListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Call<String> flairsCall = api.getUserFlairs(APIUtils.getOAuthHeader(accessToken), subredditName);
        flairsCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseUserFlairsAsyncTask(response.body(), new ParseUserFlairsAsyncTask.ParseUserFlairsAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<UserFlair> userFlairs) {
                            fetchUserFlairsInSubredditListener.fetchSuccessful(userFlairs);
                        }

                        @Override
                        public void parseFailed() {
                            fetchUserFlairsInSubredditListener.fetchFailed();
                        }
                    }).execute();
                } else if (response.code() == 403) {
                    //No flairs
                    fetchUserFlairsInSubredditListener.fetchSuccessful(null);
                } else {
                    fetchUserFlairsInSubredditListener.fetchFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchUserFlairsInSubredditListener.fetchFailed();
            }
        });
    }

    public interface FetchUserFlairsInSubredditListener {
        void fetchSuccessful(ArrayList<UserFlair> userFlairs);

        void fetchFailed();
    }

    private static class ParseUserFlairsAsyncTask extends AsyncTask<Void, ArrayList<UserFlair>, ArrayList<UserFlair>> {
        private String response;
        private ParseUserFlairsAsyncTaskListener parseFlairsAsyncTaskListener;

        ParseUserFlairsAsyncTask(String response, ParseUserFlairsAsyncTaskListener parseFlairsAsyncTaskListener) {
            this.response = response;
            this.parseFlairsAsyncTaskListener = parseFlairsAsyncTaskListener;
        }

        @Override
        protected ArrayList<UserFlair> doInBackground(Void... voids) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                ArrayList<UserFlair> userFlairs = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
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
                }
                return userFlairs;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<UserFlair> userFlairs) {
            if (userFlairs != null) {
                parseFlairsAsyncTaskListener.parseSuccessful(userFlairs);
            } else {
                parseFlairsAsyncTaskListener.parseFailed();
            }
        }

        interface ParseUserFlairsAsyncTaskListener {
            void parseSuccessful(ArrayList<UserFlair> userFlairs);

            void parseFailed();
        }
    }
}
