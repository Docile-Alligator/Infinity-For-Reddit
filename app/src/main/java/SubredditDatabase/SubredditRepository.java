package SubredditDatabase;

import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubredditRepository {

  private final SubredditDao mSubredditDao;
  private final LiveData<SubredditData> mSubredditLiveData;

  SubredditRepository(RedditDataRoomDatabase redditDataRoomDatabase, String subredditName) {
    mSubredditDao = redditDataRoomDatabase.subredditDao();
    mSubredditLiveData = mSubredditDao.getSubredditLiveDataByName(subredditName);
  }

  LiveData<SubredditData> getSubredditLiveData() {
    return mSubredditLiveData;
  }

  public void insert(SubredditData subredditData) {
    new InsertAsyncTask(mSubredditDao).execute(subredditData);
  }

  private static class InsertAsyncTask extends AsyncTask<SubredditData, Void, Void> {

    private final SubredditDao mAsyncTaskDao;

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
