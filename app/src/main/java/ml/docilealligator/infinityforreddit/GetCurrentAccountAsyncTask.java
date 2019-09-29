package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.Account.Account;
import ml.docilealligator.infinityforreddit.Account.AccountDao;

class GetCurrentAccountAsyncTask extends AsyncTask<Void, Void, Void> {

    interface GetCurrentAccountAsyncTaskListener {
        void success(Account account);
    }

    Account account;
    AccountDao accountDao;
    GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener;

    GetCurrentAccountAsyncTask(AccountDao accountDao, GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener) {
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
}
