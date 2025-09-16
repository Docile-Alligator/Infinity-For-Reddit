package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Handler;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ParseSubredditData {
    public static void parseSubredditDataSync(Handler handler, @Nullable String response,
                                              FetchSubredditData.FetchSubredditDataListener fetchSubredditDataListener) {
        if (response == null) {
            handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataFail(false));
            return;
        }

        try {
            JSONObject data = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);

            //int nCurrentOnlineSubscribers = data.getInt(JSONUtils.ACTIVE_USER_COUNT_KEY);
            SubredditData subredditData = parseSubredditDataSync(data, true);

            if (subredditData == null) {
                handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataFail(false));
            } else {
                handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataSuccess(subredditData, 0));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataFail(false));
        }
    }

    public static void parseSubredditListingDataSync(Handler handler, @Nullable String response, boolean nsfw,
                                                     FetchSubredditData.FetchSubredditListingDataListener fetchSubredditListingDataListener) {
        if (response == null) {
            handler.post(fetchSubredditListingDataListener::onFetchSubredditListingDataFail);
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray children = jsonObject.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

            ArrayList<SubredditData> subredditListingData = new ArrayList<>();
            for (int i = 0; i < children.length(); i++) {
                JSONObject data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                SubredditData subredditData = parseSubredditDataSync(data, nsfw);
                if (subredditData != null) {
                    subredditListingData.add(subredditData);
                }
            }

            String after = jsonObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);

            handler.post(() -> fetchSubredditListingDataListener.onFetchSubredditListingDataSuccess(subredditListingData, after));
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(fetchSubredditListingDataListener::onFetchSubredditListingDataFail);
        }
    }

    public static void parseSubredditListingData(Executor executor, Handler handler, @Nullable String response, boolean nsfw,
                                                 ParseSubredditListingDataListener parseSubredditListingDataListener) {
        if (response == null) {
            parseSubredditListingDataListener.onParseSubredditListingDataFail();
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray children = jsonObject.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                ArrayList<SubredditData> subredditListingData = new ArrayList<>();
                for (int i = 0; i < children.length(); i++) {
                    JSONObject data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    SubredditData subredditData = parseSubredditDataSync(data, nsfw);
                    if (subredditData != null) {
                        subredditListingData.add(subredditData);
                    }
                }

                String after = jsonObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);

                handler.post(() -> parseSubredditListingDataListener.onParseSubredditListingDataSuccess(subredditListingData, after));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseSubredditListingDataListener::onParseSubredditListingDataFail);
            }
        });
    }

    @Nullable
    private static SubredditData parseSubredditDataSync(JSONObject subredditDataJsonObject, boolean nsfw) throws JSONException {
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
        if (bannerImageUrl.equals("") && !subredditDataJsonObject.isNull(JSONUtils.BANNER_IMG_KEY)) {
            bannerImageUrl = subredditDataJsonObject.getString(JSONUtils.BANNER_IMG_KEY);
        }

        String iconUrl;
        if (subredditDataJsonObject.isNull(JSONUtils.COMMUNITY_ICON_KEY)) {
            iconUrl = "";
        } else {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.COMMUNITY_ICON_KEY);
        }
        if (iconUrl.equals("") && !subredditDataJsonObject.isNull(JSONUtils.ICON_IMG_KEY)) {
            iconUrl = subredditDataJsonObject.getString(JSONUtils.ICON_IMG_KEY);
        }

        int nSubscribers = 0;
        if (!subredditDataJsonObject.isNull(JSONUtils.SUBSCRIBERS_KEY)) {
            nSubscribers = subredditDataJsonObject.getInt(JSONUtils.SUBSCRIBERS_KEY);
        }

        return new SubredditData(id, subredditFullName, iconUrl, bannerImageUrl, description,
                sidebarDescription, nSubscribers, createdUTC, suggestedCommentSort, isNSFW);
    }

    public interface ParseSubredditListingDataListener {
        void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);

        void onParseSubredditListingDataFail();
    }
}
