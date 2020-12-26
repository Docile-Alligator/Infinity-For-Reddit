package ml.docilealligator.infinityforreddit.readpost;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

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
