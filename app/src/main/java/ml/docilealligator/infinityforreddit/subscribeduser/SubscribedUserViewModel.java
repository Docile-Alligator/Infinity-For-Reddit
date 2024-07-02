package ml.docilealligator.infinityforreddit.subscribeduser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedUserViewModel extends ViewModel {
    private final SubscribedUserRepository mSubscribedUserRepository;
    private final LiveData<List<SubscribedUserData>> mAllSubscribedUsers;
    private final LiveData<List<SubscribedUserData>> mAllFavoriteSubscribedUsers;
    private final MutableLiveData<String> searchQueryLiveData;

    public SubscribedUserViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mSubscribedUserRepository = new SubscribedUserRepository(redditDataRoomDatabase, accountName);
        searchQueryLiveData = new MutableLiveData<>("");

        mAllSubscribedUsers = Transformations.switchMap(searchQueryLiveData, searchQuery -> mSubscribedUserRepository.getAllSubscribedUsersWithSearchQuery(searchQuery));
        mAllFavoriteSubscribedUsers = Transformations.switchMap(searchQueryLiveData, searchQuery -> mSubscribedUserRepository.getAllFavoriteSubscribedUsersWithSearchQuery(searchQuery));
    }

    public LiveData<List<SubscribedUserData>> getAllSubscribedUsers() {
        return mAllSubscribedUsers;
    }

    public LiveData<List<SubscribedUserData>> getAllFavoriteSubscribedUsers() {
        return mAllFavoriteSubscribedUsers;
    }

    public void insert(SubscribedUserData subscribedUserData) {
        mSubscribedUserRepository.insert(subscribedUserData);
    }

    public void setSearchQuery(String searchQuery) {
        searchQueryLiveData.postValue(searchQuery);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mAccountName;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mAccountName = accountName;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubscribedUserViewModel(mRedditDataRoomDatabase, mAccountName);
        }
    }
}
