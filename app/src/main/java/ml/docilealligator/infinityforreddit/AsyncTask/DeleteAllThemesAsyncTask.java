package ml.docilealligator.infinityforreddit.AsyncTask;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class DeleteAllThemesAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private SharedPreferences lightThemeSharedPreferences;
    private SharedPreferences darkThemeSharedPreferences;
    private SharedPreferences amoledThemeSharedPreferences;
    private DeleteAllThemesAsyncTaskListener deleteAllThemesAsyncTaskListener;

    public interface DeleteAllThemesAsyncTaskListener {
        void success();
    }

    public DeleteAllThemesAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase,
                                    SharedPreferences lightThemeSharedPreferences,
                                    SharedPreferences darkThemeSharedPreferences,
                                    SharedPreferences amoledThemeSharedPreferences,
                                    DeleteAllThemesAsyncTaskListener deleteAllThemesAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.lightThemeSharedPreferences = lightThemeSharedPreferences;
        this.darkThemeSharedPreferences = darkThemeSharedPreferences;
        this.amoledThemeSharedPreferences = amoledThemeSharedPreferences;
        this.deleteAllThemesAsyncTaskListener = deleteAllThemesAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.customThemeDao().deleteAllCustomThemes();
        lightThemeSharedPreferences.edit().clear().apply();
        darkThemeSharedPreferences.edit().clear().apply();
        amoledThemeSharedPreferences.edit().clear().apply();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteAllThemesAsyncTaskListener.success();
    }
}
