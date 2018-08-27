package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ParseUserInfo {
    interface ParseUserInfoListener {
        void onParseUserInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma);
        void onParseUserInfoFail();
    }

    static void parseUserInfo(String response, ParseUserInfoListener parseUserInfoListener) {
        new ParseUserInfoAsyncTask(response, parseUserInfoListener).execute();
    }

    private static class ParseUserInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserInfoListener parseUserInfoListener;
        private boolean parseFailed;

        private String name;
        private String profileImageUrl;
        private String bannerImageUrl;
        private int karma;

        ParseUserInfoAsyncTask(String response, ParseUserInfoListener parseUserInfoListener){
            try {
                jsonResponse = new JSONObject(response);
                this.parseUserInfoListener = parseUserInfoListener;
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("user info json error", e.getMessage());
                parseUserInfoListener.onParseUserInfoFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                name = jsonResponse.getString(JSONUtils.NAME_KEY);
                profileImageUrl = Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString();
                bannerImageUrl = Html.fromHtml(jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY)).toString();
                int linkKarma = jsonResponse.getInt(JSONUtils.LINK_KARMA_KEY);
                int commentKarma = jsonResponse.getInt(JSONUtils.COMMENT_KARMA_KEY);
                karma = linkKarma + commentKarma;
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse comment error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseUserInfoListener.onParseUserInfoSuccess(name, profileImageUrl, bannerImageUrl, karma);
            } else {
                parseUserInfoListener.onParseUserInfoFail();
            }
        }
    }
}
