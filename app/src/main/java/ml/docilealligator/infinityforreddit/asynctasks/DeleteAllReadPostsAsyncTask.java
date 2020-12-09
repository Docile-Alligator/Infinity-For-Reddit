package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllReadPostsAsyncTask extends AsyncTask<Void, Void, Void> {

    public interface DeleteAllReadPostsAsyncTaskListener {
        void success();
    }

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private DeleteAllReadPostsAsyncTaskListener deleteAllReadPostsAsyncTaskListener;

    public DeleteAllReadPostsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                       DeleteAllReadPostsAsyncTaskListener deleteAllReadPostsAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.deleteAllReadPostsAsyncTaskListener = deleteAllReadPostsAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.readPostDao().deleteAllReadPosts();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllReadPostsAsyncTaskListener.success();
    }
}
