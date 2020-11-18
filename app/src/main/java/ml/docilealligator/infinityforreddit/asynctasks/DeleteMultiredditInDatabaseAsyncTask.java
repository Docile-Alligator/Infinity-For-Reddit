package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteMultiredditInDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {
    public interface DeleteMultiredditInDatabaseAsyncTaskListener {
        void success();
    }

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String accountName;
    private String multipath;
    private DeleteMultiredditInDatabaseAsyncTaskListener deleteMultiredditInDatabaseAsyncTaskListener;

    public DeleteMultiredditInDatabaseAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                                String accountName, String multipath,
                                                DeleteMultiredditInDatabaseAsyncTaskListener deleteMultiredditInDatabaseAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.accountName = accountName;
        this.multipath = multipath;
        this.deleteMultiredditInDatabaseAsyncTaskListener = deleteMultiredditInDatabaseAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.multiRedditDao().deleteMultiReddit(multipath, accountName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteMultiredditInDatabaseAsyncTaskListener.success();
    }
}
