package ml.ino6962.postinfinityforreddit.readpost;

import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId) {
        executor.execute(() -> {
            ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
            if (readPostDao.getReadPostsCount() > 500) {
                readPostDao.deleteOldestReadPosts(username);
            }
            if (username != null && !username.equals("")) {
                readPostDao.insert(new ReadPost(username, postId));
            }
        });
    }
}
