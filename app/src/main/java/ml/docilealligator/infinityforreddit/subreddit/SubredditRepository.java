package ml.docilealligator.infinityforreddit.subreddit;

import androidx.lifecycle.LiveData;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubredditRepository {
    private final SubredditDao mSubredditDao;
    private final LiveData<SubredditData> mSubredditLiveData;

    SubredditRepository(RedditDataRoomDatabase redditDataRoomDatabase,
                        String subredditName) {
        mSubredditDao = redditDataRoomDatabase.subredditDao();
        mSubredditLiveData = mSubredditDao.getSubredditLiveDataByName(subredditName);
    }

    LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }
}