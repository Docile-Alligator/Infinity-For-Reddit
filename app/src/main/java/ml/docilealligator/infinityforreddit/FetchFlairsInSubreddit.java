package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchFlairsInSubreddit {
    interface FetchFlairsInSubredditListener {
        void fetchSuccessful(ArrayList<String> flairs);
        void fetchFailed();
    }

    static void fetchFlairs(Retrofit oauthRetrofit, String accessToken, String subredditName, FetchFlairsInSubredditListener fetchFlairsInSubredditListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Call<String> flairsCall = api.getFlairs(RedditUtils.getOAuthHeader(accessToken), subredditName);
        flairsCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseFlairsAsyncTask(response.body(), new ParseFlairsAsyncTask.ParseFlairsAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<String> flairs) {
                            fetchFlairsInSubredditListener.fetchSuccessful(flairs);
                        }

                        @Override
                        public void parseFailed() {
                            fetchFlairsInSubredditListener.fetchFailed();
                        }
                    }).execute();
                } else if(response.code() == 403) {
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

    private static class ParseFlairsAsyncTask extends AsyncTask<Void, ArrayList<String>, ArrayList<String>> {
        interface ParseFlairsAsyncTaskListener {
            void parseSuccessful(ArrayList<String> flairs);
            void parseFailed();
        }

        private String response;
        private ParseFlairsAsyncTaskListener parseFlairsAsyncTaskListener;

        ParseFlairsAsyncTask(String response, ParseFlairsAsyncTaskListener parseFlairsAsyncTaskListener) {
            this.response = response;
            this.parseFlairsAsyncTaskListener = parseFlairsAsyncTaskListener;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                ArrayList<String> flairs = new ArrayList<>();
                for(int i  = 0; i < jsonArray.length(); i++) {
                    flairs.add(jsonArray.getJSONObject(i).getString(JSONUtils.TEXT_KEY));
                }
                return flairs;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            if(strings != null) {
                parseFlairsAsyncTaskListener.parseSuccessful(strings);
            } else {
                parseFlairsAsyncTaskListener.parseFailed();
            }
        }
    }
}
