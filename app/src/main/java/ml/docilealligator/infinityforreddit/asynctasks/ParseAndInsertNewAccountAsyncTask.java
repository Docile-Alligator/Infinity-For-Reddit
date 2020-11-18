package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountDao;

public class ParseAndInsertNewAccountAsyncTask extends AsyncTask<Void, Void, Void> {

    private String username;
    private String accessToken;
    private String refreshToken;
    private String profileImageUrl;
    private String bannerImageUrl;
    private int karma;
    private String code;
    private AccountDao accountDao;
    private ParseAndInsertAccountListener parseAndInsertAccountListener;
    public ParseAndInsertNewAccountAsyncTask(String username, String accessToken, String refreshToken, String profileImageUrl, String bannerImageUrl,
                                             int karma, String code, AccountDao accountDao,
                                             ParseAndInsertAccountListener parseAndInsertAccountListener) {
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        this.code = code;
        this.accountDao = accountDao;
        this.parseAndInsertAccountListener = parseAndInsertAccountListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Account account = new Account(username, accessToken, refreshToken, code, profileImageUrl,
                bannerImageUrl, karma, true);
        accountDao.markAllAccountsNonCurrent();
        accountDao.insert(account);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        parseAndInsertAccountListener.success();
    }

    public interface ParseAndInsertAccountListener {
        void success();
    }
}
