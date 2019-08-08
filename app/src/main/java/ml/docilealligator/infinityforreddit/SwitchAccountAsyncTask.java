package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

class SwitchAccountAsyncTask extends AsyncTask<Void, Void, Void> {
    interface SwitchAccountAsyncTaskListener {
        void switched();
    }

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String newAccountName;
    private SwitchAccountAsyncTaskListener switchAccountAsyncTaskListener;

    SwitchAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String newAccountName,
                           SwitchAccountAsyncTaskListener switchAccountAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.newAccountName = newAccountName;
        this.switchAccountAsyncTaskListener = switchAccountAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.accountDao().markAllAccountsNonCurrent();
        redditDataRoomDatabase.accountDao().markAccountCurrent(newAccountName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switchAccountAsyncTaskListener.switched();
    }
}
