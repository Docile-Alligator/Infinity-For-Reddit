package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteThemeAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String themeName;

    public DeleteThemeAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String themeName) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.themeName = themeName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.customThemeDao().deleteCustomTheme(themeName);
        return null;
    }
}
