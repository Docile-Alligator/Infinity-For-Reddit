package ml.docilealligator.infinityforreddit.user;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class UserRepository {
    private UserDao mUserDao;
    private LiveData<UserData> mUserLiveData;

    UserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String userName) {
        mUserDao = redditDataRoomDatabase.userDao();
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
