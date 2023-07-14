package ml.docilealligator.infinityforreddit.account;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountRepository {
    private AccountDao mAccountDao;
    private LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;
    private LiveData<Account> mCurrentAccountLiveData;
    private LiveData<List<Account>> mAllAccountsLiveData;

    AccountRepository(RedditDataRoomDatabase redditDataRoomDatabase) {
        mAccountDao = redditDataRoomDatabase.accountDao();
        mAccountsExceptCurrentAccountLiveData = mAccountDao.getAccountsExceptCurrentAccountLiveData();
        mCurrentAccountLiveData = mAccountDao.getCurrentAccountLiveData();
        mAllAccountsLiveData = mAccountDao.getAllAccountsLiveData();
    }

    public LiveData<List<Account>> getAccountsExceptCurrentAccountLiveData() {
        return mAccountsExceptCurrentAccountLiveData;
    }

    public LiveData<Account> getCurrentAccountLiveData() {
        return mCurrentAccountLiveData;
    }

    public LiveData<List<Account>> getAllAccountsLiveData() {
        return mAllAccountsLiveData;
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
