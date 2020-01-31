package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<SubscribedSubredditData> subscribedSubredditDataList;
    private List<SubscribedUserData> subscribedUserDataList;
    private List<SubredditData> subredditDataList;
    private InsertSubscribedThingListener mInsertSubscribedThingListener;

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, @Nullable String accountName,
                                           List<SubscribedSubredditData> subscribedSubredditDataList,
                                           List<SubscribedUserData> subscribedUserDataList,
                                           List<SubredditData> subredditDataList,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mAccountName = accountName;
        mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mSubredditDao = redditDataRoomDatabase.subredditDao();

        this.subscribedSubredditDataList = subscribedSubredditDataList;
        this.subscribedUserDataList = subscribedUserDataList;
        this.subredditDataList = subredditDataList;
        mInsertSubscribedThingListener = insertSubscribedThingListener;
    }

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                           SubscribedSubredditData subscribedSubredditDataList,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mSubscribedSubredditDao = redditDataRoomDatabase.subscribedSubredditDao();
        mAccountName = subscribedSubredditDataList.getUsername();
        mSingleSubscribedSubredditData = subscribedSubredditDataList;
        mInsertSubscribedThingListener = insertSubscribedThingListener;
    }

    public InsertSubscribedThingsAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                           SubscribedUserData subscribedUserDataList,
                                           InsertSubscribedThingListener insertSubscribedThingListener) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mAccountName = subscribedUserDataList.getUsername();
        mSingleSubscribedUserData = subscribedUserDataList;
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
            if (subscribedSubredditDataList != null) {
                List<SubscribedSubredditData> existingSubscribedSubredditDataList =
                        mSubscribedSubredditDao.getAllSubscribedSubredditsList(mAccountName);
                Collections.sort(subscribedSubredditDataList, (subscribedSubredditData, t1) -> subscribedSubredditData.getName().compareToIgnoreCase(t1.getName()));
                List<String> unsubscribedSubreddits = new ArrayList<>();
                compareTwoSubscribedSubredditList(subscribedSubredditDataList, existingSubscribedSubredditDataList,
                        unsubscribedSubreddits, 0, 0);

                for (String unsubscribed : unsubscribedSubreddits) {
                    mSubscribedSubredditDao.deleteSubscribedSubreddit(unsubscribed, mAccountName);
                }

                for (SubscribedSubredditData s : subscribedSubredditDataList) {
                    mSubscribedSubredditDao.insert(s);
                }
            }

            if (subscribedUserDataList != null) {
                List<SubscribedUserData> existingSubscribedUserDataList =
                        mSubscribedUserDao.getAllSubscribedUsersList(mAccountName);
                Collections.sort(subscribedUserDataList, (subscribedUserData, t1) -> subscribedUserData.getName().compareToIgnoreCase(t1.getName()));
                List<String> unsubscribedUsers = new ArrayList<>();
                compareTwoSubscribedUserList(subscribedUserDataList, existingSubscribedUserDataList,
                        unsubscribedUsers, 0, 0);

                for (String unsubscribed : unsubscribedUsers) {
                    mSubscribedUserDao.deleteSubscribedUser(unsubscribed, mAccountName);
                }

                for (SubscribedUserData s : subscribedUserDataList) {
                    mSubscribedUserDao.insert(s);
                }
            }

            if (subredditDataList != null) {
                for (SubredditData s : subredditDataList) {
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

    private void compareTwoSubscribedSubredditList(List<SubscribedSubredditData> newSubscribedSubreddits,
                                                   List<SubscribedSubredditData> oldSubscribedSubreddits,
                                                   List<String> unsubscribedSubredditNames, int i1, int i2) {
        if (newSubscribedSubreddits.size() <= i1 && oldSubscribedSubreddits.size() <= i2) {
            return;
        }

        if (newSubscribedSubreddits.size() <= i1) {
            for (int i = 0; i < oldSubscribedSubreddits.size(); i++) {
                unsubscribedSubredditNames.add(oldSubscribedSubreddits.get(i).getName());
            }
            return;
        }

        if (oldSubscribedSubreddits.size() > i2) {
            if (newSubscribedSubreddits.get(i1).getName().compareToIgnoreCase(oldSubscribedSubreddits.get(i2).getName()) == 0) {
                compareTwoSubscribedSubredditList(newSubscribedSubreddits, oldSubscribedSubreddits, unsubscribedSubredditNames, i1 + 1, i2 + 1);
            } else if (newSubscribedSubreddits.get(i1).getName().compareToIgnoreCase(oldSubscribedSubreddits.get(i2).getName()) < 0) {
                compareTwoSubscribedSubredditList(newSubscribedSubreddits, oldSubscribedSubreddits, unsubscribedSubredditNames, i1 + 1, i2);
            } else {
                unsubscribedSubredditNames.add(oldSubscribedSubreddits.get(i2).getName());
                compareTwoSubscribedSubredditList(newSubscribedSubreddits, oldSubscribedSubreddits, unsubscribedSubredditNames, i1, i2 + 1);
            }
        }
    }

    private void compareTwoSubscribedUserList(List<SubscribedUserData> newSubscribedUsers,
                                                   List<SubscribedUserData> oldSubscribedUsers,
                                                   List<String> unsubscribedUserNames, int i1, int i2) {
        if (newSubscribedUsers.size() <= i1 && oldSubscribedUsers.size() <= i2) {
            return;
        }

        if (newSubscribedUsers.size() <= i1) {
            for (int i = 0; i < oldSubscribedUsers.size(); i++) {
                unsubscribedUserNames.add(oldSubscribedUsers.get(i).getName());
            }
            return;
        }

        if (oldSubscribedUsers.size() > i2) {
            if (newSubscribedUsers.get(i1).getName().compareToIgnoreCase(oldSubscribedUsers.get(i2).getName()) == 0) {
                compareTwoSubscribedUserList(newSubscribedUsers, oldSubscribedUsers, unsubscribedUserNames, i1 + 1, i2 + 1);
            } else if (newSubscribedUsers.get(i1).getName().compareToIgnoreCase(oldSubscribedUsers.get(i2).getName()) < 0) {
                compareTwoSubscribedUserList(newSubscribedUsers, oldSubscribedUsers, unsubscribedUserNames, i1 + 1, i2);
            } else {
                unsubscribedUserNames.add(oldSubscribedUsers.get(i2).getName());
                compareTwoSubscribedUserList(newSubscribedUsers, oldSubscribedUsers, unsubscribedUserNames, i1, i2 + 1);
            }
        }
    }

    public interface InsertSubscribedThingListener {
        void insertSuccess();
    }
}
