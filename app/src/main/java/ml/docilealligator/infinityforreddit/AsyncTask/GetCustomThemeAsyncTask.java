package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

public class GetCustomThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String customThemeName;
    private int themeType;
    private GetCustomThemeAsyncTaskListener getCustomThemeAsyncTaskListener;
    private CustomTheme customTheme;

    public interface GetCustomThemeAsyncTaskListener {
        void success(CustomTheme customTheme);
    }

    public GetCustomThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                   String customThemeName,
                                   GetCustomThemeAsyncTaskListener getCustomThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.customThemeName = customThemeName;
        this.getCustomThemeAsyncTaskListener = getCustomThemeAsyncTaskListener;
    }

    public GetCustomThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                   int themeType,
                                   GetCustomThemeAsyncTaskListener getCustomThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.themeType = themeType;
        this.getCustomThemeAsyncTaskListener = getCustomThemeAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (customThemeName != null) {
            customTheme = redditDataRoomDatabase.customThemeDao().getCustomTheme(customThemeName);
        } else {
            switch (themeType) {
                case CustomThemeSharedPreferencesUtils.DARK:
                    customTheme = redditDataRoomDatabase.customThemeDao().getDarkCustomTheme();
                    break;
                case CustomThemeSharedPreferencesUtils.AMOLED:
                    customTheme = redditDataRoomDatabase.customThemeDao().getAmoledCustomTheme();
                    break;
                default:
                    customTheme = redditDataRoomDatabase.customThemeDao().getLightCustomTheme();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        getCustomThemeAsyncTaskListener.success(customTheme);
    }
}
