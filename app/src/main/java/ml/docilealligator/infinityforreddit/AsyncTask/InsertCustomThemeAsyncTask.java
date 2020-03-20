package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertCustomThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener;
    private CustomTheme customTheme;

    public interface InsertCustomThemeAsyncTaskListener {
        void success();
    }

    public InsertCustomThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, CustomTheme customTheme,
                                   InsertCustomThemeAsyncTaskListener insertCustomThemeAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.customTheme = customTheme;
        this.insertCustomThemeAsyncTaskListener = insertCustomThemeAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.customThemeDao().insert(customTheme);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        insertCustomThemeAsyncTaskListener.success();
    }
}
