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

            String redgifsAccessToken = currentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "");
            currentAccountSharedPreferences.edit().clear().apply();
            currentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, redgifsAccessToken).apply();

            handler.post(switchToAnonymousAccountAsyncTaskListener::logoutSuccess);
        });
    }

    public interface SwitchToAnonymousAccountAsyncTaskListener {
        void logoutSuccess();
    }
}
