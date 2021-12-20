package ml.docilealligator.infinityforreddit.subscribedsubreddit;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditRepository {
    private SubscribedSubredditDao mSubscribedSubredditDao;
    private String mAccountName;

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

    public void insert(SubscribedSubredditData subscribedSubredditData) {
        new insertAsyncTask(mSubscribedSubredditDao).execute(subscribedSubredditData);
    }

    private static class insertAsyncTask extends AsyncTask<SubscribedSubredditData, Void, Void> {

        private SubscribedSubredditDao mAsyncTaskDao;

        insertAsyncTask(SubscribedSubredditDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubscribedSubredditData... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
