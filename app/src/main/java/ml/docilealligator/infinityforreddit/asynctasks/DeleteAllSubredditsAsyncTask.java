package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllSubredditsAsyncTask extends AsyncTask<Void, Void, Void> {

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private DeleteAllSubredditsAsyncTaskListener deleteAllSubredditsAsyncTaskListener;

    public interface DeleteAllSubredditsAsyncTaskListener {
        void success();
    }

    public DeleteAllSubredditsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                        DeleteAllSubredditsAsyncTaskListener deleteAllSubredditsAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.deleteAllSubredditsAsyncTaskListener = deleteAllSubredditsAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.subredditDao().deleteAllSubreddits();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllSubredditsAsyncTaskListener.success();
    }
}
