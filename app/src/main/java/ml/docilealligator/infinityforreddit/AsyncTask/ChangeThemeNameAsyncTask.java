package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class ChangeThemeNameAsyncTask extends AsyncTask<Void, Void, Void>  {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String oldName;
    private String newName;

    public ChangeThemeNameAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String oldName, String newName) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        redditDataRoomDatabase.customThemeDao().updateName(oldName, newName);
        return null;
    }
}
