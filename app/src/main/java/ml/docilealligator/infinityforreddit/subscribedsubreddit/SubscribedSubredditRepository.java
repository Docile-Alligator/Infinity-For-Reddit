package ml.docilealligator.infinityforreddit.subscribedsubreddit;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditRepository {
    private final SubscribedSubredditDao mSubscribedSubredditDao;
    private final String mAccountName;

    SubscribedSubredditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mAccountName = accountName;
        mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
    }

    LiveData<List<SubscribedSubredditData>> getAllSubscribedSubredditsWithSearchQuery(String searchQuery) {
        return mSubscribedSubredditDao.getAllSubscribedSubredditsWithSearchQuery(mAccountName, searchQuery);
    }

    public LiveData<List<SubscribedSubredditData>> getAllFavoriteSubscribedSubredditsWithSearchQuery(String searchQuery) {
        return mSubscribedSubredditDao.getAllFavoriteSubscribedSubredditsWithSearchQuery(mAccountName, searchQuery);
    }
}
