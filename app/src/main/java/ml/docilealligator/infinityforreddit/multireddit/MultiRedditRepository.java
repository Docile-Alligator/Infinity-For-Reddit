package ml.ino6962.postinfinityforreddit.multireddit;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class MultiRedditRepository {
    private MultiRedditDao mMultiRedditDao;
    private String mAccountName;

    MultiRedditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mMultiRedditDao = redditDataRoomDatabase.multiRedditDao();
        mAccountName = accountName;
    }

    LiveData<List<MultiReddit>> getAllMultiRedditsWithSearchQuery(String searchQuery) {
        return mMultiRedditDao.getAllMultiRedditsWithSearchQuery(mAccountName, searchQuery);
    }

    LiveData<List<MultiReddit>> getAllFavoriteMultiRedditsWithSearchQuery(String searchQuery) {
        return mMultiRedditDao.getAllFavoriteMultiRedditsWithSearchQuery(mAccountName, searchQuery);
    }
}
