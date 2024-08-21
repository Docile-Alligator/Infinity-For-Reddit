package ml.docilealligator.infinityforreddit.recentsearchquery;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertRecentSearchQuery {
    public interface InsertRecentSearchQueryListener {
        void success();
    }

    public static void insertRecentSearchQueryListener(Executor executor, Handler handler,
                                                       RedditDataRoomDatabase redditDataRoomDatabase,
                                                       String username,
                                                       String recentSearchQuery,
                                                       String searchInSubredditOrUserName,
                                                       int searchInThingType,
                                                       InsertRecentSearchQueryListener insertRecentSearchQueryListener) {
        executor.execute(() -> {
            RecentSearchQueryDao recentSearchQueryDao = redditDataRoomDatabase.recentSearchQueryDao();
            List<RecentSearchQuery> recentSearchQueries = recentSearchQueryDao.getAllRecentSearchQueries(username);
            if (recentSearchQueries.size() >= 5) {
                for (int i = 4; i < recentSearchQueries.size(); i++) {
                    recentSearchQueryDao.deleteRecentSearchQueries(recentSearchQueries.get(i));
                }
            }

            recentSearchQueryDao.insert(new RecentSearchQuery(username, recentSearchQuery, searchInSubredditOrUserName, searchInThingType));

            handler.post(insertRecentSearchQueryListener::success);
        });
    }
}
