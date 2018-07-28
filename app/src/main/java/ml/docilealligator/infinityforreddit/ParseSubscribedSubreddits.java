package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class ParseSubscribedSubreddits {
    interface ParseSubscribedSubredditsListener {
        void onParseSubscribedSubredditsSuccess(ArrayList<SubredditData> subredditData, String lastItem);
        void onParseSubscribedSubredditsFail();
    }

    private ParseSubscribedSubredditsListener mParseSubscribedSubredditsListener;

    void parseSubscribedSubreddits(String response, ArrayList<SubredditData> subredditData,
                                   ParseSubscribedSubredditsListener parseSubscribedSubredditsListener) {
        mParseSubscribedSubredditsListener = parseSubscribedSubredditsListener;
        new ParseSubscribedSubredditsAsyncTask(response, subredditData).execute();
    }

    private class ParseSubscribedSubredditsAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private boolean parseFailed;
        private String lastItem;
        private ArrayList<SubredditData> subredditData;
        private ArrayList<SubredditData> newSubredditData;

        ParseSubscribedSubredditsAsyncTask(String response, ArrayList<SubredditData> subredditData){
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
                this.subredditData = subredditData;
                newSubredditData = new ArrayList<>();
            } catch (JSONException e) {
                Log.i("user info json error", e.getMessage());
                mParseSubscribedSubredditsListener.onParseSubscribedSubredditsFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                for(int i = 0; i < children.length(); i++) {
                    String name = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.DISPLAY_NAME);
                    String iconUrl = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.ICON_IMG_KEY);
                    newSubredditData.add(new SubredditData(name, iconUrl));
                }
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse comment error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                subredditData.addAll(newSubredditData);
                mParseSubscribedSubredditsListener.onParseSubscribedSubredditsSuccess(subredditData, lastItem);
            } else {
                mParseSubscribedSubredditsListener.onParseSubscribedSubredditsFail();
            }
        }
    }
}
