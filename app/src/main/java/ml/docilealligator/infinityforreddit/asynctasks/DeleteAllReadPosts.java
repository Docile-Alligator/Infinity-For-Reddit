package ml.ino6962.postinfinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class DeleteAllReadPosts {

    public static void deleteAllReadPosts(Executor executor, Handler handler,
                                          RedditDataRoomDatabase redditDataRoomDatabase,
                                          DeleteAllReadPostsAsyncTaskListener deleteAllReadPostsAsyncTaskListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.readPostDao().deleteAllReadPosts();
            handler.post(deleteAllReadPostsAsyncTaskListener::success);
        });
    }

    public interface DeleteAllReadPostsAsyncTaskListener {
        void success();
    }
}
