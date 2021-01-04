package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;

public class GetCurrentAccount {

    public static void getCurrentAccount(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                         GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener) {
        executor.execute(() -> {
            Account account = redditDataRoomDatabase.accountDao().getCurrentAccount();
            handler.post(() -> getCurrentAccountAsyncTaskListener.success(account));
        });
    }

    public interface GetCurrentAccountAsyncTaskListener {
        void success(Account account);
    }
}
