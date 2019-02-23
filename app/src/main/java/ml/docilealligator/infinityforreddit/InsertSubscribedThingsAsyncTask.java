package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import java.util.List;

import SubredditDatabase.SubredditDao;
import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;

class InsertSubscribedThingsAsyncTask extends AsyncTask<Void, Void, Void> {

    interface InsertSubscribedThingListener {
        void insertSuccess();
    }

    private SubscribedSubredditDao mSubscribedSubredditDao;
    private SubscribedUserDao mUserDao;
    private SubredditDao mSubredditDao;
    private List<SubscribedSubredditData> subscribedSubredditData;
    private List<SubscribedUserData> subscribedUserData;
    private List<SubredditData> subredditData;
    private InsertSubscribedThingListener insertSubscribedThingListener;

    InsertSubscribedThingsAsyncTask(SubscribedSubredditDao subscribedSubredditDao,
                                    SubscribedUserDao userDao,
                                    SubredditDao subredditDao,
                                    List<SubscribedSubredditData> subscribedSubredditData,
                                    List<SubscribedUserData> subscribedUserData,
                                    List<SubredditData> subredditData,
                                    InsertSubscribedThingListener insertSubscribedThingListener) {

        mSubscribedSubredditDao = subscribedSubredditDao;
        mUserDao = userDao;
        mSubredditDao = subredditDao;

        this.subscribedSubredditData = subscribedSubredditData;
        this.subscribedUserData = subscribedUserData;
        this.subredditData = subredditData;
        this.insertSubscribedThingListener = insertSubscribedThingListener;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        if(subscribedSubredditData != null) {
            for (SubscribedSubredditData s : subscribedSubredditData) {
                mSubscribedSubredditDao.insert(s);
            }
        }

        if(subscribedUserData != null) {
            for (SubscribedUserData s : subscribedUserData) {
                mUserDao.insert(s);
            }
        }

        if(subredditData != null) {
            for (SubredditData s : subredditData) {
                mSubredditDao.insert(s);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        insertSubscribedThingListener.insertSuccess();
    }
}
