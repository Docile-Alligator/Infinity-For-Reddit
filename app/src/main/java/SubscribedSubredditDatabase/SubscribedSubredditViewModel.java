package SubscribedSubredditDatabase;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditViewModel extends AndroidViewModel {

  private final SubscribedSubredditRepository mSubscribedSubredditRepository;
  private final LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;

  public SubscribedSubredditViewModel(Application application,
      RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
    super(application);
    mSubscribedSubredditRepository = new SubscribedSubredditRepository(redditDataRoomDatabase,
        accountName);
    mAllSubscribedSubreddits = mSubscribedSubredditRepository.getAllSubscribedSubreddits();
  }

  public LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
    return mAllSubscribedSubreddits;
  }

  public void insert(SubscribedSubredditData subscribedSubredditData) {
    mSubscribedSubredditRepository.insert(subscribedSubredditData);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Application mApplication;
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final String mAccountName;

    public Factory(Application application, RedditDataRoomDatabase redditDataRoomDatabase,
        String accountName) {
      this.mApplication = application;
      this.mRedditDataRoomDatabase = redditDataRoomDatabase;
      this.mAccountName = accountName;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new SubscribedSubredditViewModel(mApplication, mRedditDataRoomDatabase,
          mAccountName);
    }
  }
}
