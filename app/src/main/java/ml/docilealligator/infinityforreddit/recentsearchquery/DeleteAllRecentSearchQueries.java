package ml.docilealligator.infinityforreddit.recentsearchquery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllRecentSearchQueries {
    public static void deleteAllRecentSearchQueriesListener(Context context, RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                                            DeleteAllRecentSearchQueriesListener deleteAllRecentSearchQueriesListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.confirm_delete_all_recent_searches);
        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteRecentSearchQueryAsyncTask(context, redditDataRoomDatabase, username, deleteAllRecentSearchQueriesListener).execute();
            }
        });
        builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface DeleteAllRecentSearchQueriesListener {
        void success();
    }

    private static class DeleteRecentSearchQueryAsyncTask extends AsyncTask<Void, Void, Void> {

        private RecentSearchQueryDao recentSearchQueryDao;
        private String username;
        private DeleteAllRecentSearchQueriesListener deleteAllRecentSearchQueriesListener;
        private Context context;

        public DeleteRecentSearchQueryAsyncTask(Context context, RedditDataRoomDatabase redditDataRoomDatabase,
                                                String username,
                                                DeleteAllRecentSearchQueriesListener deleteRecentSearchQueryListener) {
            this.recentSearchQueryDao = redditDataRoomDatabase.recentSearchQueryDao();
            this.username = username;
            this.deleteAllRecentSearchQueriesListener = deleteRecentSearchQueryListener;
            this.context = context;
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
