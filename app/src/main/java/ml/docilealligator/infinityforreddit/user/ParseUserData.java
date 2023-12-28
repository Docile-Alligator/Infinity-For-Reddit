package ml.docilealligator.infinityforreddit.user;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;

public class ParseUserData {
    static void parseUserData(RedditDataRoomDatabase redditDataRoomDatabase, String response,
                              ParseUserDataListener parseUserDataListener) {
        new ParseUserDataAsyncTask(redditDataRoomDatabase, response, parseUserDataListener).execute();
    }

    static void parseUserListingData(String response, ParseUserListingDataListener parseUserListingDataListener) {
        new ParseUserListingDataAsyncTask(response, parseUserListingDataListener).execute();
    }

    private static UserData parseUserDataBase(JSONObject userDataJson, boolean parseFullKarma) throws JSONException {
        if (userDataJson == null) {
            return null;
        }

        userDataJson = userDataJson.getJSONObject(JSONUtils.DATA_KEY);
        String userName = userDataJson.getString(JSONUtils.NAME_KEY);
        String iconImageUrl = userDataJson.getString(JSONUtils.ICON_IMG_KEY);
        String bannerImageUrl = "";
        boolean canBeFollowed;
        if (!userDataJson.isNull(JSONUtils.SUBREDDIT_KEY)) {
            bannerImageUrl = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
            canBeFollowed = true;
        } else {
            canBeFollowed = false;
        }
        int linkKarma = userDataJson.getInt(JSONUtils.LINK_KARMA_KEY);
        int commentKarma = userDataJson.getInt(JSONUtils.COMMENT_KARMA_KEY);
        int awarderKarma = 0;
        int awardeeKarma = 0;
        int totalKarma = linkKarma + commentKarma;
        if (parseFullKarma) {
            awarderKarma = userDataJson.getInt(JSONUtils.AWARDER_KARMA_KEY);
            awardeeKarma = userDataJson.getInt(JSONUtils.AWARDEE_KARMA_KEY);
            totalKarma = userDataJson.getInt(JSONUtils.TOTAL_KARMA_KEY);
        }
        long cakeday = userDataJson.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        boolean isGold = userDataJson.getBoolean(JSONUtils.IS_GOLD_KEY);
        boolean isFriend = userDataJson.getBoolean(JSONUtils.IS_FRIEND_KEY);
        boolean isNsfw = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getBoolean(JSONUtils.OVER_18_KEY);
        String description = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.PUBLIC_DESCRIPTION_KEY);
        String title = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.TITLE_KEY);

        return new UserData(userName, iconImageUrl, bannerImageUrl, linkKarma, commentKarma, awarderKarma,
                awardeeKarma, totalKarma, cakeday, isGold, isFriend, canBeFollowed, isNsfw, description, title);
    }

    interface ParseUserDataListener {
        void onParseUserDataSuccess(UserData userData, int inboxCount);

        void onParseUserDataFailed();
    }

    interface ParseUserListingDataListener {
        void onParseUserListingDataSuccess(ArrayList<UserData> userData, String after);

        void onParseUserListingDataFailed();
    }

    private static class ParseUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private final RedditDataRoomDatabase redditDataRoomDatabase;
        private final String response;
        private final ParseUserDataListener parseUserDataListener;
        private boolean parseFailed = false;

        private UserData userData;
        private int inboxCount = -1;

        ParseUserDataAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String response, ParseUserDataListener parseUserDataListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.response = response;
            this.parseUserDataListener = parseUserDataListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                userData = parseUserDataBase(jsonResponse, true);
                if (redditDataRoomDatabase != null) {
                    redditDataRoomDatabase.accountDao().updateAccountInfo(userData.getName(), userData.getIconUrl(), userData.getBanner(), userData.getTotalKarma());
                }
                if (jsonResponse.getJSONObject(JSONUtils.DATA_KEY).has(JSONUtils.INBOX_COUNT_KEY)) {
                    inboxCount = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getInt(JSONUtils.INBOX_COUNT_KEY);
                }
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                parseUserDataListener.onParseUserDataSuccess(userData, inboxCount);
            } else {
                parseUserDataListener.onParseUserDataFailed();
            }
        }
    }

    private static class ParseUserListingDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String response;
        private final ParseUserListingDataListener parseUserListingDataListener;
        private String after;
        private boolean parseFailed = false;

        private final ArrayList<UserData> userDataArrayList = new ArrayList<>();

        ParseUserListingDataAsyncTask(String response, ParseUserListingDataListener parseUserListingDataListener) {
            this.parseUserListingDataListener = parseUserListingDataListener;
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                after = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                for (int i = 0; i < children.length(); i++) {
                    try {
                        UserData userData = parseUserDataBase(children.getJSONObject(i), false);
                        userDataArrayList.add(userData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                parseUserListingDataListener.onParseUserListingDataSuccess(userDataArrayList, after);
            } else {
                if (response.equals("\"{}\"")) {
                    parseUserListingDataListener.onParseUserListingDataSuccess(new ArrayList<>(), null);
                } else {
                    parseUserListingDataListener.onParseUserListingDataFailed();
                }
            }
        }
    }
}
