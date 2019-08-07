package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;

class CheckIsSubscribedToSubredditAsyncTask extends AsyncTask<Void, Void, Void> {

    private SubscribedSubredditDao subscribedSubredditDao;
    private String subredditName;
    private String accountName;
    private SubscribedSubredditData subscribedSubredditData;
    private CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener;

    interface CheckIsSubscribedToSubredditListener {
        void isSubscribed();
        void isNotSubscribed();
    }

    CheckIsSubscribedToSubredditAsyncTask(SubscribedSubredditDao subscribedSubredditDao,
                                          String subredditName, String accountName,
                                          CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
        this.subscribedSubredditDao = subscribedSubredditDao;
        this.subredditName =subredditName;
        this.accountName = accountName;
        this.checkIsSubscribedToSubredditListener = checkIsSubscribedToSubredditListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        subscribedSubredditData = subscribedSubredditDao.getSubscribedSubreddit(subredditName, accountName);
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
