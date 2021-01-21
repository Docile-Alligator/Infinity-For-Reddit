package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.SharedPreferences;
import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.AccountDao;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SwitchToAnonymousMode {
    public static void switchToAnonymousMode(RedditDataRoomDatabase redditDataRoomDatabase, SharedPreferences currentAccountSharedPreferences,
                                             Executor executor, Handler handler, boolean removeCurrentAccount,
                                             SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener) {
        executor.execute(() -> {
            AccountDao accountDao = redditDataRoomDatabase.accountDao();
            if (removeCurrentAccount) {
                accountDao.deleteCurrentAccount();
            }
            accountDao.markAllAccountsNonCurrent();

            currentAccountSharedPreferences.edit().remove(SharedPreferencesUtils.ACCESS_TOKEN).remove(SharedPreferencesUtils.ACCOUNT_NAME).apply();

            handler.post(switchToAnonymousAccountAsyncTaskListener::logoutSuccess);
        });
    }

    public interface SwitchToAnonymousAccountAsyncTaskListener {
        void logoutSuccess();
    }
}
