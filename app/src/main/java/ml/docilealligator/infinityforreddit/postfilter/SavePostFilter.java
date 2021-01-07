package ml.docilealligator.infinityforreddit.postfilter;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SavePostFilter {
    public interface SavePostFilterListener {
        void success();
        void duplicate();
    }

    public static void savePostFilter(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                      PostFilter postFilter, String originalName, SavePostFilterListener savePostFilterListener) {
        executor.execute(() -> {
            if (!originalName.equals(postFilter.name) &&
                    redditDataRoomDatabase.postFilterDao().getPostFilter(postFilter.name) != null) {
                handler.post(savePostFilterListener::duplicate);
            } else {
                if (!originalName.equals(postFilter.name)) {
                    redditDataRoomDatabase.postFilterDao().deletePostFilter(originalName);
                }
                redditDataRoomDatabase.postFilterDao().insert(postFilter);
                handler.post(savePostFilterListener::success);
            }
        });
    }
}
