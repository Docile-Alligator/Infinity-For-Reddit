package ml.docilealligator.infinityforreddit;

import SubscribedSubredditDatabase.SubscribedSubredditData;
import android.os.AsyncTask;

class CheckIsSubscribedToSubredditAsyncTask extends AsyncTask<Void, Void, Void> {

  private final RedditDataRoomDatabase redditDataRoomDatabase;
  private final String subredditName;
  private final String accountName;
  private final CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener;
  private SubscribedSubredditData subscribedSubredditData;

  CheckIsSubscribedToSubredditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
      String subredditName, String accountName,
      CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
    this.redditDataRoomDatabase = redditDataRoomDatabase;
    this.subredditName = subredditName;
    this.accountName = accountName;
    this.checkIsSubscribedToSubredditListener = checkIsSubscribedToSubredditListener;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    subscribedSubredditData = redditDataRoomDatabase.subscribedSubredditDao()
        .getSubscribedSubreddit(subredditName, accountName);
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    if (subscribedSubredditData != null) {
      checkIsSubscribedToSubredditListener.isSubscribed();
    } else {
      checkIsSubscribedToSubredditListener.isNotSubscribed();
    }
  }

  interface CheckIsSubscribedToSubredditListener {

    void isSubscribed();

    void isNotSubscribed();
  }
}
