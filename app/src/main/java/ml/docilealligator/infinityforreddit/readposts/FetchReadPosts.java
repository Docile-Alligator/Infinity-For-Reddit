package ml.docilealligator.infinityforreddit.readposts;

import android.os.AsyncTask;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;

public class FetchReadPosts {

    public interface FetchReadPostsListener {
        void success(ArrayList<ReadPost> readPosts, ArrayList<SubredditFilter> subredditFilters);
    }

    public static void fetchReadPosts(RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                      boolean fetchSubredditFilter, FetchReadPostsListener fetchReadPostsListener) {
        new FetchAllReadPostsAsyncTask(redditDataRoomDatabase, username, fetchSubredditFilter, fetchReadPostsListener).execute();
    }

    private static class FetchAllReadPostsAsyncTask extends AsyncTask<Void, Void, Void> {

        private RedditDataRoomDatabase redditDataRoomDatabase;
        private String username;
        private boolean fetchSubredditFilter;
        private FetchReadPostsListener fetchReadPostsListener;
        private ArrayList<ReadPost> readPosts;
        private ArrayList<SubredditFilter> subredditFilters;

        private FetchAllReadPostsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                           boolean fetchSubredditFilter, FetchReadPostsListener fetchReadPostsListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.username = username;
            this.fetchSubredditFilter = fetchSubredditFilter;
            this.fetchReadPostsListener = fetchReadPostsListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            readPosts = (ArrayList<ReadPost>) redditDataRoomDatabase.readPostDao().getAllReadPosts(username);
            if (fetchSubredditFilter) {
                subredditFilters = (ArrayList<SubredditFilter>) redditDataRoomDatabase.subredditFilterDao().getAllSubredditFilters();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fetchReadPostsListener.success(readPosts, subredditFilters);
        }
    }
}
