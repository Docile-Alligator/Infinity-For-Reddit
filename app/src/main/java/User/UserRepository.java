package User;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

public class UserRepository {
    private UserDao mUserDao;
    private LiveData<UserData> mUserLiveData;

    UserRepository(Application application, String userName) {
        mUserDao = UserRoomDatabase.getDatabase(application).userDao();

        mUserLiveData = mUserDao.getUserLiveData(userName);
    }

    LiveData<UserData> getUserLiveData() {
        return mUserLiveData;
    }

    public void insert(UserData userData) {
        new InsertAsyncTask(mUserDao).execute(userData);
    }

    private static class InsertAsyncTask extends AsyncTask<UserData, Void, Void> {

        private UserDao mAsyncTaskDao;

        InsertAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserData... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
