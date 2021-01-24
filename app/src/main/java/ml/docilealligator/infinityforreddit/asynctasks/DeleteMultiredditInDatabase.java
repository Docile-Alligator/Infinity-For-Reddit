package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteMultiredditInDatabase {

    public static void deleteMultiredditInDatabase(Executor executor, Handler handler,
                                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                                   String accountName, String multipath,
                                                   DeleteMultiredditInDatabaseListener deleteMultiredditInDatabaseListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.multiRedditDao().deleteMultiReddit(multipath, accountName);
            handler.post(deleteMultiredditInDatabaseListener::success);
        });
    }
    public interface DeleteMultiredditInDatabaseListener {
        void success();
    }
}
