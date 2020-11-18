package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountDao;

public class GetCurrentAccountAsyncTask extends AsyncTask<Void, Void, Void> {

    private Account account;
    private AccountDao accountDao;
    private GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener;

    public GetCurrentAccountAsyncTask(AccountDao accountDao, GetCurrentAccountAsyncTaskListener getCurrentAccountAsyncTaskListener) {
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

    public interface GetCurrentAccountAsyncTaskListener {
        void success(Account account);
    }
}
