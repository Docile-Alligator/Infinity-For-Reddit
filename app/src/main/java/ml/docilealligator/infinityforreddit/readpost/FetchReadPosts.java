package ml.docilealligator.infinityforreddit.readpost;

import android.os.AsyncTask;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class FetchReadPosts {

    public interface FetchReadPostsListener {
        void success(ArrayList<ReadPost> readPosts);
    }

    public static void fetchReadPosts(RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                      FetchReadPostsListener fetchReadPostsListener) {
        new FetchAllReadPostsAsyncTask(redditDataRoomDatabase, username, fetchReadPostsListener).execute();
    }

    private static class FetchAllReadPostsAsyncTask extends AsyncTask<Void, Void, Void> {

        private RedditDataRoomDatabase redditDataRoomDatabase;
        private String username;
        private FetchReadPostsListener fetchReadPostsListener;
        private ArrayList<ReadPost> readPosts;

        private FetchAllReadPostsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                           FetchReadPostsListener fetchReadPostsListener) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.username = username;
            this.fetchReadPostsListener = fetchReadPostsListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            readPosts = (ArrayList<ReadPost>) redditDataRoomDatabase.readPostDao().getAllReadPosts(username);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fetchReadPostsListener.success(readPosts);
        }
    }
}
