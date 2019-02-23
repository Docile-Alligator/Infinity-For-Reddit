package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;

public class CheckIsFollowingUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private SubscribedUserDao subscribedUserDao;
    private String userName;
    private SubscribedUserData subscribedUserData;
    private CheckIsFollowingUserListener checkIsFollowingUserListener;

    interface CheckIsFollowingUserListener {
        void isSubscribed();
        void isNotSubscribed();
    }

    CheckIsFollowingUserAsyncTask(SubscribedUserDao subscribedUserDao, String userName,
                                  CheckIsFollowingUserListener checkIsFollowingUserListener) {
        this.subscribedUserDao = subscribedUserDao;
        this.userName = userName;
        this.checkIsFollowingUserListener = checkIsFollowingUserListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        subscribedUserData = subscribedUserDao.getSubscribedUser(userName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(subscribedUserData != null) {
            checkIsFollowingUserListener.isSubscribed();
        } else {
            checkIsFollowingUserListener.isNotSubscribed();
        }
    }
}
