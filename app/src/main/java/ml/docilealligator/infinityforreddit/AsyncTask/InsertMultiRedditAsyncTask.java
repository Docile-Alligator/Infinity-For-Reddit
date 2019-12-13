package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditDao;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertMultiRedditAsyncTask extends AsyncTask<Void, Void, Void> {
    private MultiRedditDao multiRedditDao;
    private ArrayList<MultiReddit> multiReddits;
    private InsertMultiRedditAsyncTaskListener insertMultiRedditAsyncTaskListener;

    public InsertMultiRedditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                      ArrayList<MultiReddit> multiReddits,
                                      InsertMultiRedditAsyncTaskListener insertMultiRedditAsyncTaskListener) {
        multiRedditDao = redditDataRoomDatabase.multiRedditDao();
        this.multiReddits = multiReddits;
        this.insertMultiRedditAsyncTaskListener = insertMultiRedditAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (MultiReddit multiReddit : multiReddits) {
            multiRedditDao.insert(multiReddit);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        insertMultiRedditAsyncTaskListener.success();
    }

    public interface InsertMultiRedditAsyncTaskListener {
        void success();
    }
}
