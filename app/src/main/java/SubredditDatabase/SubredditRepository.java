package SubredditDatabase;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

public class SubredditRepository {
    private SubredditDao mSubredditDao;
    private LiveData<SubredditData> mSubredditLiveData;

    SubredditRepository(Application application, String subredditName) {
        SubredditRoomDatabase db = SubredditRoomDatabase.getDatabase(application);
        mSubredditDao = db.subredditDao();

        mSubredditLiveData = mSubredditDao.getSubredditLiveDataByName(subredditName);
    }

    LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }

    public void insert(SubredditData subredditData) {
        new InsertAsyncTask(mSubredditDao).execute(subredditData);
    }

    private static class InsertAsyncTask extends AsyncTask<SubredditData, Void, Void> {

        private SubredditDao mAsyncTaskDao;

        InsertAsyncTask(SubredditDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubredditData... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
