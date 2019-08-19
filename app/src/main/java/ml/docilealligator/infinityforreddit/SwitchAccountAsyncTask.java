package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import Account.Account;

class SwitchAccountAsyncTask extends AsyncTask<Void, Void, Void> {
    interface SwitchAccountAsyncTaskListener {
        void switched(Account account);
    }

    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String newAccountName;
    private Account account;
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
        account = redditDataRoomDatabase.accountDao().getCurrentAccount();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        switchAccountAsyncTaskListener.switched(account);
    }
}
