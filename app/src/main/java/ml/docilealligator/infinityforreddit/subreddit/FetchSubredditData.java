package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchSubredditData {
    public static void fetchSubredditData(Executor executor, Handler handler, Retrofit oauthRetrofit, Retrofit retrofit,
                                          String subredditName, String accessToken,
                                          final FetchSubredditDataListener fetchSubredditDataListener) {
        executor.execute(() -> {
            RedditAPI api = retrofit.create(RedditAPI.class);

            Call<String> subredditData;
            if (oauthRetrofit == null) {
                subredditData = api.getSubredditData(subredditName);
            } else {
                RedditAPI oauthApi = oauthRetrofit.create(RedditAPI.class);
                subredditData = oauthApi.getSubredditDataOauth(subredditName, APIUtils.getOAuthHeader(accessToken));
            }
            try {
                Response<String> response = subredditData.execute();
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditDataSync(handler, response.body(), fetchSubredditDataListener);
                } else {
                    handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataFail(response.code() == 403));
                }
            } catch (IOException e) {
                handler.post(() -> fetchSubredditDataListener.onFetchSubredditDataFail(false));
            }
        });
    }

    static void fetchSubredditListingData(Executor executor, Handler handler, Retrofit retrofit,
                                          Retrofit oauthRetrofit, String query,
                                          String after, SortType.Type sortType, @Nullable String accessToken,
                                          @NonNull String accountName, boolean nsfw,
                                          final FetchSubredditListingDataListener fetchSubredditListingDataListener) {
        executor.execute(() -> {
            RedditAPI api = (Account.ANONYMOUS_ACCOUNT.equals(accountName) ? retrofit : oauthRetrofit).create(RedditAPI.class);
            Map<String, String> headers = Account.ANONYMOUS_ACCOUNT.equals(accountName) ? new HashMap<>() : APIUtils.getOAuthHeader(accessToken);
            Call<String> subredditDataCall = api.searchSubreddits(query, after, sortType, nsfw ? 1 : 0, headers);
            try {
                Response<String> response = subredditDataCall.execute();
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditListingDataSync(handler, response.body(), nsfw,
                            fetchSubredditListingDataListener);
                } else {
                    handler.post(fetchSubredditListingDataListener::onFetchSubredditListingDataFail);
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(fetchSubredditListingDataListener::onFetchSubredditListingDataFail);
            }
        });
    }

    public interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);

        void onFetchSubredditDataFail(boolean isQuarantined);
    }

    public interface FetchSubredditListingDataListener {
        void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);

        void onFetchSubredditListingDataFail();
    }
}
