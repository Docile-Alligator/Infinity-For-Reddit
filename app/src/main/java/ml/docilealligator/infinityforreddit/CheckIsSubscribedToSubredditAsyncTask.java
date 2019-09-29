package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;

class CheckIsSubscribedToSubredditAsyncTask extends AsyncTask<Void, Void, Void> {

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String subredditName;
    private String accountName;
    private SubscribedSubredditData subscribedSubredditData;
    private CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener;

    interface CheckIsSubscribedToSubredditListener {
        void isSubscribed();
        void isNotSubscribed();
    }

    CheckIsSubscribedToSubredditAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                          String subredditName, String accountName,
                                          CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.subredditName =subredditName;
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
        if(subscribedSubredditData != null) {
            checkIsSubscribedToSubredditListener.isSubscribed();
        } else {
            checkIsSubscribedToSubredditListener.isNotSubscribed();
        }
    }
}
