package SubscribedSubredditDatabase;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SubscribedSubredditViewModel extends AndroidViewModel {
    private SubscribedSubredditRepository mSubscribedSubredditRepository;
    private LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;

    public SubscribedSubredditViewModel(Application application) {
        super(application);
        mSubscribedSubredditRepository = new SubscribedSubredditRepository(application);
        mAllSubscribedSubreddits = mSubscribedSubredditRepository.getAllSubscribedSubreddits();
    }

    public LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
        return mAllSubscribedSubreddits;
    }

    public void insert(SubscribedSubredditData subscribedSubredditData) {
        mSubscribedSubredditRepository.insert(subscribedSubredditData);
    }
}
