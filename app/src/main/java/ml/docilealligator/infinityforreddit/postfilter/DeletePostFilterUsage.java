package ml.docilealligator.infinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeletePostFilterUsage {
    public static void deletePostFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                             PostFilterUsage postFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.postFilterUsageDao().deletePostFilterUsage(postFilterUsage));
    }
}
