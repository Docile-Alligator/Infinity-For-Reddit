package Account;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountRepository {
    private AccountDao mAccountDao;
    private LiveData<Account> mAccountLiveData;
    private LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;

    AccountRepository(RedditDataRoomDatabase redditDataRoomDatabase, String username) {
        mAccountDao = redditDataRoomDatabase.accountDao();
        mAccountLiveData = mAccountDao.getAccountLiveData(username);
        mAccountsExceptCurrentAccountLiveData = mAccountDao.getAccountsExceptCurrentAccountLiveData();
    }

    LiveData<Account> getAccountLiveData() {
        return mAccountLiveData;
    }

    public LiveData<List<Account>> getAccountsExceptCurrentAccountLiveData() {
        return mAccountsExceptCurrentAccountLiveData;
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
