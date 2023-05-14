package ml.docilealligator.infinityforreddit.subreddit;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ParseSubredditData {
    public static void parseSubredditData(String response, ParseSubredditDataListener parseSubredditDataListener) {
        new ParseSubredditDataAsyncTask(response, parseSubredditDataListener).execute();
    }

    public static void parseSubredditListingData(String response, boolean nsfw, ParseSubredditListingDataListener parseSubredditListingDataListener) {
        new ParseSubredditListingDataAsyncTask(response, nsfw, parseSubredditListingDataListener).execute();
    }

    @Nullable
    private static SubredditData parseSubredditData(JSONObject subredditDataJsonObject, boolean nsfw) throws JSONException {
        boolean isNSFW = !subredditDataJsonObject.isNull(JSONUtils.OVER18_KEY) && subredditDataJsonObject.getBoolean(JSONUtils.OVER18_KEY);
        if (!nsfw && isNSFW) {
            return null;
        }
        String id = subredditDataJsonObject.getString(JSONUtils.NAME_KEY);
        String subredditFullName = subredditDataJsonObject.getString(JSONUtils.DISPLAY_NAME_KEY);
        String description = subredditDataJsonObject.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY).trim();
        String sidebarDescription = Utils.modifyMarkdown(subredditDataJsonObject.getString(JSONUtils.DESCRIPTION_KEY).trim());
        long createdUTC = subredditDataJsonObject.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        String suggestedCommentSort = subredditDataJsonObject.getString(JSONUtils.SUGGESTED_COMMENT_SORT_KEY);

        String bannerImageUrl;
        if (subredditDataJsonObject.isNull(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY)) {
            bannerImageUrl = "";
        } else {
            bannerImageUrl = subredditDataJsonObject.getString(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY);
        }
        if (bannerImageUrl.isEmpty() && !subredditDataJsonObject.isNull(JSONUtils.BANNER_IMG_KEY)) {
            bannerImageUrl = subredditDataJsonObject.getString(JSONUtils.BANNER_IMG_KEY);
        }

        String iconUrl;
        if (subredditDataJsonObject.isNull(JSONUtils.COMMUNITY_ICON_KEY)) {
            iconUrl = "";
        } else {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.COMMUNITY_ICON_KEY);
        }
        if (iconUrl.isEmpty() && !subredditDataJsonObject.isNull(JSONUtils.ICON_IMG_KEY)) {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.ICON_IMG_KEY);
        }

        int nSubscribers = 0;
        if (!subredditDataJsonObject.isNull(JSONUtils.SUBSCRIBERS_KEY)) {
            nSubscribers = subredditDataJsonObject.getInt(JSONUtils.SUBSCRIBERS_KEY);
        }

        return new SubredditData(id, subredditFullName, iconUrl, bannerImageUrl, description,
                sidebarDescription, nSubscribers, createdUTC, suggestedCommentSort, isNSFW);
    }

    interface ParseSubredditDataListener {
        void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);

        void onParseSubredditDataFail();
    }

    public interface ParseSubredditListingDataListener {
        void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);

        void onParseSubredditListingDataFail();
    }

    private static class ParseSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String response;
        private JSONObject jsonResponse;
        private boolean parseFailed = false;
        private final ParseSubredditDataListener parseSubredditDataListener;
        private SubredditData subredditData;
        private int mNCurrentOnlineSubscribers;

        ParseSubredditDataAsyncTask(String response, ParseSubredditDataListener parseSubredditDataListener) {
            this.parseSubredditDataListener = parseSubredditDataListener;
            this.response = response;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject data = jsonResponse.getJSONObject(JSONUtils.DATA_KEY);
                mNCurrentOnlineSubscribers = data.getInt(JSONUtils.ACTIVE_USER_COUNT_KEY);
                subredditData = parseSubredditData(data, true);
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                parseSubredditDataListener.onParseSubredditDataSuccess(subredditData, mNCurrentOnlineSubscribers);
            } else {
                parseSubredditDataListener.onParseSubredditDataFail();
            }
        }
    }

    private static class ParseSubredditListingDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String response;
        private final boolean nsfw;
        private boolean parseFailed = false;
        private final ParseSubredditListingDataListener parseSubredditListingDataListener;
        private final ArrayList<SubredditData> subredditListingData = new ArrayList<>();
        private String after;

        ParseSubredditListingDataAsyncTask(String response, boolean nsfw, ParseSubredditListingDataListener parseSubredditListingDataListener) {
            this.parseSubredditListingDataListener = parseSubredditListingDataListener;
            this.response = response;
            this.nsfw = nsfw;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY)
                        .getJSONArray(JSONUtils.CHILDREN_KEY);
                for (int i = 0; i < children.length(); i++) {
                    JSONObject data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    SubredditData subredditData = parseSubredditData(data, nsfw);
                    if (subredditData != null) {
                        subredditListingData.add(subredditData);
                    }
                }
                after = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
                parseSubredditListingDataListener.onParseSubredditListingDataFail();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                parseSubredditListingDataListener.onParseSubredditListingDataSuccess(subredditListingData, after);
            } else {
                parseSubredditListingDataListener.onParseSubredditListingDataFail();
            }
        }
    }
}
