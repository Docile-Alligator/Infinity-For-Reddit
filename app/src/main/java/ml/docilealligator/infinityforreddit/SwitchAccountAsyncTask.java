package ml.docilealligator.infinityforreddit;

import Account.Account;
import android.os.AsyncTask;

class SwitchAccountAsyncTask extends AsyncTask<Void, Void, Void> {

  private final RedditDataRoomDatabase redditDataRoomDatabase;
  private final String newAccountName;
  private final SwitchAccountAsyncTaskListener switchAccountAsyncTaskListener;
  private Account account;

  SwitchAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String newAccountName,
      SwitchAccountAsyncTaskListener switchAccountAsyncTaskListener) {
    this.redditDataRoomDatabase = redditDataRoomDatabase;
    this.newAccountName = newAccountName;
    this.switchAccountAsyncTaskListener = switchAccountAsyncTaskListener;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    redditDataRoomDatabase.accountDao().markAllAccountsNonCurrent();
    redditDataRoomDatabase.accountDao().markAccountCurrent(newAccountName);
    account = redditDataRoomDatabase.accountDao().getCurrentAccount();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    switchAccountAsyncTaskListener.switched(account);
  }

  interface SwitchAccountAsyncTaskListener {

    void switched(Account account);
  }
}
