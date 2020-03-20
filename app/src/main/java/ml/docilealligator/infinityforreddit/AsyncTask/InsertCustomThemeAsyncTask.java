package ml.docilealligator.infinityforreddit.AsyncTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

public class InsertCustomThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private SharedPreferences themeSharedPreferences;
    private CustomTheme customTheme;
    private InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener;

    public interface InsertCustomThemeAsyncTaskListener {
        void success();
    }

    public InsertCustomThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                      SharedPreferences themeSharedPreferences, CustomTheme customTheme,
                                      InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.themeSharedPreferences = themeSharedPreferences;
        this.customTheme = customTheme;
        this.insertCustomThemeAsyncTaskListener = insertCustomThemeAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.customThemeDao().insert(customTheme);
        CustomThemeSharedPreferencesUtils.insertThemeToSharedPreferences(customTheme, themeSharedPreferences);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        insertCustomThemeAsyncTaskListener.success();
    }
}
