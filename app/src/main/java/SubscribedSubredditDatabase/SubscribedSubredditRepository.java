package SubscribedSubredditDatabase;

import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditRepository {

  private final SubscribedSubredditDao mSubscribedSubredditDao;
  private final LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;

  SubscribedSubredditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
    mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
    mAllSubscribedSubreddits = mSubscribedSubredditDao.getAllSubscribedSubreddits(accountName);
  }

  LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
    return mAllSubscribedSubreddits;
  }

  public void insert(SubscribedSubredditData subscribedSubredditData) {
    new insertAsyncTask(mSubscribedSubredditDao).execute(subscribedSubredditData);
  }

  private static class insertAsyncTask extends AsyncTask<SubscribedSubredditData, Void, Void> {

    private final SubscribedSubredditDao mAsyncTaskDao;

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
