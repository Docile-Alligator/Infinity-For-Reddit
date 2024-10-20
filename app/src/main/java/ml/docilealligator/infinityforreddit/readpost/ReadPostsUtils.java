package ml.docilealligator.infinityforreddit.readpost;

import android.content.SharedPreferences;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ReadPostsUtils {
    public static int GetReadPostsLimit(String accountName, SharedPreferences mPostHistorySharedPreferences) {
        if (mPostHistorySharedPreferences.getBoolean(accountName + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, true)) {
            return mPostHistorySharedPreferences.getInt(accountName + SharedPreferencesUtils.READ_POSTS_LIMIT, 500);
        } else {
            return -1;
        }
    }
}
