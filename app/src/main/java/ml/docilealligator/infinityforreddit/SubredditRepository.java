package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

public class SubredditRepository {
    private SubredditDao mSubredditDao;
    private LiveData<SubredditData> mSubredditLiveData;

    SubredditRepository(Application application, String value, boolean isId) {
        SubredditRoomDatabase db = SubredditRoomDatabase.getDatabase(application);
        mSubredditDao = db.subredditDao();
        if(isId) {
            mSubredditLiveData = mSubredditDao.getSubredditLiveDataById(value);
        } else {
            mSubredditLiveData = mSubredditDao.getSubredditLiveDataByNamePrefixed(value);
        }
    }

    LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }

    public void insert(SubredditData subredditData) {
        new SubredditRepository.insertAsyncTask(mSubredditDao).execute(subredditData);
    }

    private static class insertAsyncTask extends AsyncTask<SubredditData, Void, Void> {

        private SubredditDao mAsyncTaskDao;

        insertAsyncTask(SubredditDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubredditData... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
