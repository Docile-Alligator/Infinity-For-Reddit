package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseAndSaveAccountInfo {
    public interface ParseAndSaveAccountInfoListener {
        void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma);
        void onParseMyInfoFail();
    }

    public static void parseAndSaveAccountInfo(String response, RedditDataRoomDatabase redditDataRoomDatabase,
                                               ParseAndSaveAccountInfoListener parseAndSaveAccountInfoListener) {
        new ParseAndSaveAccountInfoAsyncTask(response, redditDataRoomDatabase, parseAndSaveAccountInfoListener).execute();
    }

    private static class ParseAndSaveAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private ParseAndSaveAccountInfoListener parseAndSaveAccountInfoListener;
        private boolean parseFailed;

        private String name;
        private String profileImageUrl;
        private String bannerImageUrl;
        private int karma;

        ParseAndSaveAccountInfoAsyncTask(String response, RedditDataRoomDatabase redditDataRoomDatabase,
                                         ParseAndSaveAccountInfoListener parseAndSaveAccountInfoListener){
            try {
                jsonResponse = new JSONObject(response);
                this.redditDataRoomDatabase = redditDataRoomDatabase;
                this.parseAndSaveAccountInfoListener = parseAndSaveAccountInfoListener;
                parseFailed = false;
            } catch (JSONException e) {
                parseAndSaveAccountInfoListener.onParseMyInfoFail();
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

                redditDataRoomDatabase.accountDao().updateAccountInfo(name, profileImageUrl, bannerImageUrl, karma);
            } catch (JSONException e) {
                parseFailed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseAndSaveAccountInfoListener.onParseMyInfoSuccess(name, profileImageUrl, bannerImageUrl, karma);
            } else {
                parseAndSaveAccountInfoListener.onParseMyInfoFail();
            }
        }
    }
}
