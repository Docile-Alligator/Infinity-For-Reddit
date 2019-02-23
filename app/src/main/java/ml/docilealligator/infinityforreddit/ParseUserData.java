package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import User.UserData;

public class ParseUserData {
    interface ParseUserDataListener {
        void onParseUserDataSuccess(UserData userData);
        void onParseUserDataFailed();
    }

    interface ParseUserListingDataListener {
        void onParseUserListingDataSuccess(ArrayList<UserData> userData, String after);
        void onParseUserListingDataFailed();
    }

    static void parseUserData(String response, ParseUserDataListener parseUserDataListener) {
        new ParseUserDataAsyncTask(response, parseUserDataListener).execute();
    }

    static void parseUserListingData(String response, ParseUserListingDataListener parseUserListingDataListener) {
        new ParseUserListingDataAsyncTask(response, parseUserListingDataListener).execute();
    }

    private static class ParseUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserDataListener parseUserDataListener;
        private boolean parseFailed;

        private UserData userData;

        ParseUserDataAsyncTask(String response, ParseUserDataListener parseUserDataListener){
            try {
                jsonResponse = new JSONObject(response);
                this.parseUserDataListener = parseUserDataListener;
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("userdata json error", e.getMessage());
                parseUserDataListener.onParseUserDataFailed();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                userData = parseUserDataBase(jsonResponse);
                /*jsonResponse = jsonResponse.getJSONObject(JSONUtils.DATA_KEY);
                String userName = jsonResponse.getString(JSONUtils.NAME_KEY);
                String iconImageUrl = jsonResponse.getString(JSONUtils.ICON_IMG_KEY);
                String bannerImageUrl = "";
                boolean canBeFollowed;
                if(jsonResponse.has(JSONUtils.SUBREDDIT_KEY) && !jsonResponse.isNull(JSONUtils.SUBREDDIT_KEY)) {
                    bannerImageUrl = jsonResponse.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
                    canBeFollowed = true;
                } else {
                    canBeFollowed = false;
                }
                int linkKarma = jsonResponse.getInt(JSONUtils.LINK_KARMA_KEY);
                int commentKarma = jsonResponse.getInt(JSONUtils.COMMENT_KARMA_KEY);
                int karma = linkKarma + commentKarma;
                boolean isGold = jsonResponse.getBoolean(JSONUtils.IS_GOLD_KEY);
                boolean isFriend = jsonResponse.getBoolean(JSONUtils.IS_FRIEND_KEY);

                userData = new UserData(userName, iconImageUrl, bannerImageUrl, karma, isGold, isFriend, canBeFollowed);*/
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
                parseUserDataListener.onParseUserDataFailed();
            }
        }
    }

    private static class ParseUserListingDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserListingDataListener parseUserListingDataListener;
        private String after;
        private boolean parseFailed;

        private ArrayList<UserData> userDataArrayList;

        ParseUserListingDataAsyncTask(String response, ParseUserListingDataListener parseUserListingDataListener){
            try {
                jsonResponse = new JSONObject(response);
                this.parseUserListingDataListener = parseUserListingDataListener;
                parseFailed = false;
                userDataArrayList = new ArrayList<>();
            } catch (JSONException e) {
                Log.i("userdata json error", e.getMessage());
                this.parseUserListingDataListener.onParseUserListingDataFailed();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                after = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                for(int i = 0; i < children.length(); i++) {
                    userDataArrayList.add(parseUserDataBase(children.getJSONObject(i)));
                }
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse user data error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseUserListingDataListener.onParseUserListingDataSuccess(userDataArrayList, after);
            } else {
                parseUserListingDataListener.onParseUserListingDataFailed();
            }
        }
    }

    private static UserData parseUserDataBase(JSONObject userDataJson) throws JSONException {
        userDataJson = userDataJson.getJSONObject(JSONUtils.DATA_KEY);
        String userName = userDataJson.getString(JSONUtils.NAME_KEY);
        String iconImageUrl = userDataJson.getString(JSONUtils.ICON_IMG_KEY);
        String bannerImageUrl = "";
        boolean canBeFollowed;
        if(userDataJson.has(JSONUtils.SUBREDDIT_KEY) && !userDataJson.isNull(JSONUtils.SUBREDDIT_KEY)) {
            bannerImageUrl = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
            canBeFollowed = true;
        } else {
            canBeFollowed = false;
        }
        int linkKarma = userDataJson.getInt(JSONUtils.LINK_KARMA_KEY);
        int commentKarma = userDataJson.getInt(JSONUtils.COMMENT_KARMA_KEY);
        int karma = linkKarma + commentKarma;
        boolean isGold = userDataJson.getBoolean(JSONUtils.IS_GOLD_KEY);
        boolean isFriend = userDataJson.getBoolean(JSONUtils.IS_FRIEND_KEY);

        return new UserData(userName, iconImageUrl, bannerImageUrl, karma, isGold, isFriend, canBeFollowed);
    }
}
