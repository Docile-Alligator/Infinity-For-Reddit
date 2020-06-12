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
    private MultiReddit multiReddit;
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

    public InsertMultiRedditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                      MultiReddit multiReddit,
                                      InsertMultiRedditAsyncTaskListener insertMultiRedditAsyncTaskListener) {
        multiRedditDao = redditDataRoomDatabase.multiRedditDao();
        this.multiReddit = multiReddit;
        this.insertMultiRedditAsyncTaskListener = insertMultiRedditAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (multiReddit != null) {
            multiRedditDao.insert(multiReddit);
            return null;
        }

        List<MultiReddit> existingMultiReddits = multiRedditDao.getAllMultiRedditsList(accountName);
        Collections.sort(multiReddits, (multiReddit, t1) -> multiReddit.getName().compareToIgnoreCase(t1.getName()));
        List<String> deletedMultiredditNames = new ArrayList<>();
        compareTwoMultiRedditList(multiReddits, existingMultiReddits, deletedMultiredditNames);

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
                                              List<String> deletedMultiReddits) {
        int newIndex = 0;
        for (int oldIndex = 0; oldIndex < oldMultiReddits.size(); oldIndex++) {
            if (newIndex >= newMultiReddits.size()) {
                for (; oldIndex < oldMultiReddits.size(); oldIndex++) {
                    deletedMultiReddits.add(oldMultiReddits.get(oldIndex).getName());
                }
                return;
            }

            MultiReddit old = oldMultiReddits.get(oldIndex);
            for (; newIndex < newMultiReddits.size(); newIndex++) {
                if (newMultiReddits.get(newIndex).getName().compareToIgnoreCase(old.getName()) == 0) {
                    newIndex++;
                    break;
                }
                if (newMultiReddits.get(newIndex).getName().compareToIgnoreCase(old.getName()) > 0) {
                    deletedMultiReddits.add(old.getName());
                    break;
                }
            }
        }
    }

    public interface InsertMultiRedditAsyncTaskListener {
        void success();
    }
}
