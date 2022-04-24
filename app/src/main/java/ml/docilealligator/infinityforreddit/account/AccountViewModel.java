package ml.docilealligator.infinityforreddit.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class AccountViewModel extends ViewModel {
    private AccountRepository mAccountRepository;
    private LiveData<List<Account>> mAccountsExceptCurrentAccountLiveData;
    private LiveData<Account> mCurrentAccountLiveData;
    private LiveData<List<Account>> mAllAccountsLiveData;

    public AccountViewModel(RedditDataRoomDatabase redditDataRoomDatabase) {
        mAccountRepository = new AccountRepository(redditDataRoomDatabase);
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

        private final RedditDataRoomDatabase mRedditDataRoomDatabase;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountViewModel(mRedditDataRoomDatabase);
        }
    }
}
