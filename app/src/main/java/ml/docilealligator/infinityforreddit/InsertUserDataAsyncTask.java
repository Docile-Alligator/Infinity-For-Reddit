package ml.docilealligator.infinityforreddit;

import User.UserDao;
import User.UserData;
import android.os.AsyncTask;

public class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

  private final UserDao userDao;
  private final UserData userData;
  private final InsertUserDataCallback insertUserDataCallback;

  public InsertUserDataAsyncTask(UserDao userDao, UserData userData,
      InsertUserDataCallback insertUserDataCallback) {
    this.userDao = userDao;
    this.userData = userData;
    this.insertUserDataCallback = insertUserDataCallback;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    userDao.insert(userData);
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    if (insertUserDataCallback != null) {
      insertUserDataCallback.insertSuccess();
    }
  }

  public interface InsertUserDataCallback {

    void insertSuccess();
  }
}
