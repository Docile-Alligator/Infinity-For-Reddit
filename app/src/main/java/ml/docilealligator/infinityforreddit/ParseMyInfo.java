package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ParseMyInfo {
    interface ParseMyInfoListener {
        void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma);
        void onParseMyInfoFail();
    }

    static void parseMyInfo(String response, ParseMyInfoListener parseMyInfoListener) {
        new ParseMyInfoAsyncTask(response, parseMyInfoListener).execute();
    }

    private static class ParseMyInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseMyInfoListener parseMyInfoListener;
        private boolean parseFailed;

        private String name;
        private String profileImageUrl;
        private String bannerImageUrl;
        private int karma;

        ParseMyInfoAsyncTask(String response, ParseMyInfoListener parseMyInfoListener){
            try {
                jsonResponse = new JSONObject(response);
                this.parseMyInfoListener = parseMyInfoListener;
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("user info json error", e.getMessage());
                parseMyInfoListener.onParseMyInfoFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                name = jsonResponse.getString(JSONUtils.NAME_KEY);
                profileImageUrl = Html.fromHtml(jsonResponse.getString(JSONUtils.ICON_IMG_KEY)).toString();
                if(!jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) {
                    bannerImageUrl = Html.fromHtml(jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY)).toString();
                }
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
                parseMyInfoListener.onParseMyInfoSuccess(name, profileImageUrl, bannerImageUrl, karma);
            } else {
                parseMyInfoListener.onParseMyInfoFail();
            }
        }
    }
}
