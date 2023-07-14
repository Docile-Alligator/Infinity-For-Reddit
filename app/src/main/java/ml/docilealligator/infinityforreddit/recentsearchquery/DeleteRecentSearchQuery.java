package ml.docilealligator.infinityforreddit.recentsearchquery;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteRecentSearchQuery {
    public interface DeleteRecentSearchQueryListener {
        void success();
    }

    public static void deleteRecentSearchQueryListener(RedditDataRoomDatabase redditDataRoomDatabase,
                                                       RecentSearchQuery recentSearchQuery,
                                                       DeleteRecentSearchQueryListener deleteRecentSearchQueryListener) {
        new DeleteRecentSearchQueryAsyncTask(redditDataRoomDatabase, recentSearchQuery, deleteRecentSearchQueryListener).execute();
    }

    private static class DeleteRecentSearchQueryAsyncTask extends AsyncTask<Void, Void, Void> {

        private RecentSearchQueryDao recentSearchQueryDao;
        private RecentSearchQuery recentSearchQuery;
        private DeleteRecentSearchQueryListener deleteRecentSearchQueryListener;

        public DeleteRecentSearchQueryAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                                RecentSearchQuery recentSearchQuery,
                                                DeleteRecentSearchQueryListener deleteRecentSearchQueryListener) {
            this.recentSearchQueryDao = redditDataRoomDatabase.recentSearchQueryDao();
            this.recentSearchQuery = recentSearchQuery;
            this.deleteRecentSearchQueryListener = deleteRecentSearchQueryListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recentSearchQueryDao.deleteRecentSearchQueries(recentSearchQuery);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deleteRecentSearchQueryListener.success();
        }
    }
}
