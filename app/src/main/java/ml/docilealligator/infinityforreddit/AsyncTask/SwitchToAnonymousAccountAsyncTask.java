package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.Account.AccountDao;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SwitchToAnonymousAccountAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private boolean removeCurrentAccount;
    private SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener;
    public SwitchToAnonymousAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, boolean removeCurrentAccount,
                                             SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.removeCurrentAccount = removeCurrentAccount;
        this.switchToAnonymousAccountAsyncTaskListener = switchToAnonymousAccountAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AccountDao accountDao = redditDataRoomDatabase.accountDao();
        if (removeCurrentAccount) {
            accountDao.deleteCurrentAccount();
        }
        accountDao.markAllAccountsNonCurrent();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switchToAnonymousAccountAsyncTaskListener.logoutSuccess();
    }

    public interface SwitchToAnonymousAccountAsyncTaskListener {
        void logoutSuccess();
    }
}
