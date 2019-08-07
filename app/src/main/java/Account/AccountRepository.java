package Account;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

public class AccountRepository {
    private AccountDao mAccountDao;
    private LiveData<Account> mAccountLiveData;

    AccountRepository(Application application, String username) {
        mAccountDao = AccountRoomDatabase.getDatabase(application).accountDao();

        mAccountLiveData = mAccountDao.getAccountLiveData(username);
    }

    LiveData<Account> getAccountLiveData() {
        return mAccountLiveData;
    }

    public void insert(Account Account) {
        new InsertAsyncTask(mAccountDao).execute(Account);
    }

    private static class InsertAsyncTask extends AsyncTask<Account, Void, Void> {

        private AccountDao mAsyncTaskDao;

        InsertAsyncTask(AccountDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Account... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
