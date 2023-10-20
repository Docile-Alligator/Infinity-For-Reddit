package ml.docilealligator.infinityforreddit.commentfilter;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class FetchCommentFilter {
    public static void fetchCommentFilter(Executor executor, Handler handler,
                                          RedditDataRoomDatabase redditDataRoomDatabase,
                                          String subreddit, FetchCommentFilterListener fetchCommentFilterListener) {
        executor.execute(() -> {
            List<CommentFilter> commentFilterList = redditDataRoomDatabase.commentFilterDao().getValidCommentFilters(CommentFilterUsage.SUBREDDIT_TYPE, subreddit);
            CommentFilter commentFilter = CommentFilter.mergeCommentFilter(commentFilterList);

            handler.post(() -> fetchCommentFilterListener.success(commentFilter));
        });
    }

    public interface FetchCommentFilterListener {
        void success(CommentFilter commentFilter);
    }
}
