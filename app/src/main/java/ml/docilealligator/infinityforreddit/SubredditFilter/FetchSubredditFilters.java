package ml.docilealligator.infinityforreddit.SubredditFilter;

import android.os.AsyncTask;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class FetchSubredditFilters {
    public interface FetchSubredditFiltersListener {
        void success(ArrayList<SubredditFilter>subredditFilters);
    }

    public static void fetchSubredditFilters(RedditDataRoomDatabase redditDataRoomDatabase,
                                             FetchSubredditFiltersListener fetchSubredditFiltersListener) {
        new FetchSubredditFiltersAsyncTask(redditDataRoomDatabase, fetchSubredditFiltersListener).execute();
    }

    private static class FetchSubredditFiltersAsyncTask extends AsyncTask<Void, Void, Void> {
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private ArrayList<SubredditFilter> subredditFilters;
        private FetchSubredditFiltersListener fetchSubredditFiltersListener;

        FetchSubredditFiltersAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                       FetchSubredditFiltersListener fetchSubredditFiltersListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.fetchSubredditFiltersListener = fetchSubredditFiltersListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            subredditFilters = (ArrayList<SubredditFilter>) redditDataRoomDatabase.subredditFilterDao().getAllSubredditFilters();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fetchSubredditFiltersListener.success(subredditFilters);
        }
    }
}
