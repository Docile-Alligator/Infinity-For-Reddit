package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditDao;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertMultiRedditAsyncTask extends AsyncTask<Void, Void, Void> {
    private MultiRedditDao multiRedditDao;
    private ArrayList<MultiReddit> multiReddits;
    private String accountName;
    private InsertMultiRedditAsyncTaskListener insertMultiRedditAsyncTaskListener;

    public InsertMultiRedditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                      ArrayList<MultiReddit> multiReddits, String accountName,
                                      InsertMultiRedditAsyncTaskListener insertMultiRedditAsyncTaskListener) {
        multiRedditDao = redditDataRoomDatabase.multiRedditDao();
        this.multiReddits = multiReddits;
        this.accountName = accountName;
        this.insertMultiRedditAsyncTaskListener = insertMultiRedditAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        List<MultiReddit> existingMultiReddits = multiRedditDao.getAllMultiRedditsList(accountName);
        Collections.sort(multiReddits, (multiReddit, t1) -> multiReddit.getName().compareToIgnoreCase(t1.getName()));
        List<String> deletedMultiredditNames = new ArrayList<>();
        compareTwoMultiRedditList(multiReddits, existingMultiReddits, deletedMultiredditNames, 0, 0);

        for (String deleted : deletedMultiredditNames) {
            multiRedditDao.deleteMultiReddit(deleted, accountName);
        }

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

    private void compareTwoMultiRedditList(List<MultiReddit> newMultiReddits,
                                              List<MultiReddit> oldMultiReddits,
                                              List<String> deletedMultiReddits, int i1, int i2) {
        if (newMultiReddits.size() <= i1 && oldMultiReddits.size() <= i2) {
            return;
        }

        if (newMultiReddits.size() <= i1) {
            for (int i = 0; i < oldMultiReddits.size(); i++) {
                deletedMultiReddits.add(oldMultiReddits.get(i).getName());
            }
            return;
        }

        if (oldMultiReddits.size() > i2) {
            if (newMultiReddits.get(i1).getName().compareToIgnoreCase(oldMultiReddits.get(i2).getName()) == 0) {
                compareTwoMultiRedditList(newMultiReddits, oldMultiReddits, deletedMultiReddits, i1 + 1, i2 + 1);
            } else if (newMultiReddits.get(i1).getName().compareToIgnoreCase(oldMultiReddits.get(i2).getName()) < 0) {
                compareTwoMultiRedditList(newMultiReddits, oldMultiReddits, deletedMultiReddits, i1 + 1, i2);
            } else {
                deletedMultiReddits.add(oldMultiReddits.get(i2).getName());
                compareTwoMultiRedditList(newMultiReddits, oldMultiReddits, deletedMultiReddits, i1, i2 + 1);
            }
        }
    }

    public interface InsertMultiRedditAsyncTaskListener {
        void success();
    }
}
