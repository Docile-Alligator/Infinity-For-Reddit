package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;

public class CheckIsFollowingUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private SubscribedUserDao subscribedUserDao;
    private String username;
    private String accountName;
    private SubscribedUserData subscribedUserData;
    private CheckIsFollowingUserListener checkIsFollowingUserListener;

    public interface CheckIsFollowingUserListener {
        void isSubscribed();
        void isNotSubscribed();
    }

    public CheckIsFollowingUserAsyncTask(SubscribedUserDao subscribedUserDao, String username, String accountName,
                                         CheckIsFollowingUserListener checkIsFollowingUserListener) {
        this.subscribedUserDao = subscribedUserDao;
        this.username = username;
        this.accountName = accountName;
        this.checkIsFollowingUserListener = checkIsFollowingUserListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        subscribedUserData = subscribedUserDao.getSubscribedUser(username, accountName);
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
