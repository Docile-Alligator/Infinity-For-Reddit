package ml.docilealligator.infinityforreddit.commentfilter;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SaveCommentFilter {
    public interface SaveCommentFilterListener {
        void success();
        void duplicate();
    }

    public static void saveCommentFilter(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                      CommentFilter commentFilter, String originalName, SaveCommentFilter.SaveCommentFilterListener saveCommentFilterListener) {
        executor.execute(() -> {
            if (!originalName.equals(commentFilter.name) &&
                    redditDataRoomDatabase.commentFilterDao().getCommentFilter(commentFilter.name) != null) {
                handler.post(saveCommentFilterListener::duplicate);
            } else {
                List<CommentFilterUsage> commentFilterUsages = redditDataRoomDatabase.commentFilterUsageDao().getAllCommentFilterUsage(originalName);
                if (!originalName.equals(commentFilter.name)) {
                    redditDataRoomDatabase.commentFilterDao().deleteCommentFilter(originalName);
                }
                redditDataRoomDatabase.commentFilterDao().insert(commentFilter);
                for (CommentFilterUsage commentFilterUsage : commentFilterUsages) {
                    commentFilterUsage.name = commentFilter.name;
                    redditDataRoomDatabase.commentFilterUsageDao().insert(commentFilterUsage);
                }
                handler.post(saveCommentFilterListener::success);
            }
        });
    }
}
