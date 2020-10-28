package ml.docilealligator.infinityforreddit.SubredditFilter;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertSubredditFilter {
    public interface InsertSubredditFilterListener {
        void success();
    }

    public static void insertSubredditFilter(RedditDataRoomDatabase redditDataRoomDatabase, SubredditFilter subredditFilter,
                                      InsertSubredditFilterListener insertSubredditFilterListener) {
        new InsertSubredditFilterAsyncTask(redditDataRoomDatabase, subredditFilter, insertSubredditFilterListener).execute();
    }

    private static class InsertSubredditFilterAsyncTask extends AsyncTask<Void, Void, Void> {
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private SubredditFilter subredditFilter;
        private InsertSubredditFilterListener insertSubredditFilterListener;

        InsertSubredditFilterAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, SubredditFilter subredditFilter,
                                       InsertSubredditFilterListener insertSubredditFilterListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.subredditFilter = subredditFilter;
            this.insertSubredditFilterListener = insertSubredditFilterListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            redditDataRoomDatabase.subredditFilterDao().insert(subredditFilter);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            insertSubredditFilterListener.success();
        }
    }
}
