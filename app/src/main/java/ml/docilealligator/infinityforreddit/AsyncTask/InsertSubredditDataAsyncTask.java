package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;

public class InsertSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {
    private SubredditDao mSubredditDao;
    private SubredditData subredditData;
    private InsertSubredditDataAsyncTaskListener insertSubredditDataAsyncTaskListener;

    public InsertSubredditDataAsyncTask(RedditDataRoomDatabase db, SubredditData subredditData,
                                 InsertSubredditDataAsyncTaskListener insertSubredditDataAsyncTaskListener) {
        mSubredditDao = db.subredditDao();
        this.subredditData = subredditData;
        this.insertSubredditDataAsyncTaskListener = insertSubredditDataAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        mSubredditDao.insert(subredditData);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        insertSubredditDataAsyncTaskListener.insertSuccess();
    }

    public interface InsertSubredditDataAsyncTaskListener {
        void insertSuccess();
    }
}
