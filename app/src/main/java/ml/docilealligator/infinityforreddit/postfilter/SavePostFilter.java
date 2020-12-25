package ml.docilealligator.infinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SavePostFilter {
    public interface SavePostFilterListener {
        //Need to make sure it is running in the UI thread.
        void success();
    }

    public static void savePostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      PostFilter postFilter, SavePostFilterListener savePostFilterListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.postFilterDao().insert(postFilter);
            savePostFilterListener.success();
        });
    }
}
