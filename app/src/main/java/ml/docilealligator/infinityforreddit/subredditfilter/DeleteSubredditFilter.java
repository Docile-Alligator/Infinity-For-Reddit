package ml.docilealligator.infinityforreddit.subredditfilter;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteSubredditFilter {
    public interface DeleteSubredditFilterListener {
        void success();
    }

    public static void deleteSubredditFilter(RedditDataRoomDatabase redditDataRoomDatabase, SubredditFilter subredditFilter,
                                             DeleteSubredditFilterListener deleteSubredditFilterListener) {
        new DeleteSubredditFilterAsyncTask(redditDataRoomDatabase, subredditFilter, deleteSubredditFilterListener).execute();
    }

    private static class DeleteSubredditFilterAsyncTask extends AsyncTask<Void, Void, Void> {
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private SubredditFilter subredditFilter;
        private DeleteSubredditFilterListener deleteSubredditFilterListener;

        DeleteSubredditFilterAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, SubredditFilter subredditFilter,
                                       DeleteSubredditFilterListener deleteSubredditFilterListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.subredditFilter = subredditFilter;
            this.deleteSubredditFilterListener = deleteSubredditFilterListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            redditDataRoomDatabase.subredditFilterDao().deleteSubredditFilter(subredditFilter);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deleteSubredditFilterListener.success();
        }
    }
}
