package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.Account.AccountDao;

class SwitchToAnonymousAccountAsyncTask extends AsyncTask<Void, Void, Void> {
    interface SwitchToAnonymousAccountAsyncTaskListener {
        void logoutSuccess();
    }

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private boolean removeCurrentAccount;
    private SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener;

    SwitchToAnonymousAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, boolean removeCurrentAccount,
                                      SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.removeCurrentAccount = removeCurrentAccount;
        this.switchToAnonymousAccountAsyncTaskListener = switchToAnonymousAccountAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AccountDao accountDao = redditDataRoomDatabase.accountDao();
        if(removeCurrentAccount) {
            accountDao.deleteCurrentAccount();
        }
        accountDao.markAllAccountsNonCurrent();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switchToAnonymousAccountAsyncTaskListener.logoutSuccess();
    }
}
