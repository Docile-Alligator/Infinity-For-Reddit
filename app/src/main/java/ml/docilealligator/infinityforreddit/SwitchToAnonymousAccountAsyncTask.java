package ml.docilealligator.infinityforreddit;

import Account.AccountDao;
import android.os.AsyncTask;

class SwitchToAnonymousAccountAsyncTask extends AsyncTask<Void, Void, Void> {

  private final RedditDataRoomDatabase redditDataRoomDatabase;
  private final boolean removeCurrentAccount;
  private final SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener;

  SwitchToAnonymousAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
      boolean removeCurrentAccount,
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

  interface SwitchToAnonymousAccountAsyncTaskListener {

    void logoutSuccess();
  }
}
