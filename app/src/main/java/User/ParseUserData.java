package User;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.JSONUtils;

public class ParseUserData {
    interface ParseUserDataListener {
        void onParseUserDataSuccess(User user);
        void onParseUserDataFail();
    }

    static void parseMyInfo(String response, ParseUserDataListener parseUserDataListener) {
        new ParseUserDataAsyncTask(response, parseUserDataListener).execute();
    }

    private static class ParseUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserDataListener parseUserDataListener;
        private boolean parseFailed;

        private User user;

        ParseUserDataAsyncTask(String response, ParseUserDataListener parseUserDataListener){
            try {
                jsonResponse = new JSONObject(response);
                this.parseUserDataListener = parseUserDataListener;
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("user data json error", e.getMessage());
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
                if(!jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) {
                    bannerImageUrl = jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
                }
                int linkKarma = jsonResponse.getInt(JSONUtils.LINK_KARMA_KEY);
                int commentKarma = jsonResponse.getInt(JSONUtils.COMMENT_KARMA_KEY);
                int karma = linkKarma + commentKarma;
                boolean isGold = jsonResponse.getBoolean(JSONUtils.IS_GOLD_KEY);
                boolean isFriend = jsonResponse.getBoolean(JSONUtils.IS_FRIEND_KEY);

                user = new User(userName, iconImageUrl, bannerImageUrl, karma, isGold, isFriend);
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse user data error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseUserDataListener.onParseUserDataSuccess(user);
            } else {
                parseUserDataListener.onParseUserDataFail();
            }
        }
    }
}
