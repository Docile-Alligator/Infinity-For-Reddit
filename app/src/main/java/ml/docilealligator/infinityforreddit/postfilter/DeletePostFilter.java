package ml.docilealligator.infinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.database.RedditDataRoomDatabase;

public class DeletePostFilter {
    public static void deletePostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor, PostFilter postFilter) {
        executor.execute(() -> redditDataRoomDatabase.postFilterDao().deletePostFilter(postFilter));
    }
}
