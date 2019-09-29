package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;

public class CheckIsSubscribedToSubredditAsyncTask extends AsyncTask<Void, Void, Void> {

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String subredditName;
    private String accountName;
    private SubscribedSubredditData subscribedSubredditData;
    private CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener;

    public CheckIsSubscribedToSubredditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                                 String subredditName, String accountName,
                                                 CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.subredditName = subredditName;
        this.accountName = accountName;
        this.checkIsSubscribedToSubredditListener = checkIsSubscribedToSubredditListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        subscribedSubredditData = redditDataRoomDatabase.subscribedSubredditDao().getSubscribedSubreddit(subredditName, accountName);
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

    public interface CheckIsSubscribedToSubredditListener {
        void isSubscribed();

        void isNotSubscribed();
    }
}
