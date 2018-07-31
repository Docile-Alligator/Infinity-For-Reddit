package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

public class SubredditViewModel extends AndroidViewModel {
    private SubredditRepository mSubredditRepository;
    private LiveData<SubredditData> mSubredditData;

    public SubredditViewModel(Application application, String id) {
        super(application);
        mSubredditRepository = new SubredditRepository(application, id);
        mSubredditData = mSubredditRepository.getSubredditData();
    }

    public LiveData<SubredditData> getSubredditData() {
        return mSubredditData;
    }

    public void insert(SubredditData subredditData) {
        mSubredditRepository.insert(subredditData);
    }
}
