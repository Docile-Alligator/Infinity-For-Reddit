package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import SubredditDatabase.SubredditData;

class ParseSubredditData {
    interface ParseSubredditDataListener {
        void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);
        void onParseSubredditDataFail();
    }

    interface ParseSubredditListingDataListener {
        void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);
        void onParseSubredditListingDataFail();
    }

    static void parseSubredditData(String response, ParseSubredditDataListener parseSubredditDataListener) {
        new ParseSubredditDataAsyncTask(response, parseSubredditDataListener).execute();
    }

    static void parseSubredditListingData(String response, ParseSubredditListingDataListener parseSubredditListingDataListener) {
        new ParseSubredditListingDataAsyncTask(response, parseSubredditListingDataListener).execute();
    }

    private static class ParseSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private boolean parseFailed;
        private ParseSubredditDataListener parseSubredditDataListener;
        private SubredditData subredditData;
        private int mNCurrentOnlineSubscribers;

        ParseSubredditDataAsyncTask(String response, ParseSubredditDataListener parseSubredditDataListener){
            this.parseSubredditDataListener = parseSubredditDataListener;
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("subreddit json error", e.getMessage());
                parseSubredditDataListener.onParseSubredditDataFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject data = jsonResponse.getJSONObject(JSONUtils.DATA_KEY);
                mNCurrentOnlineSubscribers = data.getInt(JSONUtils.ACTIVE_USER_COUNT_KEY);
                subredditData = parseSubredditData(data);
                /*String id = data.getString(JSONUtils.EXTRA_NAME);
                String subredditFullName = data.getString(JSONUtils.DISPLAY_NAME);
                String description = data.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY).trim();

                String bannerImageUrl;
                if(data.isNull(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY)) {
                    bannerImageUrl = "";
                } else {
                    bannerImageUrl = data.getString(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY);
                }
                if(bannerImageUrl.equals("") && !data.isNull(JSONUtils.BANNER_IMG_KEY)) {
                    bannerImageUrl= data.getString(JSONUtils.BANNER_IMG_KEY);
                }

                String iconUrl;
                if(data.isNull(JSONUtils.COMMUNITY_ICON_KEY)) {
                    iconUrl = "";
                } else {
                    iconUrl = data.getString(JSONUtils.COMMUNITY_ICON_KEY);
                }
                if(iconUrl.equals("") && !data.isNull(JSONUtils.ICON_IMG_KEY)) {
                    iconUrl = data.getString(JSONUtils.ICON_IMG_KEY);
                }

                int nSubscribers = data.getInt(JSONUtils.SUBSCRIBERS_KEY);
                int nCurrentOnlineSubscribers = data.getInt(JSONUtils.ACTIVE_USER_COUNT_KEY);
                subredditData = new SubredditData(id, subredditFullName, iconUrl, bannerImageUrl, description, nSubscribers);
                mNCurrentOnlineSubscribers = nCurrentOnlineSubscribers;*/
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse", "SubredditData error");
                parseSubredditDataListener.onParseSubredditDataFail();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseSubredditDataListener.onParseSubredditDataSuccess(subredditData, mNCurrentOnlineSubscribers);
            } else {
                parseSubredditDataListener.onParseSubredditDataFail();
            }
        }
    }

    private static class ParseSubredditListingDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private boolean parseFailed;
        private ParseSubredditListingDataListener parseSubredditListingDataListener;
        private ArrayList<SubredditData> subredditListingData;
        private String after;

        ParseSubredditListingDataAsyncTask(String response, ParseSubredditListingDataListener parseSubredditListingDataListener){
            this.parseSubredditListingDataListener = parseSubredditListingDataListener;
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
                subredditListingData = new ArrayList<>();
            } catch (JSONException e) {
                Log.i("subreddit json error", e.getMessage());
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(!parseFailed) {
                    JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY)
                            .getJSONArray(JSONUtils.CHILDREN_KEY);
                    for(int i = 0; i < children.length(); i++) {
                        JSONObject data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                        SubredditData subredditData = parseSubredditData(data);
                        subredditListingData.add(subredditData);
                    }
                    after = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                }
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse", "SubredditDataListing error");
                parseSubredditListingDataListener.onParseSubredditListingDataFail();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseSubredditListingDataListener.onParseSubredditListingDataSuccess(subredditListingData, after);
            } else {
                parseSubredditListingDataListener.onParseSubredditListingDataFail();
            }
        }
    }

    private static SubredditData parseSubredditData(JSONObject subredditDataJsonObject) throws JSONException {
        String id = subredditDataJsonObject.getString(JSONUtils.NAME_KEY);
        String subredditFullName = subredditDataJsonObject.getString(JSONUtils.DISPLAY_NAME);
        String description = subredditDataJsonObject.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY).trim();

        String bannerImageUrl;
        if(subredditDataJsonObject.isNull(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY)) {
            bannerImageUrl = "";
        } else {
            bannerImageUrl = subredditDataJsonObject.getString(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY);
        }
        if(bannerImageUrl.equals("") && !subredditDataJsonObject.isNull(JSONUtils.BANNER_IMG_KEY)) {
            bannerImageUrl= subredditDataJsonObject.getString(JSONUtils.BANNER_IMG_KEY);
        }

        String iconUrl;
        if(subredditDataJsonObject.isNull(JSONUtils.COMMUNITY_ICON_KEY)) {
            iconUrl = "";
        } else {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.COMMUNITY_ICON_KEY);
        }
        if(iconUrl.equals("") && !subredditDataJsonObject.isNull(JSONUtils.ICON_IMG_KEY)) {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.ICON_IMG_KEY);
        }

        int nSubscribers = 0;
        if(!subredditDataJsonObject.isNull(JSONUtils.SUBSCRIBERS_KEY)) {
            nSubscribers = subredditDataJsonObject.getInt(JSONUtils.SUBSCRIBERS_KEY);
        }

        return new SubredditData(id, subredditFullName, iconUrl, bannerImageUrl, description, nSubscribers);
    }
}
