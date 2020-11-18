package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchFlairs {
    public static void fetchFlairsInSubreddit(Retrofit oauthRetrofit, String accessToken, String subredditName, FetchFlairsInSubredditListener fetchFlairsInSubredditListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Call<String> flairsCall = api.getFlairs(APIUtils.getOAuthHeader(accessToken), subredditName);
        flairsCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseFlairsAsyncTask(response.body(), new ParseFlairsAsyncTask.ParseFlairsAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<Flair> flairs) {
                            fetchFlairsInSubredditListener.fetchSuccessful(flairs);
                        }

                        @Override
                        public void parseFailed() {
                            fetchFlairsInSubredditListener.fetchFailed();
                        }
                    }).execute();
                } else if (response.code() == 403) {
                    //No flairs
                    fetchFlairsInSubredditListener.fetchSuccessful(null);
                } else {
                    fetchFlairsInSubredditListener.fetchFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchFlairsInSubredditListener.fetchFailed();
            }
        });
    }

    public interface FetchFlairsInSubredditListener {
        void fetchSuccessful(ArrayList<Flair> flairs);

        void fetchFailed();
    }

    private static class ParseFlairsAsyncTask extends AsyncTask<Void, ArrayList<Flair>, ArrayList<Flair>> {
        private String response;
        private ParseFlairsAsyncTaskListener parseFlairsAsyncTaskListener;
        ParseFlairsAsyncTask(String response, ParseFlairsAsyncTaskListener parseFlairsAsyncTaskListener) {
            this.response = response;
            this.parseFlairsAsyncTaskListener = parseFlairsAsyncTaskListener;
        }

        @Override
        protected ArrayList<Flair> doInBackground(Void... voids) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                ArrayList<Flair> flairs = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String id = jsonArray.getJSONObject(i).getString(JSONUtils.ID_KEY);
                    String text = jsonArray.getJSONObject(i).getString(JSONUtils.TEXT_KEY);
                    boolean editable = jsonArray.getJSONObject(i).getBoolean(JSONUtils.TEXT_EDITABLE_KEY);

                    flairs.add(new Flair(id, text, editable));
                }
                return flairs;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Flair> strings) {
            if (strings != null) {
                parseFlairsAsyncTaskListener.parseSuccessful(strings);
            } else {
                parseFlairsAsyncTaskListener.parseFailed();
            }
        }

        interface ParseFlairsAsyncTaskListener {
            void parseSuccessful(ArrayList<Flair> flairs);

            void parseFailed();
        }
    }
}
