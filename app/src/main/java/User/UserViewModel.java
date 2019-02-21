package User;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

public class UserViewModel extends AndroidViewModel {
    private UserRepository mSubredditRepository;
    private LiveData<UserData> mUserLiveData;

    public UserViewModel(Application application, String id) {
        super(application);
        mSubredditRepository = new UserRepository(application, id);
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

        private final String userName;

        public Factory(@NonNull Application application, String userName) {
            mApplication = application;
            this.userName = userName;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new UserViewModel(mApplication, userName);
        }
    }
}
