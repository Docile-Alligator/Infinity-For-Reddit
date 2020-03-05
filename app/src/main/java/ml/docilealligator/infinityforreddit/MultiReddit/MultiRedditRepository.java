package ml.docilealligator.infinityforreddit.MultiReddit;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class MultiRedditRepository {
    private LiveData<List<MultiReddit>> mAllMultiReddits;
    private LiveData<List<MultiReddit>> mAllFavoriteMultiReddits;

    MultiRedditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        MultiRedditDao multiRedditDao = redditDataRoomDatabase.multiRedditDao();
        mAllMultiReddits = multiRedditDao.getAllMultiReddits(accountName);
        mAllFavoriteMultiReddits = multiRedditDao.getAllFavoriteMultiReddits(accountName);
    }

    LiveData<List<MultiReddit>> getAllMultiReddits() {
        return mAllMultiReddits;
    }

    LiveData<List<MultiReddit>> getAllFavoriteMultiReddits() {
        return mAllFavoriteMultiReddits;
    }
}
