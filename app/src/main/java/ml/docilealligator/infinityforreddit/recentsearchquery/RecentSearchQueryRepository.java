package ml.ino6962.postinfinityforreddit.recentsearchquery;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class RecentSearchQueryRepository {
    private LiveData<List<RecentSearchQuery>> mAllRecentSearchQueries;

    RecentSearchQueryRepository(RedditDataRoomDatabase redditDataRoomDatabase, String username) {
        mAllRecentSearchQueries = redditDataRoomDatabase.recentSearchQueryDao().getAllRecentSearchQueriesLiveData(username);
    }

    LiveData<List<RecentSearchQuery>> getAllRecentSearchQueries() {
        return mAllRecentSearchQueries;
    }
}
