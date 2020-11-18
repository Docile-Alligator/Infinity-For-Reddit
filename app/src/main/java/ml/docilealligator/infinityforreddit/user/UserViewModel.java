package ml.docilealligator.infinityforreddit.user;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class UserViewModel extends AndroidViewModel {
    private UserRepository mSubredditRepository;
    private LiveData<UserData> mUserLiveData;

    public UserViewModel(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String id) {
        super(application);
        mSubredditRepository = new UserRepository(redditDataRoomDatabase, id);
        mUserLiveData = mSubredditRepository.getUserLiveData();
    }

    public LiveData<UserData> getUserLiveData() {
        return mUserLiveData;
    }

    public void insert(UserData userData) {
        mSubredditRepository.insert(userData);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mUsername;

        public Factory(@NonNull Application application, RedditDataRoomDatabase redditDataRoomDatabase, String username) {
            mApplication = application;
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mUsername = username;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new UserViewModel(mApplication, mRedditDataRoomDatabase, mUsername);
        }
    }
}
