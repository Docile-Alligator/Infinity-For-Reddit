package ml.docilealligator.infinityforreddit;

import Account.Account;
import Account.AccountDao;
import android.os.AsyncTask;

class ParseAndInsertNewAccountAsyncTask extends AsyncTask<Void, Void, Void> {

  private final String username;
  private final String accessToken;
  private final String refreshToken;
  private final String profileImageUrl;
  private final String bannerImageUrl;
  private final int karma;
  private final String code;
  private final AccountDao accountDao;
  private final ParseAndInsertAccountListener parseAndInsertAccountListener;

  ParseAndInsertNewAccountAsyncTask(String username, String accessToken, String refreshToken,
      String profileImageUrl, String bannerImageUrl,
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

  interface ParseAndInsertAccountListener {

    void success();
  }
}
