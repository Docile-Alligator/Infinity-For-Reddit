package ml.docilealligator.infinityforreddit;

import Account.Account;
import Account.AccountDao;
import android.os.AsyncTask;

class GetCurrentAccountAsyncTask extends AsyncTask<Void, Void, Void> {

  final AccountDao accountDao;
  final GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener;
  Account account;

  GetCurrentAccountAsyncTask(AccountDao accountDao,
      GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener) {
    this.accountDao = accountDao;
    this.getCurrentAccountAsyncTaskListener = getCurrentAccountAsyncTaskListener;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    account = accountDao.getCurrentAccount();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    getCurrentAccountAsyncTaskListener.success(account);
  }

  interface GetCurrentAccountAsyncTaskListener {

    void success(Account account);
  }
}
