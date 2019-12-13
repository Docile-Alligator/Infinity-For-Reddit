package ml.docilealligator.infinityforreddit.MultiReddit;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class MultiRedditRepository {
    private MultiRedditDao mMultiRedditDao;
    private LiveData<List<MultiReddit>> mAllMultiReddits;
    private LiveData<List<MultiReddit>> mAllFavoriteMultiReddits;

    MultiRedditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mMultiRedditDao = redditDataRoomDatabase.multiRedditDao();
        mAllMultiReddits = mMultiRedditDao.getAllMultiReddits(accountName);
        mAllFavoriteMultiReddits = mMultiRedditDao.getAllFavoriteMultiReddits(accountName);
    }

    LiveData<List<MultiReddit>> getAllMultiReddits() {
        return mAllMultiReddits;
    }

    LiveData<List<MultiReddit>> getAllFavoriteMultiReddits() {
        return mAllFavoriteMultiReddits;
    }

    public void insert(MultiReddit MultiReddit) {
        new MultiRedditRepository.insertAsyncTask(mMultiRedditDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<MultiReddit, Void, Void> {

        private MultiRedditDao mAsyncTaskDao;

        insertAsyncTask(MultiRedditDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MultiReddit... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
