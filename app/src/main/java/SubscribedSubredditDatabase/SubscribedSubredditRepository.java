package SubscribedSubredditDatabase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class SubscribedSubredditRepository {
    private SubscribedSubredditDao mSubscribedSubredditDao;
    private LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;

    SubscribedSubredditRepository(Application application) {
        SubscribedSubredditRoomDatabase db = SubscribedSubredditRoomDatabase.getDatabase(application);
        mSubscribedSubredditDao = db.subscribedSubredditDao();
        mAllSubscribedSubreddits = mSubscribedSubredditDao.getAllSubscribedSubreddits();
    }

    LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
        return mAllSubscribedSubreddits;
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
