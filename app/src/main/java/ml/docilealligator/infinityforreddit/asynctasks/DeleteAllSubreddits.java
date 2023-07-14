package ml.ino6962.postinfinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class DeleteAllSubreddits {

    public static void deleteAllSubreddits(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                           DeleteAllSubredditsAsyncTaskListener deleteAllSubredditsAsyncTaskListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.subredditDao().deleteAllSubreddits();
            handler.post(deleteAllSubredditsAsyncTaskListener::success);
        });
    }

    public interface DeleteAllSubredditsAsyncTaskListener {
        void success();
    }
}
