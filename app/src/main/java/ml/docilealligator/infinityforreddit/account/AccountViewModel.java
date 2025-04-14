package ml.docilealligator.infinityforreddit.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountViewModel extends ViewModel {
    private final AccountRepository mAccountRepository;
    private final LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;
    private final LiveData<Account> mCurrentAccountLiveData;
    private final LiveData<List<Account>> mAllAccountsLiveData;

    public AccountViewModel(Executor executor, RedditDataRoomDatabase redditDataRoomDatabase) {
        mAccountRepository = new AccountRepository(executor, redditDataRoomDatabase);
        mAccountsExceptCurrentAccountLiveData = mAccountRepository.getAccountsExceptCurrentAccountLiveData();
        mCurrentAccountLiveData = mAccountRepository.getCurrentAccountLiveData();
        mAllAccountsLiveData = mAccountRepository.getAllAccountsLiveData();
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

    public void insert(Account userData) {
        mAccountRepository.insert(userData);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Executor mExecutor;
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;

        public Factory(Executor executor, RedditDataRoomDatabase redditDataRoomDatabase) {
            mExecutor = executor;
            mRedditDataRoomDatabase = redditDataRoomDatabase;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountViewModel(mExecutor, mRedditDataRoomDatabase);
        }
    }
}
