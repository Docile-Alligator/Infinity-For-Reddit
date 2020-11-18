package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.Map;

import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class DeleteAllPostLayoutsAsyncTask extends AsyncTask<Void, Void, Void> {

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences postLayoutSharedPreferences;
    private DeleteAllPostLayoutsAsyncTaskListener deleteAllPostLayoutsAsyncTaskListener;

    public interface DeleteAllPostLayoutsAsyncTaskListener {
        void success();
    }

    public DeleteAllPostLayoutsAsyncTask(SharedPreferences defaultSharedPreferences,
                                         SharedPreferences postLayoutSharedPreferences,
                                         DeleteAllPostLayoutsAsyncTaskListener deleteAllPostLayoutsAsyncTaskListener) {
        this.defaultSharedPreferences = defaultSharedPreferences;
        this.postLayoutSharedPreferences = postLayoutSharedPreferences;
        this.deleteAllPostLayoutsAsyncTaskListener = deleteAllPostLayoutsAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Map<String,?> keys = defaultSharedPreferences.getAll();
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        for(Map.Entry<String,?> entry : keys.entrySet()){
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
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllPostLayoutsAsyncTaskListener.success();
    }
}
