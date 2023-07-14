package ml.ino6962.postinfinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class DeletePostFilterUsage {
    public static void deletePostFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                             PostFilterUsage postFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.postFilterUsageDao().deletePostFilterUsage(postFilterUsage));
    }
}
