package ml.docilealligator.infinityforreddit.commentfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SaveCommentFilterUsage {
    public static void saveCommentFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                           CommentFilterUsage commentFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.commentFilterUsageDao().insert(commentFilterUsage));
    }
}
