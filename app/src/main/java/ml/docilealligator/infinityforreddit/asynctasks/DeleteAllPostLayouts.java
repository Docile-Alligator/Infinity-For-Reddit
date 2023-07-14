package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.SharedPreferences;
import android.os.Handler;

import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class DeleteAllPostLayouts {

    public static void deleteAllPostLayouts(Executor executor, Handler handler, SharedPreferences defaultSharedPreferences,
                                            SharedPreferences postLayoutSharedPreferences,
                                            DeleteAllPostLayoutsAsyncTaskListener deleteAllPostLayoutsAsyncTaskListener) {
        executor.execute(() -> {
            Map<String,?> keys = defaultSharedPreferences.getAll();
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();

            for (Map.Entry<String,?> entry : keys.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(SharedPreferencesUtils.POST_LAYOUT_SHARED_PREFERENCES_FILE)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_POPULAR_POST_LEGACY)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_ALL_POST_LEGACY)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE)
                        || key.startsWith(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST)) {
                    editor.remove(key);
                }
            }
            editor.apply();

            postLayoutSharedPreferences.edit().clear().apply();

            handler.post(deleteAllPostLayoutsAsyncTaskListener::success);
        });
    }

    public interface DeleteAllPostLayoutsAsyncTaskListener {
        void success();
    }
}
