package SubredditDatabase;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

public class SubredditViewModel extends AndroidViewModel {
    private SubredditRepository mSubredditRepository;
    private LiveData<SubredditData> mSubredditLiveData;

    public SubredditViewModel(Application application, String id) {
        super(application);
        mSubredditRepository = new SubredditRepository(application, id);
        mSubredditLiveData = mSubredditRepository.getSubredditLiveData();
    }

    public LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }

    public void insert(SubredditData subredditData) {
        mSubredditRepository.insert(subredditData);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final String subredditName;

        public Factory(@NonNull Application application, String subredditName) {
            mApplication = application;
            this.subredditName = subredditName;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new SubredditViewModel(mApplication, subredditName);
        }
    }
}
