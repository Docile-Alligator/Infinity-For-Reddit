package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditDao;
import ml.docilealligator.infinityforreddit.SubscribedSubredditDatabase.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;

public class InsertSubscribedThingsAsyncTask extends AsyncTask<Void, Void, Void> {

    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private String mAccountName;
    private SubscribedSubredditDao mSubscribedSubredditDao;
    private SubscribedUserDao mSubscribedUserDao;
    private SubredditDao mSubredditDao;
    private SubscribedSubredditData mSingleSubscribedSubredditData;
    private SubscribedUserData mSingleSubscribedUserData;
    private List<SubscribedSubredditData> subscribedSubredditData;
    private List<SubscribedUserData> subscribedUserData;
    private List<SubredditData> subredditData;
    private InsertSubscribedThingListener mInsertSubscribedThingListener;

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, @Nullable String accountName,
                                           List<SubscribedSubredditData> subscribedSubredditData,
                                           List<SubscribedUserData> subscribedUserData,
                                           List<SubredditData> subredditData,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccountName = accountName;
        mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mSubredditDao = redditDataRoomDatabase.subredditDao();

        this.subscribedSubredditData = subscribedSubredditData;
        this.subscribedUserData = subscribedUserData;
        this.subredditData = subredditData;
        mInsertSubscribedThingListener = insertSubscribedThingListener;
    }

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                           SubscribedSubredditData subscribedSubredditData,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
        mAccountName = subscribedSubredditData.getUsername();
        mSingleSubscribedSubredditData = subscribedSubredditData;
        mInsertSubscribedThingListener = insertSubscribedThingListener;
    }

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                           SubscribedUserData subscribedUserData,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mAccountName = subscribedUserData.getUsername();
        mSingleSubscribedUserData = subscribedUserData;
        mInsertSubscribedThingListener = insertSubscribedThingListener;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        if (mAccountName != null && mRedditDataRoomDatabase.accountDao().getAccountData(mAccountName) == null) {
            return null;
        }

        if (mSingleSubscribedSubredditData != null) {
            mSubscribedSubredditDao.insert(mSingleSubscribedSubredditData);
        } else if (mSingleSubscribedUserData != null) {
            mSubscribedUserDao.insert(mSingleSubscribedUserData);
        } else {
            if (subscribedSubredditData != null) {
                for (SubscribedSubredditData s : subscribedSubredditData) {
                    mSubscribedSubredditDao.insert(s);
                }
            }

            if (subscribedUserData != null) {
                for (SubscribedUserData s : subscribedUserData) {
                    mSubscribedUserDao.insert(s);
                }
            }

            if (subredditData != null) {
                for (SubredditData s : subredditData) {
                    mSubredditDao.insert(s);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mInsertSubscribedThingListener.insertSuccess();
    }

    public interface InsertSubscribedThingListener {
        void insertSuccess();
    }
}
