package ml.docilealligator.infinityforreddit.postfilter;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SavePostFilter {
    public static final int ERROR_DUPLICATE_NAME = 1;

    public interface SavePostFilterListener {
        //Need to make sure it is running in the UI thread.
        void success();
        void failed(int errorCode);
    }

    public static void savePostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      PostFilter postFilter, SavePostFilterListener savePostFilterListener) {
        executor.execute(() -> {
            if (redditDataRoomDatabase.postFilterDao().getPostFilter(postFilter.name) == null) {
                redditDataRoomDatabase.postFilterDao().insert(postFilter);
                savePostFilterListener.success();
            } else {
                savePostFilterListener.failed(ERROR_DUPLICATE_NAME);
            }
        });
    }
}
