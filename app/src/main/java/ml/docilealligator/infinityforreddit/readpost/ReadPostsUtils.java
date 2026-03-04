package ml.docilealligator.infinityforreddit.readpost;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ReadPostsUtils {
    public static int GetReadPostsLimit(String accountName, @Nullable SharedPreferences mPostHistorySharedPreferences) {
        if (mPostHistorySharedPreferences == null) {
            return -1;
        }

        if (Account.ANONYMOUS_ACCOUNT.equals(accountName)
                || mPostHistorySharedPreferences.getBoolean(accountName + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, true)) {
            return mPostHistorySharedPreferences.getInt(accountName + SharedPreferencesUtils.READ_POSTS_LIMIT, 500);
        } else {
            return -1;
        }
    }
}
