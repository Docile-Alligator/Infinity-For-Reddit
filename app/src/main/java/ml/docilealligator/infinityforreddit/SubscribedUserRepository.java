package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class SubscribedUserRepository {
    private SubscribedUserDao mSubscribedUserDao;
    private LiveData<List<SubscribedUserData>> mAllSubscribedUsers;

    SubscribedUserRepository(Application application) {
        SubscribedUserRoomDatabase db = SubscribedUserRoomDatabase.getDatabase(application);
        mSubscribedUserDao = db.subscribedUserDao();
        mAllSubscribedUsers = mSubscribedUserDao.getAllSubscribedUsers();
    }

    LiveData<List<SubscribedUserData>> getAllSubscribedSubreddits() {
        return mAllSubscribedUsers;
    }

    public void insert(SubscribedUserData subscribedUserData) {
        new SubscribedUserRepository.insertAsyncTask(mSubscribedUserDao).execute(subscribedUserData);
    }

    private static class insertAsyncTask extends AsyncTask<SubscribedUserData, Void, Void> {

        private SubscribedUserDao mAsyncTaskDao;

        insertAsyncTask(SubscribedUserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SubscribedUserData... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
