package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import Account.Account;
import Account.AccountDao;

class ParseAndInsertAccount extends AsyncTask<Void, Void, Void> {

    interface ParseAndInsertAccountListener {
        void success();
    }

    private String username;
    private String accessToken;
    private String refreshToken;
    private String profileImageUrl;
    private String bannerImageUrl;
    private int karma;
    private String code;
    private AccountDao accountDao;
    private ParseAndInsertAccountListener parseAndInsertAccountListener;

    ParseAndInsertAccount(String username, String accessToken, String refreshToken, String profileImageUrl, String bannerImageUrl,
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
        accountDao.insert(account);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        parseAndInsertAccountListener.success();
    }
}
