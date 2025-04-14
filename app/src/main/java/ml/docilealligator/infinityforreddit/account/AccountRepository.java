package ml.docilealligator.infinityforreddit.account;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountRepository {
    private final Executor mExecutor;
    private final AccountDao mAccountDao;
    private final LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;
    private final LiveData<Account> mCurrentAccountLiveData;
    private final LiveData<List<Account>> mAllAccountsLiveData;

    AccountRepository(Executor executor,  RedditDataRoomDatabase redditDataRoomDatabase) {
        mExecutor = executor;
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

    public void insert(Account account) {
        mExecutor.execute(() -> mAccountDao.insert(account));
    }
}
