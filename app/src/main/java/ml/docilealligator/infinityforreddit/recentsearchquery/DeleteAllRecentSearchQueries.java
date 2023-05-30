package ml.docilealligator.infinityforreddit.recentsearchquery;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllRecentSearchQueries {
    public interface DeleteAllRecentSearchQueriesListener {
        void success();
    }

    public static void deleteAllRecentSearchQueriesListener(RedditDataRoomDatabase redditDataRoomDatabase,  String username,
                                                       DeleteAllRecentSearchQueriesListener deleteAllRecentSearchQueriesListener) {
        new DeleteRecentSearchQueryAsyncTask(redditDataRoomDatabase, username, deleteAllRecentSearchQueriesListener).execute();
    }

    private static class DeleteRecentSearchQueryAsyncTask extends AsyncTask<Void, Void, Void> {

        private RecentSearchQueryDao recentSearchQueryDao;
        private String username;
        private DeleteAllRecentSearchQueriesListener deleteAllRecentSearchQueriesListener;

        public DeleteRecentSearchQueryAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                                String username,
                                                DeleteAllRecentSearchQueriesListener deleteRecentSearchQueryListener) {
            this.recentSearchQueryDao = redditDataRoomDatabase.recentSearchQueryDao();
            this.username = username;
            this.deleteAllRecentSearchQueriesListener = deleteRecentSearchQueryListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recentSearchQueryDao.deleteAllRecentSearchQueries(username);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deleteAllRecentSearchQueriesListener.success();
        }
    }
}
