package ml.docilealligator.infinityforreddit.readpost;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;

public class ReadPostModification {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId, @ReadPostType int readPostType,
                                      int readPostsLimit) {
        executor.execute(() -> {
            ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
            int limit = Math.max(readPostsLimit, 100);
            boolean isReadPostLimit = readPostsLimit != -1;
            while (readPostDao.getReadPostsCount(username, readPostType) > limit && isReadPostLimit) {
                readPostDao.deleteOldestReadPosts(username, readPostType);
            }
            if (username != null && !username.isEmpty()) {
                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                }

                readPostDao.insert(new ReadPost(username, postId, readPostType));
            }
        });
    }

    public static void deleteReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId, @ReadPostType int readPostType) {
        executor.execute(() -> {
            if (username != null && !username.isEmpty()) {
                redditDataRoomDatabase.readPostDao().deleteReadPost(username, postId, readPostType);
            }
        });
    }
}
