package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.user.UserDao;
import ml.docilealligator.infinityforreddit.user.UserData;

public class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
    private UserDao userDao;
    private UserData userData;
    private InsertUserDataCallback insertUserDataCallback;

    public InsertUserDataAsyncTask(UserDao userDao, UserData userData, InsertUserDataCallback insertUserDataCallback) {
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
