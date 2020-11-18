package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllUsersAsyncTask extends AsyncTask<Void, Void, Void> {

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private DeleteAllUsersAsyncTaskListener deleteAllUsersAsyncTaskListener;

    public interface DeleteAllUsersAsyncTaskListener {
        void success();
    }

    public DeleteAllUsersAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                   DeleteAllUsersAsyncTaskListener deleteAllUsersAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.deleteAllUsersAsyncTaskListener = deleteAllUsersAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.userDao().deleteAllUsers();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllUsersAsyncTaskListener.success();
    }
}
