package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditDao;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import retrofit2.Retrofit;

public class LoadSubredditIcon {

    public static void loadSubredditIcon(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                         String subredditName, String accessToken, Retrofit oauthRetrofit, Retrofit retrofit,
                                         LoadSubredditIconAsyncTaskListener loadSubredditIconAsyncTaskListener) {
        executor.execute(() -> {
            SubredditDao subredditDao = redditDataRoomDatabase.subredditDao();
            SubredditData subredditData = subredditDao.getSubredditData(subredditName);
            if (subredditData != null) {
                String iconImageUrl = subredditDao.getSubredditData(subredditName).getIconUrl();
                handler.post(() -> loadSubredditIconAsyncTaskListener.loadIconSuccess(iconImageUrl));
            } else {
                handler.post(() -> FetchSubredditData.fetchSubredditData(oauthRetrofit, retrofit, subredditName, accessToken, new FetchSubredditData.FetchSubredditDataListener() {
                    @Override
                    public void onFetchSubredditDataSuccess(SubredditData subredditData1, int nCurrentOnlineSubscribers) {
                        ArrayList<SubredditData> singleSubredditDataList = new ArrayList<>();
                        singleSubredditDataList.add(subredditData1);
                        InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase, null,
                                null, null, singleSubredditDataList,
                                () -> loadSubredditIconAsyncTaskListener.loadIconSuccess(subredditData1.getIconUrl()));
                    }

                    @Override
                    public void onFetchSubredditDataFail(boolean isQuarantined) {
                        loadSubredditIconAsyncTaskListener.loadIconSuccess(null);
                    }
                }));
            }
        });
    }

    public interface LoadSubredditIconAsyncTaskListener {
        void loadIconSuccess(String iconImageUrl);
    }
}
