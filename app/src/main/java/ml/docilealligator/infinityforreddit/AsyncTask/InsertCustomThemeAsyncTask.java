package ml.docilealligator.infinityforreddit.AsyncTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

public class InsertCustomThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private SharedPreferences lightThemeSharedPreferences;
    private SharedPreferences darkThemeSharedPreferences;
    private SharedPreferences amoledThemeSharedPreferences;
    private CustomTheme customTheme;
    private boolean checkDuplicate;
    private InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener;
    private boolean isDuplicate = false;

    public interface InsertCustomThemeAsyncTaskListener {
        void success();
        default void duplicate() {}
    }

    public InsertCustomThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                      SharedPreferences lightThemeSharedPreferences,
                                      SharedPreferences darkThemeSharedPreferences,
                                      SharedPreferences amoledThemeSharedPreferences,
                                      CustomTheme customTheme, boolean checkDuplicate,
                                      InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.lightThemeSharedPreferences = lightThemeSharedPreferences;
        this.darkThemeSharedPreferences = darkThemeSharedPreferences;
        this.amoledThemeSharedPreferences = amoledThemeSharedPreferences;
        this.customTheme = customTheme;
        this.checkDuplicate = checkDuplicate;
        this.insertCustomThemeAsyncTaskListener = insertCustomThemeAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (customTheme.isLightTheme) {
            redditDataRoomDatabase.customThemeDao().unsetLightTheme();
            CustomThemeSharedPreferencesUtils.insertThemeToSharedPreferences(customTheme, lightThemeSharedPreferences);
        }
        if (customTheme.isDarkTheme) {
            redditDataRoomDatabase.customThemeDao().unsetDarkTheme();
            CustomThemeSharedPreferencesUtils.insertThemeToSharedPreferences(customTheme, darkThemeSharedPreferences);
        }
        if (customTheme.isAmoledTheme) {
            redditDataRoomDatabase.customThemeDao().unsetAmoledTheme();
            CustomThemeSharedPreferencesUtils.insertThemeToSharedPreferences(customTheme, amoledThemeSharedPreferences);
        }
        if (checkDuplicate) {
            if (redditDataRoomDatabase.customThemeDao().getCustomTheme(customTheme.name) != null) {
                isDuplicate = true;
                return null;
            }
        }
        redditDataRoomDatabase.customThemeDao().insert(customTheme);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (isDuplicate) {
            insertCustomThemeAsyncTaskListener.duplicate();
        } else {
            insertCustomThemeAsyncTaskListener.success();
        }
    }
}
