package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import User.User;
import User.UserDao;

public class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
    private UserDao userDao;
    private User user;
    private InsertUserDataCallback insertUserDataCallback;

    public interface InsertUserDataCallback {
        void insertSuccess();
    }

    public InsertUserDataAsyncTask(UserDao userDao, User user, InsertUserDataCallback insertUserDataCallback) {
        this.userDao = userDao;
        this.user = user;
        this.insertUserDataCallback = insertUserDataCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        userDao.insert(user);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(insertUserDataCallback != null) {
            insertUserDataCallback.insertSuccess();
        }
    }
}
