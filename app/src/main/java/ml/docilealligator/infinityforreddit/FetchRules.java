package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRules {
    public interface FetchRulesListener {
        void success(ArrayList<Rule> rules);
        void failed();
    }

    public static void fetchRules(Retrofit retrofit, String subredditName, FetchRulesListener fetchRulesListener) {

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> rulesCall = api.getRules(subredditName);
        rulesCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseRulesAsyncTask(response.body(), new ParseRulesAsyncTask.ParseRulesAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<Rule> rules) {
                            fetchRulesListener.success(rules);
                        }

                        @Override
                        public void parseFailed() {
                            fetchRulesListener.failed();
                        }
                    }).execute();
                } else {
                    fetchRulesListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchRulesListener.failed();
            }
        });
    }

    private static class ParseRulesAsyncTask extends AsyncTask<Void, ArrayList<Rule>, ArrayList<Rule>> {
        private String response;
        private ParseRulesAsyncTask.ParseRulesAsyncTaskListener parseRulesAsyncTaskListener;

        ParseRulesAsyncTask(String response, ParseRulesAsyncTask.ParseRulesAsyncTaskListener parseRulesAsyncTaskListener) {
            this.response = response;
            this.parseRulesAsyncTaskListener = parseRulesAsyncTaskListener;
        }

        @Override
        protected ArrayList<Rule> doInBackground(Void... voids) {
            try {
                JSONArray rulesArray = new JSONObject(response).getJSONArray(JSONUtils.RULES_KEY);
                ArrayList<Rule> rules = new ArrayList<>();
                for (int i = 0; i < rulesArray.length(); i++) {
                    String shortName = rulesArray.getJSONObject(i).getString(JSONUtils.SHORT_NAME_KEY);
                    String description = null;
                    if (rulesArray.getJSONObject(i).has(JSONUtils.DESCRIPTION_KEY)) {
                        description = Utils.modifyMarkdown(rulesArray.getJSONObject(i).getString(JSONUtils.DESCRIPTION_KEY));
                    }
                    rules.add(new Rule(shortName, description));
                }
                return rules;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Rule> rules) {
            if (rules != null) {
                parseRulesAsyncTaskListener.parseSuccessful(rules);
            } else {
                parseRulesAsyncTaskListener.parseFailed();
            }
        }

        interface ParseRulesAsyncTaskListener {
            void parseSuccessful(ArrayList<Rule> rules);

            void parseFailed();
        }
    }
}
