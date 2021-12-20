package ml.docilealligator.infinityforreddit.subscribeduser;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedUserRepository {
    private SubscribedUserDao mSubscribedUserDao;
    private String mAccountName;

    SubscribedUserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mAccountName = accountName;
    }

    LiveData<List<SubscribedUserData>> getAllSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
    }

    LiveData<List<SubscribedUserData>> getAllFavoriteSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllFavoriteSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
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
