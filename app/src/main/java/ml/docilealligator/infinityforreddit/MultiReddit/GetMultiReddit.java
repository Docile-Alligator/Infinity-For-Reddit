package ml.docilealligator.infinityforreddit.MultiReddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GetMultiReddit {
    public interface GetMultiRedditListener {
        void success(ArrayList<MultiReddit> multiReddits);
        void failed();
    }

    public static void getMyMultiReddits(Retrofit oauthRetrofit, String accessToken, GetMultiRedditListener getMultiRedditListener) {
        oauthRetrofit.create(RedditAPI.class)
                .getMyMultiReddits(RedditUtils.getOAuthHeader(accessToken)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseMultiRedditsListAsyncTask(response.body(), new ParseMultiRedditsListAsyncTask.ParseMultiRedditsListAsyncTaskListener() {
                        @Override
                        public void success(ArrayList<MultiReddit> multiReddits) {
                            getMultiRedditListener.success(multiReddits);
                        }

                        @Override
                        public void failed() {
                            getMultiRedditListener.failed();
                        }
                    }).execute();
                } else {
                    getMultiRedditListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                getMultiRedditListener.failed();
            }
        });
    }

    private static class ParseMultiRedditsListAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray arrayResponse;
        private boolean parseFailed;
        private ArrayList<MultiReddit> multiReddits;
        private ParseMultiRedditsListAsyncTaskListener parseMultiRedditsListAsyncTaskListener;

        interface ParseMultiRedditsListAsyncTaskListener {
            void success(ArrayList<MultiReddit> multiReddits);
            void failed();
        }

        ParseMultiRedditsListAsyncTask(String response,
                                       ParseMultiRedditsListAsyncTaskListener parseMultiRedditsListAsyncTaskListener) {
            this.parseMultiRedditsListAsyncTaskListener = parseMultiRedditsListAsyncTaskListener;
            try {
                arrayResponse = new JSONArray(response);
                multiReddits = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!parseFailed) {
                for (int i = 0; i < arrayResponse.length(); i++) {
                    try {
                        JSONObject singleMultiReddit = arrayResponse.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                        String displayName = singleMultiReddit.getString(JSONUtils.DISPLAY_NAME);
                        String name = singleMultiReddit.getString(JSONUtils.NAME_KEY);
                        String description = singleMultiReddit.getString(JSONUtils.DESCRIPTION_HTML_KEY);
                        int nSubscribers = singleMultiReddit.getInt(JSONUtils.NUM_SUBSCRIBERS_KEY);
                        String copiedFrom = singleMultiReddit.getString(JSONUtils.COPIED_FROM_KEY);
                        String iconUrl = singleMultiReddit.getString(JSONUtils.ICON_URL_KEY);
                        long createdUTC = singleMultiReddit.getLong(JSONUtils.CREATED_UTC_KEY);
                        String visibility = singleMultiReddit.getString(JSONUtils.VISIBILITY_KEY);
                        boolean over18 = singleMultiReddit.getBoolean(JSONUtils.OVER_18_KEY);
                        String path = singleMultiReddit.getString(JSONUtils.PATH_KEY);
                        String owner = singleMultiReddit.getString(JSONUtils.OWNER_KEY);
                        boolean isSubscriber = singleMultiReddit.getBoolean(JSONUtils.IS_SUBSCRIBER_KEY);
                        boolean isFavorited = singleMultiReddit.getBoolean(JSONUtils.IS_FAVORITED_KEY);

                        JSONArray subredditsArray = singleMultiReddit.getJSONArray(JSONUtils.SUBREDDITS_KEY);
                        ArrayList<String> subreddits = new ArrayList<>();
                        for (int j = 0; j < subredditsArray.length(); j++) {
                            subreddits.add(subredditsArray.getJSONObject(j).getString(JSONUtils.NAME_KEY));
                        }

                        MultiReddit multiReddit = new MultiReddit(path, displayName, name, description, copiedFrom,
                                iconUrl, visibility, owner, nSubscribers, createdUTC, over18, isSubscriber,
                                isFavorited, subreddits);
                        multiReddits.add(multiReddit);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!parseFailed) {
                parseMultiRedditsListAsyncTaskListener.success(multiReddits);
            } else {
                parseMultiRedditsListAsyncTaskListener.failed();
            }
        }
    }
}
