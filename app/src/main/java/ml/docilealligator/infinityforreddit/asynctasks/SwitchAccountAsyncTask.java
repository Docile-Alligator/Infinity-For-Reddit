package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SwitchAccountAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String newAccountName;
    private Account account;
    private SwitchAccountAsyncTaskListener switchAccountAsyncTaskListener;
    public SwitchAccountAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String newAccountName,
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

    public interface SwitchAccountAsyncTaskListener {
        void switched(Account account);
    }
}
