package ml.docilealligator.infinityforreddit.asynctasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.Map;

import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class DeleteAllSortTypesAsyncTask extends AsyncTask<Void, Void, Void> {
    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences sortTypeSharedPreferences;
    private DeleteAllSortTypesAsyncTaskListener deleteAllSortTypesAsyncTaskListener;

    public interface DeleteAllSortTypesAsyncTaskListener {
        void success();
    }

    public DeleteAllSortTypesAsyncTask(SharedPreferences defaultSharedPreferences,
                                       SharedPreferences sortTypeSharedPreferences,
                                       DeleteAllSortTypesAsyncTaskListener deleteAllSortTypesAsyncTaskListener) {
        this.defaultSharedPreferences = defaultSharedPreferences;
        this.sortTypeSharedPreferences = sortTypeSharedPreferences;
        this.deleteAllSortTypesAsyncTaskListener = deleteAllSortTypesAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Map<String,?> keys = defaultSharedPreferences.getAll();
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            String key = entry.getKey();
            if (key.contains(SharedPreferencesUtils.SORT_TYPE_BEST_POST) || key.contains(SharedPreferencesUtils.SORT_TIME_BEST_POST)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_ALL_POST_LEGACY) || key.contains(SharedPreferencesUtils.SORT_TIME_ALL_POST_LEGACY)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_POPULAR_POST_LEGACY) || key.contains(SharedPreferencesUtils.SORT_TIME_POPULAR_POST_LEGACY)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_SEARCH_POST) || key.contains(SharedPreferencesUtils.SORT_TIME_SEARCH_POST)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_SUBREDDIT_POST_BASE) || key.contains(SharedPreferencesUtils.SORT_TIME_SUBREDDIT_POST_BASE)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_MULTI_REDDIT_POST_BASE) || key.contains(SharedPreferencesUtils.SORT_TIME_MULTI_REDDIT_POST_BASE)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_USER_POST_BASE) || key.contains(SharedPreferencesUtils.SORT_TIME_USER_POST_BASE)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_USER_COMMENT) || key.contains(SharedPreferencesUtils.SORT_TIME_USER_COMMENT)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_SEARCH_SUBREDDIT) || key.contains(SharedPreferencesUtils.SORT_TYPE_SEARCH_USER)
                    || key.contains(SharedPreferencesUtils.SORT_TYPE_POST_COMMENT)) {
                editor.remove(key);
            }
        }
        editor.apply();

        sortTypeSharedPreferences.edit().clear().apply();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllSortTypesAsyncTaskListener.success();
    }
}
