package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.User.UserData;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;

public class ParseUserData {
    static void parseUserData(String response, ParseUserDataListener parseUserDataListener) {
        new ParseUserDataAsyncTask(response, parseUserDataListener).execute();
    }

    static void parseUserListingData(String response, ParseUserListingDataListener parseUserListingDataListener) {
        new ParseUserListingDataAsyncTask(response, parseUserListingDataListener).execute();
    }

    private static UserData parseUserDataBase(JSONObject userDataJson) throws JSONException {
        if(userDataJson == null) {
            return null;
        }

        userDataJson = userDataJson.getJSONObject(JSONUtils.DATA_KEY);
        String userName = userDataJson.getString(JSONUtils.NAME_KEY);
        String iconImageUrl = userDataJson.getString(JSONUtils.ICON_IMG_KEY);
        String bannerImageUrl = "";
        boolean canBeFollowed;
        if (userDataJson.has(JSONUtils.SUBREDDIT_KEY) && !userDataJson.isNull(JSONUtils.SUBREDDIT_KEY)) {
            bannerImageUrl = userDataJson.getJSONObject(JSONUtils.SUBREDDIT_KEY).getString(JSONUtils.BANNER_IMG_KEY);
            canBeFollowed = true;
        } else {
            canBeFollowed = false;
        }
        int linkKarma = userDataJson.getInt(JSONUtils.LINK_KARMA_KEY);
        int commentKarma = userDataJson.getInt(JSONUtils.COMMENT_KARMA_KEY);
        long cakeday = userDataJson.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        boolean isGold = userDataJson.getBoolean(JSONUtils.IS_GOLD_KEY);
        boolean isFriend = userDataJson.getBoolean(JSONUtils.IS_FRIEND_KEY);
        String description = userDataJson.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY);

        return new UserData(userName, iconImageUrl, bannerImageUrl, linkKarma, commentKarma, cakeday,
                isGold, isFriend, canBeFollowed, description);
    }

    interface ParseUserDataListener {
        void onParseUserDataSuccess(UserData userData);

        void onParseUserDataFailed();
    }

    interface ParseUserListingDataListener {
        void onParseUserListingDataSuccess(ArrayList<UserData> userData, String after);

        void onParseUserListingDataFailed();
    }

    private static class ParseUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private ParseUserDataListener parseUserDataListener;
        private boolean parseFailed;

        private UserData userData;

        ParseUserDataAsyncTask(String response, ParseUserDataListener parseUserDataListener) {
            try {
                jsonResponse = new JSONObject(response);
                this.parseUserDataListener = parseUserDataListener;
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseUserDataListener.onParseUserDataFailed();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                userData = parseUserDataBase(jsonResponse);
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                parseUserDataListener.onParseUserDataSuccess(userData);
            } else {
                parseUserDataListener.onParseUserDataFailed();
            }
        }
    }

    private static class ParseUserListingDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private String response;
        private JSONObject jsonResponse;
        private ParseUserListingDataListener parseUserListingDataListener;
        private String after;
        private boolean parseFailed;

        private ArrayList<UserData> userDataArrayList;

        ParseUserListingDataAsyncTask(String response, ParseUserListingDataListener parseUserListingDataListener) {
            this.parseUserListingDataListener = parseUserListingDataListener;
            this.response = response;
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
                userDataArrayList = new ArrayList<>();
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (!parseFailed) {
                    after = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                    JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                    for (int i = 0; i < children.length(); i++) {
                        userDataArrayList.add(parseUserDataBase(children.getJSONObject(i)));
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
