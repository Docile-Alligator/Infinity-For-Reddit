package ml.docilealligator.infinityforreddit.multireddit;

import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;

public class ParseMultiReddit {
    interface ParseMultiRedditsListListener {
        void success(ArrayList<MultiReddit> multiReddits);
        void failed();
    }

    interface ParseMultiRedditListener {
        void success();
        void failed();
    }

    public static void parseMultiRedditsList(Executor executor, Handler handler, String response,
                                             ParseMultiRedditsListListener parseMultiRedditsListListener) {
        executor.execute(() -> {
            try {
                JSONArray arrayResponse = new JSONArray(response);
                ArrayList<MultiReddit> multiReddits = new ArrayList<>();
                for (int i = 0; i < arrayResponse.length(); i++) {
                    try {
                        multiReddits.add(parseMultiReddit(arrayResponse.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(() -> parseMultiRedditsListListener.success(multiReddits));
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseMultiRedditsListListener::failed);
            }
        });
    }

    public static void parseAndSaveMultiReddit(Executor executor, Handler handler, String response, RedditDataRoomDatabase redditDataRoomDatabase,
                                               ParseMultiRedditListener parseMultiRedditListener) {
        executor.execute(() -> {
            try {
                MultiReddit multiReddit = parseMultiReddit(new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY));
                redditDataRoomDatabase.multiRedditDao().insert(multiReddit);

                handler.post(parseMultiRedditListener::success);
            } catch (JSONException e) {
                e.printStackTrace();
                handler.post(parseMultiRedditListener::failed);
            }
        });
    }

    private static MultiReddit parseMultiReddit(JSONObject singleMultiRedditJSON) throws JSONException {
        String displayName = singleMultiRedditJSON.getString(JSONUtils.DISPLAY_NAME_KEY);
        String name = singleMultiRedditJSON.getString(JSONUtils.NAME_KEY);
        String description = singleMultiRedditJSON.getString(JSONUtils.DESCRIPTION_MD_KEY);
        int nSubscribers = singleMultiRedditJSON.getInt(JSONUtils.NUM_SUBSCRIBERS_KEY);
        String copiedFrom = singleMultiRedditJSON.getString(JSONUtils.COPIED_FROM_KEY);
        String iconUrl = singleMultiRedditJSON.getString(JSONUtils.ICON_URL_KEY);
        long createdUTC = singleMultiRedditJSON.getLong(JSONUtils.CREATED_UTC_KEY);
        String visibility = singleMultiRedditJSON.getString(JSONUtils.VISIBILITY_KEY);
        boolean over18 = singleMultiRedditJSON.getBoolean(JSONUtils.OVER_18_KEY);
        String path = singleMultiRedditJSON.getString(JSONUtils.PATH_KEY);
        String owner = singleMultiRedditJSON.getString(JSONUtils.OWNER_KEY);
        boolean isSubscriber = singleMultiRedditJSON.getBoolean(JSONUtils.IS_SUBSCRIBER_KEY);
        boolean isFavorited = singleMultiRedditJSON.getBoolean(JSONUtils.IS_FAVORITED_KEY);

        JSONArray subredditsArray = singleMultiRedditJSON.getJSONArray(JSONUtils.SUBREDDITS_KEY);
        ArrayList<String> subreddits = new ArrayList<>();
        for (int j = 0; j < subredditsArray.length(); j++) {
            subreddits.add(subredditsArray.getJSONObject(j).getString(JSONUtils.NAME_KEY));
        }

        return new MultiReddit(path, displayName, name, description, copiedFrom,
                iconUrl, visibility, owner, nSubscribers, createdUTC, over18, isSubscriber,
                isFavorited, subreddits);
    }
}
