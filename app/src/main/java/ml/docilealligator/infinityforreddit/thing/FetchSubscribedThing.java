package ml.docilealligator.infinityforreddit.thing;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchSubscribedThing {
    public static void fetchSubscribedThing(Executor executor, Handler handler, final Retrofit oauthRetrofit,
                                            @Nullable String accessToken, @NonNull String accountName,
                                            final String lastItem,
                                            final ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                            final ArrayList<SubscribedUserData> subscribedUserData,
                                            final ArrayList<SubredditData> subredditData,
                                            final FetchSubscribedThingListener fetchSubscribedThingListener) {
        executor.execute(() -> {
            try {
                Response<String> response = oauthRetrofit.create(RedditAPI.class).getSubscribedThing(lastItem, APIUtils.getOAuthHeader(accessToken)).execute();
                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);

                    List<SubscribedSubredditData> newSubscribedSubredditData = new ArrayList<>();
                    List<SubscribedUserData> newSubscribedUserData = new ArrayList<>();
                    List<SubredditData> newSubredditData = new ArrayList<>();

                    for (int i = 0; i < children.length(); i++) {
                        try {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    subscribedSubredditData.addAll(newSubscribedSubredditData);
                    subscribedUserData.addAll(newSubscribedUserData);
                    subredditData.addAll(newSubredditData);

                    String newLastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);

                    if (newLastItem.equals("null")) {
                        handler.post(() -> fetchSubscribedThingListener.onFetchSubscribedThingSuccess(
                                subscribedSubredditData, subscribedUserData, subredditData));
                    } else {
                        handler.post(() -> fetchSubscribedThing(executor, handler, oauthRetrofit, accessToken, accountName, newLastItem,
                                subscribedSubredditData, subscribedUserData, subredditData,
                                fetchSubscribedThingListener));
                    }
                } else {
                    handler.post(fetchSubscribedThingListener::onFetchSubscribedThingFail);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                handler.post(fetchSubscribedThingListener::onFetchSubscribedThingFail);
            }
        });
    }

    public interface FetchSubscribedThingListener {
        void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                           ArrayList<SubscribedUserData> subscribedUserData,
                                           ArrayList<SubredditData> subredditData);

        void onFetchSubscribedThingFail();
    }
}
