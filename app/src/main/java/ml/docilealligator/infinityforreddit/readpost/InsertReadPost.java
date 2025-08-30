package ml.docilealligator.infinityforreddit.readpost;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId, int readPostsLimit) {
        executor.execute(() -> {
            ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
            int limit = Math.max(readPostsLimit, 100);
            boolean isReadPostLimit = readPostsLimit != -1;
            while (readPostDao.getReadPostsCount(username) > limit && isReadPostLimit) {
                readPostDao.deleteOldestReadPosts(username);
            }
            if (username != null && !username.isEmpty()) {
                readPostDao.insert(new ReadPost(username, postId));
            }
        });
    }
}
