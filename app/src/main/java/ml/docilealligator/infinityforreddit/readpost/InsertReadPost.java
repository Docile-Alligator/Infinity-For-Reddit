package ml.docilealligator.infinityforreddit.readpost;

import java.util.concurrent.Executor;

import android.content.SharedPreferences;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId, SharedPreferences postHistorySharedPreferences) {
        int limit = Integer.parseInt(postHistorySharedPreferences.getString(username + SharedPreferencesUtils.READ_POSTS_LIMIT_BASE, "500"));
        boolean isLimit = postHistorySharedPreferences.getBoolean(username + SharedPreferencesUtils.LIMIT_READ_POSTS_BASE, true);
        executor.execute(() -> {
            ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
            while (readPostDao.getUserReadPostsCount(username) > limit && isLimit) {
                readPostDao.deleteOldestReadPosts(username);
            }
            if (username != null && !username.equals("")) {
                readPostDao.insert(new ReadPost(username, postId));
            }
        });
    }
}
