package ml.docilealligator.infinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeletePostFilter {
    public static void deletePostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor, PostFilter postFilter) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                redditDataRoomDatabase.postFilterDao().deletePostFilter(postFilter);
            }
        });
    }
}
