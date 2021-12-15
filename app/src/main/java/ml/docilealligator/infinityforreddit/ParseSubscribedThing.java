package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

class ParseSubscribedThing {
    static void parseSubscribedSubreddits(String response, String accountName,
                                          ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                          ArrayList<SubscribedUserData> subscribedUserData,
                                          ArrayList<SubredditData> subredditData,
                                          ParseSubscribedSubredditsListener parseSubscribedSubredditsListener) {
        new ParseSubscribedSubredditsAsyncTask(response, accountName, subscribedSubredditData, subscribedUserData, subredditData,
                parseSubscribedSubredditsListener).execute();
    }

    interface ParseSubscribedSubredditsListener {
        void onParseSubscribedSubredditsSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                ArrayList<SubscribedUserData> subscribedUserData,
                                                ArrayList<SubredditData> subredditData,
                                                String lastItem);

        void onParseSubscribedSubredditsFail();
    }

    private static class ParseSubscribedSubredditsAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private String accountName;
        private boolean parseFailed;
        private String lastItem;
        private ArrayList<SubscribedSubredditData> subscribedSubredditData;
        private ArrayList<SubscribedUserData> subscribedUserData;
        private ArrayList<SubredditData> subredditData;
        private ArrayList<SubscribedSubredditData> newSubscribedSubredditData;
        private ArrayList<SubscribedUserData> newSubscribedUserData;
        private ArrayList<SubredditData> newSubredditData;
        private ParseSubscribedSubredditsListener parseSubscribedSubredditsListener;

        ParseSubscribedSubredditsAsyncTask(String response, String accountName, ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                           ArrayList<SubscribedUserData> subscribedUserData,
                                           ArrayList<SubredditData> subredditData,
                                           ParseSubscribedSubredditsListener parseSubscribedSubredditsListener) {
            try {
                jsonResponse = new JSONObject(response);
                this.accountName = accountName;
                parseFailed = false;
                this.subscribedSubredditData = subscribedSubredditData;
                this.subscribedUserData = subscribedUserData;
                this.subredditData = subredditData;
                newSubscribedSubredditData = new ArrayList<>();
                newSubscribedUserData = new ArrayList<>();
                newSubredditData = new ArrayList<>();
                this.parseSubscribedSubredditsListener = parseSubscribedSubredditsListener;
            } catch (JSONException e) {
                e.printStackTrace();
                parseSubscribedSubredditsListener.onParseSubscribedSubredditsFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                for (int i = 0; i < children.length(); i++) {
                    JSONObject data = children.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    String name = data.getString(JSONUtils.DISPLAY_NAME_KEY);
                    String bannerImageUrl = data.getString(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY);
                    if (bannerImageUrl.equals("") || bannerImageUrl.equals("null")) {
                        bannerImageUrl = data.getString(JSONUtils.BANNER_IMG_KEY);
                        if (bannerImageUrl.equals("null")) {
                            bannerImageUrl = "";
                        }
                    }
                    String iconUrl = data.getString(JSONUtils.COMMUNITY_ICON_KEY);
                    if (iconUrl.equals("") || iconUrl.equals("null")) {
                        iconUrl = data.getString(JSONUtils.ICON_IMG_KEY);
                        if (iconUrl.equals("null")) {
                            iconUrl = "";
                        }
                    }
                    String id = data.getString(JSONUtils.NAME_KEY);
                    boolean isFavorite = data.getBoolean(JSONUtils.USER_HAS_FAVORITED_KEY);

                    if (data.getString(JSONUtils.SUBREDDIT_TYPE_KEY)
                            .equals(JSONUtils.SUBREDDIT_TYPE_VALUE_USER)) {
                        //It's a user
                        newSubscribedUserData.add(new SubscribedUserData(name.substring(2), iconUrl, accountName, isFavorite));
                    } else {
                        String subredditFullName = data.getString(JSONUtils.DISPLAY_NAME_KEY);
                        String description = data.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY).trim();
                        String sidebarDescription = Utils.modifyMarkdown(data.getString(JSONUtils.DESCRIPTION_KEY).trim());
                        int nSubscribers = data.getInt(JSONUtils.SUBSCRIBERS_KEY);
                        long createdUTC = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
                        String suggestedCommentSort = data.getString(JSONUtils.SUGGESTED_COMMENT_SORT_KEY);
                        boolean isNSFW = data.getBoolean(JSONUtils.OVER18_KEY);
                        newSubscribedSubredditData.add(new SubscribedSubredditData(id, name, iconUrl, accountName, isFavorite));
                        newSubredditData.add(new SubredditData(id, subredditFullName, iconUrl,
                                bannerImageUrl, description, sidebarDescription, nSubscribers, createdUTC,
                                suggestedCommentSort, isNSFW));
                    }
                }
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                subscribedSubredditData.addAll(newSubscribedSubredditData);
                subscribedUserData.addAll(newSubscribedUserData);
                subredditData.addAll(newSubredditData);
                parseSubscribedSubredditsListener.onParseSubscribedSubredditsSuccess(subscribedSubredditData,
                        subscribedUserData, subredditData, lastItem);
            } else {
                parseSubscribedSubredditsListener.onParseSubscribedSubredditsFail();
            }
        }
    }
}
