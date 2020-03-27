package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String themeName;
    private DeleteThemeAsyncTaskListener deleteThemeAsyncTaskListener;
    private boolean isLightTheme = false;
    private boolean isDarkTheme = false;
    private boolean isAmoledTheme = false;

    public interface DeleteThemeAsyncTaskListener {
        void success(boolean isLightTheme, boolean isDarkTheme, boolean isAmoledTheme);
    }

    public DeleteThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String themeName,
                                DeleteThemeAsyncTaskListener deleteThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.themeName = themeName;
        this.deleteThemeAsyncTaskListener = deleteThemeAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        CustomTheme customTheme = redditDataRoomDatabase.customThemeDao().getCustomTheme(themeName);
        if (customTheme != null) {
            isLightTheme = customTheme.isLightTheme;
            isDarkTheme = customTheme.isDarkTheme;
            isAmoledTheme = customTheme.isAmoledTheme;
            redditDataRoomDatabase.customThemeDao().deleteCustomTheme(themeName);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteThemeAsyncTaskListener.success(isLightTheme, isDarkTheme, isAmoledTheme);
    }
}
