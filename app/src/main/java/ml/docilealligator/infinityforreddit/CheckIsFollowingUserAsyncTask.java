package ml.docilealligator.infinityforreddit;

import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;
import android.os.AsyncTask;

public class CheckIsFollowingUserAsyncTask extends AsyncTask<Void, Void, Void> {

  private final SubscribedUserDao subscribedUserDao;
  private final String username;
  private final String accountName;
  private final CheckIsFollowingUserListener checkIsFollowingUserListener;
  private SubscribedUserData subscribedUserData;

  CheckIsFollowingUserAsyncTask(SubscribedUserDao subscribedUserDao, String username,
      String accountName,
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
    if (subscribedUserData != null) {
      checkIsFollowingUserListener.isSubscribed();
    } else {
      checkIsFollowingUserListener.isNotSubscribed();
    }
  }

  interface CheckIsFollowingUserListener {

    void isSubscribed();

    void isNotSubscribed();
  }
}
