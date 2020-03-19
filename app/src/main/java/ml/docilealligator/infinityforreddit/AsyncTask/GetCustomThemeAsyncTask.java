package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class GetCustomThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String customThemeName;
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

    @Override
    protected Void doInBackground(Void... voids) {
        customTheme = redditDataRoomDatabase.customThemeDao().getCustomTheme(customThemeName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        getCustomThemeAsyncTaskListener.success(customTheme);
    }
}
