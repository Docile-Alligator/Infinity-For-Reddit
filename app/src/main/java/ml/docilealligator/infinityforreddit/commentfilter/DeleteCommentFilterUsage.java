package ml.docilealligator.infinityforreddit.commentfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteCommentFilterUsage {
    public static void deleteCommentFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                             CommentFilterUsage commentFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.commentFilterUsageDao().deleteCommentFilterUsage(commentFilterUsage));
    }
}
