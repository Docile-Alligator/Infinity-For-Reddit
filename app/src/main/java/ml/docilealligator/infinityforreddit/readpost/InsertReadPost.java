package ml.docilealligator.infinityforreddit.readpost;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, String username, String postId) {
        new InsertReadPostAsyncTask(redditDataRoomDatabase, username, postId).execute();
    }

    private static class InsertReadPostAsyncTask extends AsyncTask<Void, Void, Void> {

        private RedditDataRoomDatabase redditDataRoomDatabase;
        private String username;
        private String postId;

        public InsertReadPostAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String username, String postId) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.username = username;
            this.postId = postId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
            if (readPostDao.getReadPostsCount() > 500) {
                readPostDao.deleteOldestReadPosts(username);
            }
            if (username != null && !username.equals("")) {
                readPostDao.insert(new ReadPost(username, postId));
            }
            return null;
        }
    }
}
