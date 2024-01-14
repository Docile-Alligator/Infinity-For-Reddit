package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;

public class DeleteMultiredditInDatabase {

    public static void deleteMultiredditInDatabase(Executor executor, Handler handler,
                                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                                   @NonNull String accountName, String multipath,
                                                   DeleteMultiredditInDatabaseListener deleteMultiredditInDatabaseListener) {
        executor.execute(() -> {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                redditDataRoomDatabase.multiRedditDao().anonymousDeleteMultiReddit(multipath);
            } else {
                redditDataRoomDatabase.multiRedditDao().deleteMultiReddit(multipath, accountName);
            }
            handler.post(deleteMultiredditInDatabaseListener::success);
        });
    }
    public interface DeleteMultiredditInDatabaseListener {
        void success();
    }
}
