package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import User.UserData;

public class ParseUserData {
    interface ParseUserDataListener {
        void onParseUserDataSuccess(UserData userData);
        void onParseUserDataFail();
    }

    static void parseUserData(String response, ParseUserDataListener parseUserDataListener) {
        new ParseUserDataAsyncTask(response, parseUserDataListener).execute();
    }

    private static class ParseUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserDataListener parseUserDataListener;
        private boolean parseFailed;

        private UserData userData;

        ParseUserDataAsyncTask(String response, ParseUserDataListener parseUserDataListener){
            try {
                Log.i("response", response);
                jsonResponse = new JSONObject(response);
                this.parseUserDataListener = parseUserDataListener;
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("userdata json error", e.getMessage());
                parseUserDataListener.onParseUserDataFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                jsonResponse = jsonResponse.getJSONObject(JSONUtils.DATA_KEY);
                String userName = jsonResponse.getString(JSONUtils.NAME_KEY);
                String iconImageUrl = jsonResponse.getString(JSONUtils.ICON_IMG_KEY);
                String bannerImageUrl = "";
                if(jsonResponse.has(JSONUtils.SUBREDDIT_KEY) && !jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) {
                    bannerImageUrl = jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
                }
                int linkKarma = jsonResponse.getInt(JSONUtils.LINK_KARMA_KEY);
                int commentKarma = jsonResponse.getInt(JSONUtils.COMMENT_KARMA_KEY);
                int karma = linkKarma + commentKarma;
                boolean isGold = jsonResponse.getBoolean(JSONUtils.IS_GOLD_KEY);
                boolean isFriend = jsonResponse.getBoolean(JSONUtils.IS_FRIEND_KEY);

                userData = new UserData(userName, iconImageUrl, bannerImageUrl, karma, isGold, isFriend);
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse user data error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseUserDataListener.onParseUserDataSuccess(userData);
            } else {
                parseUserDataListener.onParseUserDataFail();
            }
        }
    }
}
