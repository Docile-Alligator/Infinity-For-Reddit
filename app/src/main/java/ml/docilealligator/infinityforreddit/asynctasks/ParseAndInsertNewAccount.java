package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.account.AccountDao;

public class ParseAndInsertNewAccount {

    public static void parseAndInsertNewAccount(Executor executor, Handler handler, String username,
                                                String accessToken, String refreshToken, String profileImageUrl,
                                                String bannerImageUrl, int karma, String code, AccountDao accountDao,
                                                ParseAndInsertAccountListener parseAndInsertAccountListener) {
        executor.execute(() -> {
            Account account = new Account(username, accessToken, refreshToken, code, profileImageUrl,
                    bannerImageUrl, karma, true);
            accountDao.markAllAccountsNonCurrent();
            accountDao.insert(account);

            handler.post(parseAndInsertAccountListener::success);
        });
    }

    public interface ParseAndInsertAccountListener {
        void success();
    }
}
