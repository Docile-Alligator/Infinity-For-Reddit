package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class ParseSubscribedThing {
    interface ParseSubscribedSubredditsListener {
        void onParseSubscribedSubredditsSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                ArrayList<SubscribedUserData> subscribedUserData,
                                                String lastItem);
        void onParseSubscribedSubredditsFail();
    }

    private ParseSubscribedSubredditsListener mParseSubscribedSubredditsListener;

    void parseSubscribedSubreddits(String response, ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                   ArrayList<SubscribedUserData> subscribedUserData,
                                   ParseSubscribedSubredditsListener parseSubscribedSubredditsListener) {
        mParseSubscribedSubredditsListener = parseSubscribedSubredditsListener;
        new ParseSubscribedSubredditsAsyncTask(response, subscribedSubredditData, subscribedUserData).execute();
    }

    private class ParseSubscribedSubredditsAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private boolean parseFailed;
        private String lastItem;
        private ArrayList<SubscribedSubredditData> subscribedSubredditData;
        private ArrayList<SubscribedUserData> subscribedUserData;
        private ArrayList<SubscribedSubredditData> newSubscribedSubredditData;
        private ArrayList<SubscribedUserData> newSubscribedUserData;

        ParseSubscribedSubredditsAsyncTask(String response, ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                           ArrayList<SubscribedUserData> subscribedUserData){
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
                this.subscribedSubredditData = subscribedSubredditData;
                this.subscribedUserData = subscribedUserData;
                newSubscribedSubredditData = new ArrayList<>();
                newSubscribedUserData = new ArrayList<>();
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
                    String id = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.NAME_KEY);
                    if(iconUrl.equals("null")) {
                        iconUrl = "";
                    }

                    if(children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.SUBREDDIT_TYPE_KEY)
                            .equals(JSONUtils.SUBREDDIT_TYPE_VALUE_USER)) {
                        //It's a user
                        newSubscribedUserData.add(new SubscribedUserData(id, name.substring(2), iconUrl));
                    } else {
                        newSubscribedSubredditData.add(new SubscribedSubredditData(id, name, iconUrl));
                    }
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
                subscribedSubredditData.addAll(newSubscribedSubredditData);
                subscribedUserData.addAll(newSubscribedUserData);
                mParseSubscribedSubredditsListener.onParseSubscribedSubredditsSuccess(subscribedSubredditData,
                        subscribedUserData, lastItem);
            } else {
                mParseSubscribedSubredditsListener.onParseSubscribedSubredditsFail();
            }
        }
    }
}
