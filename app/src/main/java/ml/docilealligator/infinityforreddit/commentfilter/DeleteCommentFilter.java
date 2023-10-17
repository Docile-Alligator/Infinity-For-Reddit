package ml.docilealligator.infinityforreddit.commentfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteCommentFilter {
    public static void deleteCommentFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor, CommentFilter commentFilter) {
        executor.execute(() -> redditDataRoomDatabase.commentFilterDao().deleteCommentFilter(commentFilter));
    }
}
