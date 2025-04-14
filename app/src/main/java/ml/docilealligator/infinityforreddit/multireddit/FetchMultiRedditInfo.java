package ml.docilealligator.infinityforreddit.multireddit;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMultiRedditInfo {

    public interface FetchMultiRedditInfoListener {
        void success(MultiReddit multiReddit);
        void failed();
    }

    public static void fetchMultiRedditInfo(Executor executor, Handler handler, Retrofit retrofit,
                                            String accessToken, String multipath,
                                            FetchMultiRedditInfoListener fetchMultiRedditInfoListener) {
        retrofit.create(RedditAPI.class).getMultiRedditInfo(APIUtils.getOAuthHeader(accessToken), multipath).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MultiReddit multiReddit = parseMultiRedditInfo(response.body());
                        if (multiReddit != null) {
                            handler.post(() -> fetchMultiRedditInfoListener.success(multiReddit));
                        } else {
                            handler.post(fetchMultiRedditInfoListener::failed);
                        }
                    });
                } else {
                    fetchMultiRedditInfoListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMultiRedditInfoListener.failed();
            }
        });
    }

    public static void anonymousFetchMultiRedditInfo(Executor executor, Handler handler,
                                                     RedditDataRoomDatabase redditDataRoomDatabase,
                                                     String multipath,
                                                     FetchMultiRedditInfoListener fetchMultiRedditInfoListener) {
        executor.execute(() -> {
            MultiReddit multiReddit = redditDataRoomDatabase.multiRedditDao().getMultiReddit(multipath, Account.ANONYMOUS_ACCOUNT);
            ArrayList<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits =
                    (ArrayList<AnonymousMultiredditSubreddit>) redditDataRoomDatabase.anonymousMultiredditSubredditDao().getAllAnonymousMultiRedditSubreddits(multipath);
            ArrayList<String> subredditNames = new ArrayList<>();
            for (AnonymousMultiredditSubreddit a : anonymousMultiredditSubreddits) {
                subredditNames.add(a.getSubredditName());
            }
            multiReddit.setSubreddits(subredditNames);
            handler.post(() -> fetchMultiRedditInfoListener.success(multiReddit));
        });
    }

    @WorkerThread
    @Nullable
    private static MultiReddit parseMultiRedditInfo(String response) {
        try {
            JSONObject object = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
            String path = object.getString(JSONUtils.PATH_KEY);
            String displayName = object.getString(JSONUtils.DISPLAY_NAME_KEY);
            String name = object.getString(JSONUtils.NAME_KEY);
            String description = object.getString(JSONUtils.DESCRIPTION_MD_KEY);
            String copiedFrom = object.getString(JSONUtils.COPIED_FROM_KEY);
            String iconUrl = object.getString(JSONUtils.ICON_URL_KEY);
            String visibility = object.getString(JSONUtils.VISIBILITY_KEY);
            String owner = object.getString(JSONUtils.OWNER_KEY);
            int nSubscribers = object.getInt(JSONUtils.NUM_SUBSCRIBERS_KEY);
            long createdUTC = object.getLong(JSONUtils.CREATED_UTC_KEY);
            boolean over18 = object.getBoolean(JSONUtils.OVER_18_KEY);
            boolean isSubscriber = object.getBoolean(JSONUtils.IS_SUBSCRIBER_KEY);
            boolean isFavorite = object.getBoolean(JSONUtils.IS_FAVORITED_KEY);
            ArrayList<String> subreddits = new ArrayList<>();
            JSONArray subredditsArray = object.getJSONArray(JSONUtils.SUBREDDITS_KEY);
            for (int i = 0; i < subredditsArray.length(); i++) {
                subreddits.add(subredditsArray.getJSONObject(i).getString(JSONUtils.NAME_KEY));
            }

            return new MultiReddit(path, displayName, name, description, copiedFrom, iconUrl,
                    visibility, owner, nSubscribers, createdUTC, over18, isSubscriber, isFavorite,
                    subreddits);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
